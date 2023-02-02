package io.tcj.keyvalue;

import io.tcj.protos.kv.Kv;
import io.tcj.protos.kv.KvServiceListBlockingServer;
import io.tcj.protos.kv.ListKvReq;
import io.tcj.protos.kv.ListKvResp;
import misk.web.actions.WebAction;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class KvListRpc implements WebAction, KvServiceListBlockingServer {
  @Inject KvStore kvStore;

  @NotNull
  @Override
  public ListKvResp List(@NotNull ListKvReq request) {
    return new ListKvResp.Builder()
        .kv(
            kvStore.kvRecords(request.ns, request.k_prefix, request.limit).stream()
                .map(r -> new Kv.Builder().ns(r.getNs()).k(r.getK()).v(r.getV()).build())
                .collect(Collectors.toList()))
        .build();
  }
}
