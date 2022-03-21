package io.tcj;

import com.google.inject.AbstractModule;
import misk.web.WebActionModule;
import misk.web.dashboard.AdminDashboardModule;

public class TraviscjAppModule extends AbstractModule {
  @Override
  protected void configure() {
    install(WebActionModule.create(HelloJavaAction.class));
//    install(WebActionModule.create(MortgageCalcAction.class));
    install(WebActionModule.create(MortgageCalc2Action.class));
    install(WebActionModule.create(MortgageCalcProtoAction.class));
//    install(WebActionModule.create(MortgageCalc3Action.class));
//    install(WebActionModule.create(MortgageCalc4Action.class));

    //        install(new ConfigDashboardTabModule(true));
  }
}
