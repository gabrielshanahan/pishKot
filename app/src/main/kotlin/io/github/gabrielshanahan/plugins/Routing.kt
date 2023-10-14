package io.github.gabrielshanahan.plugins

import com.slack.api.Slack
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.future.await

fun Application.configureRouting() {
    routing {
        get("/") {
            val token = System.getenv("SLACK_TOKEN")
            val slack = Slack.getInstance()
            val methodsAsync = slack.methodsAsync(token)

            val conversationListResponse = methodsAsync.conversationsList { it }.await()

            val sandboxChannel = conversationListResponse.channels.find { it.name == "sandbox" }!!

            val response1 = methodsAsync.conversationsJoin {
                it.channel(sandboxChannel.id)
            }.await()

            val response = methodsAsync.chatPostMessage {
                it.channel(sandboxChannel.id)
                    .text("Hello Kotlin! :wave:")
            }.await()

            call.respondText("Responses are: ${listOf(response1, response)}")
        }
    }
}
