package io.tcj;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import io.tcj.db.TraviscjDb;
import io.tcj.db.TraviscjReadyOnlyDb;
import kotlin.Unit;
import misk.MiskTestingServiceModule;
import misk.config.ConfigModule;
import misk.config.MiskConfig;
import misk.environment.DeploymentModule;
import misk.jdbc.DataSourceClusterConfig;
import misk.jdbc.RealDatabasePool;
import misk.jooq.JooqModule;
import misk.jooq.listeners.JooqTimestampRecordListenerOptions;
import misk.resources.ResourceLoader;
import wisp.deployment.Deployment;
import wisp.deployment.DeploymentKt;

/**
 * Test wiring: mirrors {@link TraviscjApp}'s jOOQ setup but under the TESTING
 * deployment, loading config from traviscj-common.yaml + traviscj-testing.yaml
 * (the latter repoints the datasource at the misk_jooq_test_codegen schema).
 * Installed via {@code @MiskTestModule} so misk starts the DataSourceService and
 * makes the @TraviscjDb JooqTransacter injectable.
 */
public class TraviscjTestModule extends AbstractModule {
  @Override
  protected void configure() {
    Deployment deployment = DeploymentKt.getTESTING();
    TraviscjConfig config =
        MiskConfig.load(
            TraviscjConfig.class,
            "traviscj",
            deployment,
            ImmutableList.of(),
            ResourceLoader.Companion.getSYSTEM());

    DataSourceClusterConfig dataSourceClusterConfig =
        new DataSourceClusterConfig(config.data_source, config.data_source);
    JooqModule jooqModule =
        new JooqModule(
            kotlin.jvm.JvmClassMappingKt.getKotlinClass(TraviscjDb.class),
            dataSourceClusterConfig,
            "jooq",
            RealDatabasePool.INSTANCE,
            kotlin.jvm.JvmClassMappingKt.getKotlinClass(TraviscjReadyOnlyDb.class),
            new JooqTimestampRecordListenerOptions(true, "created_at", "updated_at"),
            true,
            true,
            (configuration -> Unit.INSTANCE));

    install(new MiskTestingServiceModule());
    install(new DeploymentModule(deployment));
    install(new ConfigModule<>(TraviscjConfig.class, "traviscj", config));
    install(jooqModule);
  }
}
