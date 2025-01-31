package it.unibo

import it.unibo.application.NotificationServiceImpl
import it.unibo.domain.CryptoRepository
import it.unibo.infrastructure.adapter.InMemoryCryptoRepository
import it.unibo.infrastructure.adapter.WebServer
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

fun main() {
    val logger = LoggerFactory.getLogger("CoinGeckoApp")

    val repository: CryptoRepository = InMemoryCryptoRepository()
    val notificationService = NotificationServiceImpl(repository)

    val supervisor = SupervisorJob()
    val webServer = WebServer(notificationService).apply { start() }

    // Shutdown hook
    Runtime.getRuntime().addShutdownHook(
        Thread {
            runBlocking {
                logger.info("Shutting down...")

                webServer.stop()
                supervisor.cancelAndJoin()
                logger.info("Shutdown complete")
            }
        },
    )

    runBlocking { supervisor.join() }
}
