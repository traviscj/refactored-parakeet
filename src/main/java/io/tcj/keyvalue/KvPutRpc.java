package io.tcj.keyvalue;

import io.tcj.protos.kv.Kv;
import io.tcj.protos.kv.KvServicePutBlockingServer;
import misk.security.authz.Authenticated;
import misk.web.actions.WebAction;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

public class KvPutRpc implements WebAction, KvServicePutBlockingServer {
  @Inject KvStore kvStore;

  @NotNull
  @Override
  @Authenticated(capabilities = {"admin_access"})
  public Kv Put(@NotNull Kv request) {
    kvStore.put(request.ns, request.k, request.v);
    return request;
  }
}
