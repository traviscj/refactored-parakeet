package io.tcj;

import com.google.inject.AbstractModule;
import io.tcj.feeds.impl.KvFeedModule;
import io.tcj.keyvalue.KvListNamespaceRpc;
import io.tcj.keyvalue.KvListRpc;
import io.tcj.keyvalue.KvPutRpc;
import io.tcj.mortgage.MortgageCalcRpc;
import misk.web.WebActionModule;
import misk.web.metadata.all.AllMetadataModule;

public class TraviscjAppModule extends AbstractModule {
  @Override
  protected void configure() {
//    bind()
    install(WebActionModule.create(MortgageCalcRpc.class));
    install(WebActionModule.create(KvListRpc.class));
    install(WebActionModule.create(KvPutRpc.class));
    install(WebActionModule.create(KvListNamespaceRpc.class));

    install(new KvFeedModule());
//    install(WebActionModule.create())

    install(new AllMetadataModule());
  }
}
