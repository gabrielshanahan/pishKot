package io.github.gabrielshanahan

import io.github.gabrielshanahan.plugins.configureRouting
import io.github.gabrielshanahan.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.flywaydb.core.Flyway

fun main() {
    val flyway: Flyway = Flyway.configure().dataSource(
        System.getenv("DATABASE_URL"),
        System.getenv("POSTGRES_USER"),
        System.getenv("POSTGRES_PASSWORD")
    ).load()

    flyway.migrate()
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureRouting()
}
