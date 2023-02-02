package io.tcj;

import com.google.inject.AbstractModule;
import io.tcj.keyvalue.KvListRpc;
import io.tcj.keyvalue.KvPutRpc;
import io.tcj.mortgage.MortgageCalcRpc;
import misk.web.WebActionModule;

public class TraviscjAppModule extends AbstractModule {
  @Override
  protected void configure() {
//    bind()
    install(WebActionModule.create(MortgageCalcRpc.class));
    install(WebActionModule.create(KvListRpc.class));
    install(WebActionModule.create(KvPutRpc.class));

//    install(WebActionModule.create())
  }
}
