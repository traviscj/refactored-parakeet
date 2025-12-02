package io.tcj.keyvalue;

import io.tcj.protos.kv.KvServiceListNamespaceBlockingServer;
import io.tcj.protos.kv.ListNamespaceReq;
import io.tcj.protos.kv.ListNamespaceResp;
import misk.security.authz.Authenticated;
import misk.web.actions.WebAction;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

public class KvListNamespaceRpc implements WebAction, KvServiceListNamespaceBlockingServer {
  @Inject KvStore kvStore;

  @NotNull
  @Override
  @Authenticated(capabilities = {"admin_access"})
  public ListNamespaceResp ListNamespace(@NotNull ListNamespaceReq request) {
    Integer limit = request.limit;
    return new ListNamespaceResp.Builder()
        .ns(kvStore.ns(request.ns_prefix, limit == null ? ListNamespaceReq.DEFAULT_LIMIT : limit))
        .build();
  }
}
