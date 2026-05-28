#!/usr/bin/env -S uv run --script
# /// script
# requires-python = ">=3.9"
# dependencies = ["fastapi", "uvicorn", "pymysql"]
# ///
"""Single-file FastAPI reimplementation of io.tcj.KvService with pluggable storage.

Mirrors the Misk gRPC wire contract from src/main/proto/io/tcj/kv.proto:
  POST /io.tcj.KvService/List           ListKvReq        -> ListKvResp
  POST /io.tcj.KvService/Put            Kv               -> Kv
  POST /io.tcj.KvService/ListNamespace  ListNamespaceReq -> ListNamespaceResp

Backends (selected at boot):
  * filesystem (default) -- <root>/<enc(ns)>/<enc(k)>, file contents == v.
  * mysql                -- the existing `kv` table (ns/k/v columns).

Backend selection:
  * No config           -> filesystem at KV_DATA_DIR (default ./kv-data).
  * KV_PROFILE=<name>   -> the [<name>] section of ~/.config/kv (override path
                           with KV_CONFIG). INI, one section per profile, e.g.:

      [local]
      backend  = mysql
      host     = localhost
      port     = 3307
      database = traviscj_localdev
      user     = root
      password =

      [aws]
      backend  = mysql
      host     = traviscj.xxxx.us-east-1.rds.amazonaws.com
      database = traviscj
      user     = traviscj

      [i711]
      backend  = mysql
      host     = i711.local
      database = traviscj

      [fs]
      backend  = filesystem
      path     = /var/lib/kv

Run (uv resolves deps from the inline script metadata above):
  ./kv_service.py                                 # filesystem, serves :1337
  KV_PROFILE=local uv run kv_service.py           # mysql via ~/.config/kv [local]
  KV_DATA_DIR=./kv-data PORT=8080 uv run kv_service.py
"""
import configparser
import os
import tempfile
from pathlib import Path
from typing import Dict, List, Optional
from urllib.parse import quote, unquote

from fastapi import FastAPI, HTTPException
from pydantic import AliasChoices, BaseModel, ConfigDict, Field

DEFAULT_LIMIT = 50  # proto2 default for both list RPCs
DEFAULT_NS = "-"  # matches the `ns` column default in v001__kv_entries.sql

ROOT = Path(os.environ.get("KV_DATA_DIR", "./kv-data"))
CONFIG_PATH = Path(os.environ.get("KV_CONFIG", Path.home() / ".config" / "kv"))


# --- proto messages ---------------------------------------------------------
class _Msg(BaseModel):
    # accept both proto snake_case and protobuf-JSON camelCase, ignore unknowns
    model_config = ConfigDict(populate_by_name=True, extra="ignore")


class Kv(_Msg):
    ns: Optional[str] = None
    k: Optional[str] = None
    v: Optional[str] = None


class ListKvReq(_Msg):
    ns: Optional[str] = None
    k_prefix: Optional[str] = Field(
        default=None, validation_alias=AliasChoices("k_prefix", "kPrefix")
    )
    limit: int = DEFAULT_LIMIT


class ListKvResp(_Msg):
    kv: List[Kv] = Field(default_factory=list)


class ListNamespaceReq(_Msg):
    ns_prefix: Optional[str] = Field(
        default=None, validation_alias=AliasChoices("ns_prefix", "nsPrefix")
    )
    limit: int = DEFAULT_LIMIT


class ListNamespaceResp(_Msg):
    ns: List[str] = Field(default_factory=list)


def _effective_limit(limit: Optional[int]) -> int:
    return limit if (limit is not None and limit > 0) else DEFAULT_LIMIT


# --- storage backends -------------------------------------------------------
class Store:
    """Backend contract. Put upserts on (ns, k); lists filter by prefix + limit."""

    def put(self, ns: Optional[str], k: str, v: Optional[str]) -> None:
        raise NotImplementedError

    def kv_records(self, ns: Optional[str], k_prefix: Optional[str], limit: Optional[int]) -> List[Kv]:
        raise NotImplementedError

    def namespaces(self, ns_prefix: Optional[str], limit: Optional[int]) -> List[str]:
        raise NotImplementedError

    def describe(self) -> Dict[str, object]:
        raise NotImplementedError


def _enc(s: str) -> str:
    return quote(s, safe="")


def _dec(s: str) -> str:
    return unquote(s)


class FilesystemStore(Store):
    """dir-per-namespace, file-per-key. ns/k percent-encoded into one safe path
    segment so arbitrary strings round-trip; prefix filters run on decoded values."""

    def __init__(self, root: Path):
        self.root = root

    def put(self, ns: Optional[str], k: str, v: Optional[str]) -> None:
        ns_dir = self.root / _enc(ns or DEFAULT_NS)
        ns_dir.mkdir(parents=True, exist_ok=True)
        dest = ns_dir / _enc(k)
        # write-then-rename so concurrent readers never see a torn value
        fd, tmp = tempfile.mkstemp(dir=ns_dir)
        try:
            with os.fdopen(fd, "w", encoding="utf-8") as f:
                f.write(v or "")
            os.replace(tmp, dest)
        except BaseException:
            if os.path.exists(tmp):
                os.unlink(tmp)
            raise

    def kv_records(self, ns: Optional[str], k_prefix: Optional[str], limit: Optional[int]) -> List[Kv]:
        if not self.root.exists():
            return []
        ns_dirs = [self.root / _enc(ns)] if ns else sorted(p for p in self.root.iterdir() if p.is_dir())
        out: List[Kv] = []
        for ns_dir in ns_dirs:
            if not ns_dir.is_dir():
                continue
            ns_name = _dec(ns_dir.name)
            for key_file in sorted(ns_dir.iterdir()):
                if not key_file.is_file():
                    continue
                k = _dec(key_file.name)
                if k_prefix and not k.startswith(k_prefix):
                    continue
                out.append(Kv(ns=ns_name, k=k, v=key_file.read_text(encoding="utf-8")))
        return out[: _effective_limit(limit)]

    def namespaces(self, ns_prefix: Optional[str], limit: Optional[int]) -> List[str]:
        if not self.root.exists():
            return []
        names = sorted(_dec(p.name) for p in self.root.iterdir() if p.is_dir())
        if ns_prefix:
            names = [n for n in names if n.startswith(ns_prefix)]
        return names[: _effective_limit(limit)]

    def describe(self) -> Dict[str, object]:
        return {"backend": "filesystem", "root": str(self.root.resolve())}


