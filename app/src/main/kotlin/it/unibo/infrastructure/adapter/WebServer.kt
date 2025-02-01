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
import it.unibo.domain.Currency
import it.unibo.domain.Message
import it.unibo.domain.PriceAlert
import it.unibo.domain.PriceUpdate
import it.unibo.domain.PriceUpdateCurrency
import kotlinx.serialization.SerializationException

class WebServer(private val notificationService: NotificationService) {
    companion object {
        const val PORT = 8080
        const val GRACE_PERIOD = 1000L
        const val TIMEOUT = 5000L
    }

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
                    val priceUpdateCurrency = PriceUpdateCurrency(Currency.fromCode(currencyParam), priceUpdate)

                    notificationService.handlePriceUpdate(priceUpdateCurrency)
                    call.respond(HttpStatusCode.OK, "Data received")
                }

                post("/createAlert") {
                    val userId = call.authenticate(System.getenv("JWT_SECRET")) ?: return@post

                    val cryptoId = call.parameters["cryptoId"]
                    val priceParam = call.parameters["price"]
                    val currencyParam = call.parameters["currency"]

                    if (currencyParam == null) {
                        call.respond(HttpStatusCode.BadRequest, "Missing required parameter: currency")
                        return@post
                    }
                    if (cryptoId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Missing required parameter: cryptoId")
                        return@post
                    }
                    if (priceParam == null) {
                        call.respond(HttpStatusCode.BadRequest, "Missing required parameter: price")
                        return@post
                    }

                    val message: Message =
                        try {
                            call.receive<Message>()
                        } catch (e: SerializationException) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid message format: ${e.message}")
                            return@post
                        } catch (e: NumberFormatException) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid number format: ${e.message}")
                            return@post
                        }

                    val currency = Currency.fromCode(currencyParam)
                    val price = priceParam.toDouble()

                    val alert = PriceAlert(userId, cryptoId, price, currency, message)
                    notificationService.createAlert(alert)
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
