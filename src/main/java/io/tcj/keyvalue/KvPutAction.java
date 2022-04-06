package io.tcj.keyvalue;

import io.tcj.protos.kv.Kv;
import io.tcj.protos.kv.KvServicePutBlockingServer;
import javax.inject.Inject;
import misk.web.actions.WebAction;
import org.jetbrains.annotations.NotNull;

public class KvPutAction implements WebAction, KvServicePutBlockingServer {
  @Inject KvStore kvStore;

  @NotNull
  @Override
  public Kv Put(@NotNull Kv request) {
    kvStore.put(request.ns, request.k, request.v);
    return request;
  }
}