def _like_prefix(prefix: str) -> str:
    # escape LIKE metacharacters so a prefix is matched literally (then append %)
    esc = prefix.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
    return esc + "%"


class MySqlStore(Store):
    """Targets the existing `kv` table. Put is an upsert on the unique (ns, k)."""

    def __init__(self, host: str, port: int, user: str, password: str, database: str):
        import pymysql  # lazy: only needed when this backend is selected

        self._pymysql = pymysql
        self._cfg = dict(
            host=host, port=port, user=user, password=password,
            database=database, charset="utf8mb4", autocommit=True,
        )
        self._conn = None

    def _cursor(self):
        if self._conn is None:
            self._conn = self._pymysql.connect(**self._cfg)
        else:
            self._conn.ping(reconnect=True)
        return self._conn.cursor()

    def put(self, ns: Optional[str], k: str, v: Optional[str]) -> None:
        with self._cursor() as cur:
            cur.execute(
                "INSERT INTO kv (ns, k, v) VALUES (%s, %s, %s) "
                "ON DUPLICATE KEY UPDATE v = VALUES(v)",
                (ns or DEFAULT_NS, k, v or ""),
            )

    def kv_records(self, ns: Optional[str], k_prefix: Optional[str], limit: Optional[int]) -> List[Kv]:
        clauses: List[str] = []
        params: List[object] = []
        if ns:
            clauses.append("ns = %s")
            params.append(ns)
        if k_prefix:
            clauses.append("k LIKE %s")
            params.append(_like_prefix(k_prefix))
        where = (" WHERE " + " AND ".join(clauses)) if clauses else ""
        params.append(_effective_limit(limit))
        with self._cursor() as cur:
            cur.execute(f"SELECT ns, k, v FROM kv{where} ORDER BY ns, k LIMIT %s", params)
            return [Kv(ns=r[0], k=r[1], v=r[2]) for r in cur.fetchall()]

    def namespaces(self, ns_prefix: Optional[str], limit: Optional[int]) -> List[str]:
        where = ""
        params: List[object] = []
        if ns_prefix:
            where = " WHERE ns LIKE %s"
            params.append(_like_prefix(ns_prefix))
        params.append(_effective_limit(limit))
        with self._cursor() as cur:
            cur.execute(f"SELECT DISTINCT ns FROM kv{where} ORDER BY ns LIMIT %s", params)
            return [r[0] for r in cur.fetchall()]

    def describe(self) -> Dict[str, object]:
        return {k: self._cfg[k] for k in ("host", "port", "database")} | {"backend": "mysql"}


def make_store() -> Store:
    profile = os.environ.get("KV_PROFILE")
    if not profile:
        return FilesystemStore(ROOT)

    cp = configparser.ConfigParser(interpolation=None)  # interpolation off: passwords may contain %
    if not cp.read(CONFIG_PATH):
        raise RuntimeError(f"KV_PROFILE={profile!r} set but no config readable at {CONFIG_PATH}")
    if profile not in cp:
        raise RuntimeError(f"profile [{profile}] not in {CONFIG_PATH}; have {cp.sections()}")

    sec = cp[profile]
    backend = sec.get("backend", "mysql")
    if backend == "filesystem":
        return FilesystemStore(Path(sec.get("path", str(ROOT))))
    if backend == "mysql":
        return MySqlStore(
            host=sec.get("host", "localhost"),
            port=sec.getint("port", 3306),
            user=sec.get("user", "root"),
            password=sec.get("password", ""),
            database=sec.get("database", ""),
        )
    raise RuntimeError(f"unknown backend {backend!r} in profile [{profile}]")


# --- service ----------------------------------------------------------------
app = FastAPI(title="io.tcj.KvService (filesystem | mysql)")
store: Store = make_store()


@app.post("/io.tcj.KvService/List", response_model=ListKvResp)
def list_kv(req: ListKvReq) -> ListKvResp:
    return ListKvResp(kv=store.kv_records(req.ns, req.k_prefix, req.limit))


@app.post("/io.tcj.KvService/Put", response_model=Kv)
def put(req: Kv) -> Kv:
    if not req.k:
        raise HTTPException(status_code=400, detail="k is required")
    store.put(req.ns, req.k, req.v)
    return req


@app.post("/io.tcj.KvService/ListNamespace", response_model=ListNamespaceResp)
def list_namespace(req: ListNamespaceReq) -> ListNamespaceResp:
    return ListNamespaceResp(ns=store.namespaces(req.ns_prefix, req.limit))


@app.get("/_health")
def health() -> dict:
    return {"status": "ok", **store.describe()}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=int(os.environ.get("PORT", "1337")))
