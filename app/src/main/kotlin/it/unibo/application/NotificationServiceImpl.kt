package it.unibo.application

import it.unibo.domain.AlertType
import it.unibo.domain.PriceAlert
import it.unibo.domain.PriceAlertRepository
import it.unibo.domain.PriceUpdateCurrency
import it.unibo.domain.port.EventDispatcher
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.slf4j.LoggerFactory

class NotificationServiceImpl(
    private val priceAlertRepository: PriceAlertRepository,
    private val eventDispatcher: EventDispatcher,
) : NotificationService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * When a price update is received, for each crypto price the service:
     *  - Retrieves all non-triggered alerts for the given crypto and currency.
     *  - Checks if the update meets the alert condition based on AlertType.
     *  - If so, it sends a notification via the event dispatcher and marks the alert as triggered.
     */
    override suspend fun handlePriceUpdate(priceUpdate: PriceUpdateCurrency) {
        logger.info("Handling price update for currency: ${priceUpdate.currency}")

        priceUpdate.priceUpdate.payload.forEach { cryptoPrice ->
            logger.info("Processing price update for crypto: ${cryptoPrice.id} with price: ${cryptoPrice.price}")

            // Get any pending alerts for this crypto and currency.
            val alerts = priceAlertRepository.getAlertsForCrypto(cryptoPrice.id, priceUpdate.currency)
            logger.info("Found ${alerts.size} alerts for crypto: ${cryptoPrice.id} and currency: ${priceUpdate.currency}")

            alerts.forEach { alert ->
                val shouldTrigger =
                    when (alert.alertType) {
                        AlertType.ABOVE -> cryptoPrice.price >= alert.alertPrice
                        AlertType.BELOW -> cryptoPrice.price <= alert.alertPrice
                    }

                if (!alert.triggered && shouldTrigger) {
                    logger.info("Triggering alert for user: ${alert.userId}, crypto: ${alert.cryptoId}, alert price: ${alert.alertPrice}, current price: ${cryptoPrice.price}")

                    // Build the JSON payload for the notification.
                    val notificationJson =
                        buildJsonObject {
                            put("userId", alert.userId)
                            put("cryptoId", alert.cryptoId)
                            put("alertPrice", alert.alertPrice)
                            put("currentPrice", cryptoPrice.price)
                            put("message", alert.message.message)
                            put("alertType", alert.alertType.toString())
                        }
                    // Notify the external event dispatcher.
                    eventDispatcher.notifyUser(notificationJson)
                    logger.info("Notification sent for userid: ${alert.userId}")

                    // Mark this alert as triggered.
                    priceAlertRepository.markAsTriggered(alert)
                    logger.info("Alert marked as triggered: ${alert.userId}")
                } else {
                    logger.info("Alert not triggered for user: ${alert.userId}, crypto: ${alert.cryptoId}, alert price: ${alert.alertPrice}, current price: ${cryptoPrice.price}")
                }
            }
        }

        logger.info("Completed handling price update for currency: ${priceUpdate.currency}")
    }

    /**
     * Create a new price alert by saving it to the repository.
     */
    override suspend fun createAlert(alert: PriceAlert) {
        priceAlertRepository.save(alert)
    }
}