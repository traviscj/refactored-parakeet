package io.tcj;

import com.google.inject.AbstractModule;
import misk.web.WebActionModule;

public class TraviscjAppModule extends AbstractModule {
  @Override
  protected void configure() {
    install(WebActionModule.create(MortgageCalcRpc.class));
    install(WebActionModule.create(KvListAction.class));
    install(WebActionModule.create(KvPutAction.class));
  }
}
