package io.tcj;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import misk.MiskApplication;
import misk.MiskCaller;
import misk.MiskRealServiceModule;
import misk.config.ConfigModule;
import misk.config.MiskConfig;
import misk.environment.DeploymentModule;
import misk.resources.ResourceLoader;
import misk.security.authz.MiskCallerAuthenticator;
import misk.web.MiskWebModule;
import misk.web.dashboard.AdminDashboardModule;
import misk.web.dashboard.ConfigDashboardTabModule;
import wisp.deployment.Deployment;

public class TraviscjApp {
  public static void main(String[] args) {
    Deployment deployment = new Deployment("development", false, false, false, true);
    TraviscjConfig config =
        MiskConfig.load(
            TraviscjConfig.class,
            "traviscj",
            deployment,
            ImmutableList.of(),
            ResourceLoader.Companion.getSYSTEM());

    new MiskApplication(
            new MiskRealServiceModule(),
            new MiskWebModule(config.web),
            new TraviscjAppModule(),
            new ConfigModule<>(TraviscjConfig.class, "traviscj", config),
            new DeploymentModule(deployment),
            new AbstractModule() {
              @Override
              protected void configure() {
                Multibinder.newSetBinder(binder(), MiskCallerAuthenticator.class)
                    .addBinding()
                    .toInstance(
                        () -> new MiskCaller(null, "traviscj", ImmutableSet.of("admin_access")));
              }
            },
            new AdminDashboardModule(true),
            new ConfigDashboardTabModule(true))
        .run(args);
  }
}
