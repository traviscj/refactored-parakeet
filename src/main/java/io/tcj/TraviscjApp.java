package io.tcj;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import kotlin.Unit;
import misk.MiskApplication;
import misk.MiskCaller;
import misk.MiskRealServiceModule;
import misk.config.ConfigModule;
import misk.config.MiskConfig;
import misk.environment.DeploymentModule;
import misk.jdbc.DataSourceClusterConfig;
import misk.jdbc.RealDatabasePool;
import misk.jooq.JooqModule;
import misk.jooq.listeners.JooqTimestampRecordListenerOptions;
import misk.resources.ResourceLoader;
import misk.security.authz.MiskCallerAuthenticator;
import misk.web.MiskWebModule;
import misk.web.dashboard.AdminDashboardModule;
import misk.web.dashboard.ConfigDashboardTabModule;
import wisp.deployment.Deployment;
import wisp.deployment.DeploymentKt;

public class TraviscjApp {
    public static void main(String[] args) {
        Deployment deployment = DeploymentKt.getPRODUCTION();
        TraviscjConfig config =
                MiskConfig.load(
                        TraviscjConfig.class,
                        "traviscj",
                        deployment,
                        ImmutableList.of(),
                        ResourceLoader.Companion.getSYSTEM());

        DataSourceClusterConfig dataSourceClusterConfig = new DataSourceClusterConfig(config.data_source, config.data_source);

//        System.out.println("StartDatabaseService.class.getName() = " + StartDatabaseService.class.getName());
//        com.github.dockerjava.api.DockerClient docker = StartDatabaseService.Companion.getDocker();

        JooqModule jooqModule = new JooqModule(
                kotlin.jvm.JvmClassMappingKt.getKotlinClass(TraviscjDb.class),
                dataSourceClusterConfig,
                "jooq",
                RealDatabasePool.INSTANCE,
                kotlin.jvm.JvmClassMappingKt.getKotlinClass(TraviscjReadyOnlyDb.class),
                new JooqTimestampRecordListenerOptions(true, "created_at", "updated_at"),
                (configuration -> {
                    return Unit.INSTANCE;
                }));
        new MiskApplication(
                new MiskRealServiceModule(),
                new MiskWebModule(config.web),
                new TraviscjAppModule(),
                new ConfigModule<>(TraviscjConfig.class, "traviscj", config),
                new DeploymentModule(deployment),
                jooqModule,
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        Multibinder.newSetBinder(binder(), MiskCallerAuthenticator.class)
                                .addBinding()
                                .toInstance(
                                        () -> new MiskCaller(null, "traviscj", ImmutableSet.of("admin_access")));
                    }
                },
                new AdminDashboardModule(false),
                new ConfigDashboardTabModule(false))
                .run(args);
    }
}
