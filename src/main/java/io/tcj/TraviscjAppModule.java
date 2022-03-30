package io.tcj;

import com.google.inject.AbstractModule;
import io.tcj.keyvalue.KvListAction;
import io.tcj.keyvalue.KvPutAction;
import io.tcj.mortgage.MortgageCalcRpc;
import misk.web.WebActionModule;

public class TraviscjAppModule extends AbstractModule {
  @Override
  protected void configure() {
    install(WebActionModule.create(MortgageCalcRpc.class));
    install(WebActionModule.create(KvListAction.class));
    install(WebActionModule.create(KvPutAction.class));
  }
}
