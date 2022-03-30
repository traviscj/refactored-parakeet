package io.tcj.keyvalue;

import io.tcj.protos.kv.Empty;
import io.tcj.protos.kv.Kv;
import io.tcj.protos.kv.KvServiceListBlockingServer;
import io.tcj.protos.kv.ListKvResp;
import java.util.stream.Collectors;
import javax.inject.Inject;
import misk.web.actions.WebAction;
import org.jetbrains.annotations.NotNull;

public class KvListAction implements WebAction, KvServiceListBlockingServer {
  @Inject KvStore kvStore;

  @NotNull
  @Override
  public ListKvResp List(@NotNull Empty request) {
    return new ListKvResp.Builder()
        .kv(
            kvStore.kvRecords().stream()
                .map(r -> new Kv.Builder().ns(r.getNs()).k(r.getK()).v(r.getV()).build())
                .collect(Collectors.toList()))
        .build();
  }
}
