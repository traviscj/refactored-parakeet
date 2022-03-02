package io.tcj;

import com.google.inject.AbstractModule;
import misk.web.WebActionModule;

public class TraviscjAppModule extends AbstractModule {
    @Override
    protected void configure() {
        install(WebActionModule.create(HelloJavaAction.class));
        install(WebActionModule.create(MortgageCalcAction.class));

//        install(new ConfigDashboardTabModule(true));
    }
}
