import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.target.HostManager.Companion.host

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { setUrl("https://mvnrepository.com/repos/central") }
        maven { setUrl("https://repo1.maven.org/maven2/") }
        maven { setUrl("https://s01.oss.sonatype.org/service/local/repositories/comsquareup-1071/content") }
    }

    dependencies {
        classpath("org.flywaydb:flyway-mysql:11.3.3")
        classpath("com.mysql:mysql-connector-j:8.4.0")
    }
}

plugins {
    id("application")
    id("idea")
//    id("org.flywaydb.flyway") version "11.3.3"

    alias(libs.plugins.wire)
    alias(libs.plugins.kotlinJvm)
//    alias(libs.plugins.flyway)
    alias(libs.plugins.jooq)
    alias(libs.plugins.miskSchemaMigrator)
}

group = "io.tcj"
version = "1.0-SNAPSHOT"

repositories {
    gradlePluginPortal()
    mavenCentral()
//    maven { setUrl("https://repo1.maven.org/maven2/") }
//    maven { setUrl("https://mvnrepository.com/repos/central") }
    maven { setUrl("https://s01.oss.sonatype.org/service/local/repositories/comsquareup-1071/content") }
}

application {
    mainClass.set("io.tcj.TraviscjApp")
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/wire/")
        }
    }
}

wire {
    sourcePath {
        srcDir("src/main/proto/")
    }
    kotlin {
        javaInterop = true
        rpcCallStyle = "blocking"
        rpcRole = "server"
        singleMethodServices = true
    }
}

dependencies {
    implementation(libs.miskActions)
    implementation(libs.miskAdmin)
    implementation(libs.miskCore)
    implementation(libs.miskGrpcReflect)
    implementation(libs.miskInject)
    implementation(libs.miskJdbc)
    implementation(libs.miskJooq)
    implementation(libs.miskService)
//    implementation(libs.miskDashboard)
//    implementation(libs.miskWeb)
    implementation(libs.misk)
//    implementation(libs.wispConfig)
//    implementation(libs.wispDeployment)
//    implementation(libs.wispLogging)
//    implementation(libs.wispResourceLoader)
//    implementation(libs.wispContainersTesting)
    testImplementation(libs.miskTesting)

    implementation(libs.slf4jSimple)
    implementation(libs.kotlinLogging)

    implementation(libs.retrofitWire)
    implementation(libs.retrofitMoshi)
    implementation(libs.wireReflector)
    implementation(libs.wireMoshiAdapter)
    implementation(libs.retrofitWire)
    implementation(libs.protobufJava)

    implementation(libs.wireRuntime)
//    implementation(libs.flyway)
//    implementation(libs.flywayMysql)
    implementation(libs.dockerCore)
    implementation(libs.mysql)
//    jooqGenerator("org.postgresql:postgresql")
    jooqGenerator(libs.mysql)

    testImplementation(libs.junitApi)
    testRuntimeOnly(libs.junitEngine)
    testImplementation(libs.assertj)



    implementation(group = "io.netty", name = "netty-transport-native-kqueue")
//    implementation("com.github.docker-java:docker-java-transport-okhttp:3.2.13")
    implementation(libs.dockerTransportHttpClient)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test> {
    useJUnitPlatform()
}

//tasks.withType<ShadowJar> {
//    zip64 = true
//    mergeServiceFiles()
//    archiveBaseName.set("traviscj-shadowed")
//    archiveClassifier.set("")
//    archiveVersion.set("")
//}

val dbConfig = mapOf(
    "url" to "jdbc:mysql://localhost:3306/",
    "schema" to "codegen",
    "user" to "root",
    "password" to ""
)

miskSchemaMigrator {
    database = dbConfig["schema"]
//    host = "localhost" // optional, defaults to localhost
//    port = 3306 // optional, defaults to 3306
    username = dbConfig["user"]
    password = dbConfig["password"]
    migrationsDir = layout.projectDirectory.dir("src/main/resources/db/migration")
    migrationsFormat = "TRADITIONAL"
}

//flyway {
////    url = "jdbc:postgresql://localhost:5432/traviscj_localdev"
//    url = "jdbc:mysql://localhost:3306/traviscj_localdev"
//    user = "root"
//    password = ""
//    schemas = arrayOf("public")
//    locations = arrayOf("filesystem:src/main/resources/db/migration")
//}

//tasks.register<org.flywaydb.gradle.task.FlywayMigrateTask>("migrateCodegen") {
////    url = "jdbc:postgresql://localhost:5432/traviscj_codegen"
//    url = "jdbc:mysql://localhost:3306/misk_jooq_test_codegen"
//
//    user = "root"
//    password = ""
//    schemas = arrayOf("public")
//    locations = arrayOf("filesystem:src/main/resources/db/migration")
//}

jooq {
//    version.set("3.19.18")
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true)
            jooqConfiguration.apply {
//            generationTool {
                jdbc.apply {
//                    driver = "org.postgresql.Driver"
//                    url = "jdbc:postgresql://localhost:5432/traviscj_codegen"
//                    user = "root"
//                    password = ""
                    driver = "com.mysql.cj.jdbc.Driver"
                    url = "jdbc:mysql://localhost:3306/misk_jooq_test_codegen"
                    user = "root"
                    password = ""

                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        name = "org.jooq.meta.mysql.MySQLDatabase"
                        inputSchema = "public"
//                        outputSchema = "tcj_kv"
                        isOutputSchemaToDefault = true
                        includes = ".*"
                        excludes = "(.*?FLYWAY_SCHEMA_HISTORY)|(.*?schema_version)"
                        recordVersionFields = "version"
                    }
//                    generate.apply {
//                        deprecated = false
//                        records = true
//                        immutablePojos = true
//                        fluentSetters = true
//                    }
                    target.apply {
                        packageName = "io.tcj.jooq"
                        directory = "${project.projectDir}/jooq/src/main/java"
                    }
                }
            }
        }
    }
}

//tasks.named("generateJooq").configure {
tasks.withType<nu.studer.gradle.jooq.JooqGenerate>().configureEach {
    dependsOn(tasks.named("migrateSchema"))
    inputs.files(fileTree("src/main/resources/db/migration"))
        .withPropertyName("migrations")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    // interesting, this is opposite of https://github.com/cashapp/misk/blob/master/misk-jooq/build.gradle.kts
    allInputsDeclared.set(true)
}
