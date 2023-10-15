import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

val ktorVersion: String by project
val logbackVersion: String by project
val slackVersion: String by project
val flywayVersion: String by project
val postgresDriverVersion: String by project

val javaLanguageVersion = JavaLanguageVersion.of(17)
val mainAppClass = "io.github.gabrielshanahan.ApplicationKt"

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    id("io.ktor.plugin") version "2.3.5"
    id("org.jmailen.kotlinter") version "4.0.0"
    id("io.gitlab.arturbosch.detekt").version("1.23.1")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.flywaydb.flyway") version "9.22.3"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-gson-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")

//    implementation("org.jooq:jooq:3.18.7")
//    implementation("org.jooq:jooq-meta:3.18.7")
//    implementation("org.jooq:jooq-codegen:3.18.7")

    implementation("org.flywaydb:flyway-core:$flywayVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    implementation("com.slack.api:slack-api-client:$slackVersion")
    implementation("com.slack.api:slack-api-model-kotlin-extension:$slackVersion")
    implementation("com.slack.api:slack-api-client-kotlin-extension:$slackVersion")

    runtimeOnly("org.postgresql:postgresql:$postgresDriverVersion")

    testImplementation("io.ktor:ktor-server-tests-jvm")
}

java {
    toolchain {
        languageVersion.set(javaLanguageVersion)
    }
}

application {
    mainClass.set(mainAppClass)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    baseline = file("$rootDir/config/detekt/baseline.xml")
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = javaLanguageVersion.toString()
}
tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = javaLanguageVersion.toString()
}

tasks {
    shadowJar {
        manifest.attributes["Main-Class"] = mainAppClass
        archiveClassifier.set("")
    }
}

