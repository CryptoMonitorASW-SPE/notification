@file:Suppress("ktlint:standard:no-wildcard-imports")

package it.unibo.infrastructure.adapter

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import it.unibo.application.NotificationService
import it.unibo.domain.PriceUpdate

class WebServer(private val notificationService: NotificationService) {
    companion object {
        const val PORT = 8080
        const val GRACE_PERIOD = 1000L
        const val TIMEOUT = 5000L
    }

    private val logger = org.slf4j.LoggerFactory.getLogger("WebServer")

    private val server =
        embeddedServer(Netty, port = PORT) {
            install(ContentNegotiation) { json() }
            routing {
                post("/data") {
                    val currencyParam = call.parameters["currency"]
                    if (currencyParam == null) {
                        call.respond(HttpStatusCode.BadRequest, "Missing required parameter: currency")
                        return@post
                    }

                    val priceUpdate = call.receive<PriceUpdate>()
                    notificationService.savePriceUpdate(priceUpdate)
                    call.respond(HttpStatusCode.OK, "Data received")
                }

                post("/createAlert") {
                    val userId = call.parameters["userId"]
                    val cryptoId = call.parameters["cryptoId"]
                    val price = call.parameters["price"]
                    val currencyParam = call.parameters["currency"]

                    if (userId == null || currencyParam == null || cryptoId == null || price == null) {
                        call.respond(HttpStatusCode.BadRequest, "Missing required parameters: userId and currency")
                        return@post
                    }
                    call.respond(HttpStatusCode.OK, "Alert created")
                }

                get("/health") {
                    call.respond(mapOf("status" to "healthy"))
                }
            }
        }

    fun start() {
        server.start(wait = true)
    }

    fun stop() {
        server.stop(GRACE_PERIOD, TIMEOUT)
    }
}
