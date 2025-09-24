import com.github.gradle.node.npm.task.NpmTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.named

val exposed_version: String by project
val koin_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val prometheus_version: String by project
val flyway_version: String by project

plugins {
    kotlin("jvm") version "2.2.20"
    id("io.ktor.plugin") version "3.3.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
    id("com.github.node-gradle.node") version "7.1.0"
}

group = "com.saban"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { url = uri("https://packages.confluent.io/maven/") }
}

dependencies {
    //ktor
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-sessions")
    implementation("io.ktor:ktor-server-host-common")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-openapi")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-metrics")
    implementation("io.ktor:ktor-server-metrics-micrometer")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheus_version")
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
    implementation("io.github.flaxoos:ktor-server-rate-limiting:2.1.2")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-caching-headers")

    //database
    implementation("org.postgresql:postgresql:42.7.5")
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.flywaydb:flyway-core:$flyway_version")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:$flyway_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")

    //other
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.github.config4k:config4k:0.7.0")
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation(awssdk.services.s3)
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

node {
    version = "20.17.0" // Set the Node.js version you want
    npmVersion = "10.8.2" // Optionally specify an npm version
    download = false // Automatically download Node.js
    nodeProjectDir = file("${projectDir}/saban-gui") // Set the working directory to the child directory
}

tasks {
    val buildingJar = gradle.startParameter.taskNames.any { it == "buildFatJar" || it == "shadowJar" }

    val cleanTask = register<Delete>("cleanFrontend") {
        delete("src/main/resources/static")
        onlyIf { buildingJar }
    }

    val buildTask by register<NpmTask>("npmBuild") {
        args = listOf("run", "generate")
        dependsOn("npmInstall") // Ensure dependencies are installed before building
        onlyIf { buildingJar }
    }

    val copyTask = register<Copy>("copyFrontend") {
        dependsOn(cleanTask, buildTask)
        from("gui/.output/public")
        into("src/main/resources/gui")
        onlyIf { buildingJar }
    }

    named("processResources") {
        dependsOn(copyTask)
    }

    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        dependsOn(copyTask)
    }
}
