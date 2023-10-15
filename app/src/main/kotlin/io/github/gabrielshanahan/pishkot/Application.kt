package io.github.gabrielshanahan.pishkot

import io.github.gabrielshanahan.pishkot.plugins.configureRouting
import io.github.gabrielshanahan.pishkot.plugins.configureSerialization
import io.github.gabrielshanahan.pishkot.tables.Posts
import io.github.gabrielshanahan.pishkot.tables.Posts.POSTS
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.flywaydb.core.Flyway
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import java.sql.DriverManager

val logger = LoggerFactory.getLogger("ApplicationKt")

fun main() {

    val dbUrl = System.getenv("DATABASE_URL")
    val dbUser = System.getenv("POSTGRES_USER")
    val dbPassword = System.getenv("POSTGRES_PASSWORD")

    val flyway: Flyway = Flyway.configure().dataSource(
        dbUrl,
        dbUser,
        dbPassword
    ).load()

    flyway.migrate()

    try {
        DriverManager.getConnection(dbUrl, dbUser, dbPassword).use { conn ->
            val create = DSL.using(conn, SQLDialect.POSTGRES)
            val result = create.select().from(POSTS).fetch()

            result.forEach {
                logger.info("Name is ${it.getValue(POSTS.NAME)}")
            }
        }
    }
    catch (e: Exception) {
        logger.error("An error occurred", e)
        throw e
    }

    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureRouting()
}
