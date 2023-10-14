import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

val ktorVersion: String by project
val logbackVersion: String by project
val slackVersion: String by project

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    id("org.jmailen.kotlinter") version "4.0.0"
    id("io.gitlab.arturbosch.detekt").version("1.23.1")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    implementation("com.slack.api:slack-api-client:$slackVersion")
    implementation("com.slack.api:slack-api-model-kotlin-extension:$slackVersion")
    implementation("com.slack.api:slack-api-client-kotlin-extension:$slackVersion")

    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(20))
    }
}

application {
    mainClass.set("io.github.gabrielshanahan.AppKt")
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
    jvmTarget = JavaLanguageVersion.of(20).toString()
}
tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = JavaLanguageVersion.of(20).toString()
}
