import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Logging

val ktorVersion: String by project
val logbackVersion: String by project
val slackVersion: String by project
val flywayVersion: String by project
val postgresDriverVersion: String by project

val javaLanguageVersion = JavaLanguageVersion.of(17)
val mainAppClass = "io.github.gabrielshanahan.pishkot.ApplicationKt"

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    id("io.ktor.plugin") version "2.3.5"
    id("org.jmailen.kotlinter") version "4.0.0"
    id("io.gitlab.arturbosch.detekt").version("1.23.1")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.flywaydb.flyway") version "9.22.3"
    id("nu.studer.jooq") version "8.2.1"
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

    implementation("org.jooq:jooq:3.18.7")
    implementation("org.jooq:jooq-meta:3.18.7")
    implementation("org.jooq:jooq-codegen:3.18.7")

    implementation("org.flywaydb:flyway-core:$flywayVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    implementation("com.slack.api:slack-api-client:$slackVersion")
    implementation("com.slack.api:slack-api-model-kotlin-extension:$slackVersion")
    implementation("com.slack.api:slack-api-client-kotlin-extension:$slackVersion")

    "org.postgresql:postgresql:$postgresDriverVersion"
        .also(::runtimeOnly)
        .also(::jooqGenerator)

    testImplementation("io.ktor:ktor-server-tests-jvm")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
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

java {
    toolchain {
        languageVersion.set(javaLanguageVersion)
    }
}

application {
    mainClass.set(mainAppClass)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    baseline = file("$rootDir/config/detekt/baseline.xml")
}

val dotEnvs = file("../.env").readLines()
    .map { it.split("=") }
    .groupBy(
        { it.first() },
        { it.last() }
    )

jooq {
    configurations {
        create("main") {  // name of the jOOQ configuration
            generateSchemaSourceOnCompilation.set(true)  // default (can be omitted)
            jooqConfiguration.apply {
                logging = Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/${dotEnvs.getValue("POSTGRES_DB").single()}"
                    user = dotEnvs.getValue("POSTGRES_USER").single()
                    password = dotEnvs.getValue("POSTGRES_PASSWORD").single()
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        forcedTypes.addAll(listOf(
                            ForcedType().apply {
                                name = "varchar"
                                includeExpression = ".*"
                                includeTypes = "JSONB?"
                            },
                            ForcedType().apply {
                                name = "varchar"
                                includeExpression = ".*"
                                includeTypes = "INET"
                            }
                        ))
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "io.github.gabrielshanahan.pishkot"
                        directory = "build/generated-src/jooq/main"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}