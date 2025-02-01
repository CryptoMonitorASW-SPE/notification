package it.unibo.application

import it.unibo.domain.PriceAlert
import it.unibo.domain.PriceAlertRepository
import it.unibo.domain.PriceUpdateCurrency
import it.unibo.domain.port.EventDispatcher
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class NotificationServiceImpl(
    private val priceAlertRepository: PriceAlertRepository,
    private val eventDispatcher: EventDispatcher,
) : NotificationService {
    /**
     * When a price update is received, for each crypto price the service:
     *  - Retrieves all non-triggered alerts for the given crypto and currency.
     *  - Checks if the update meets the alert condition (here: current price >= alert price).
     *  - If so, it sends a notification via the event dispatcher and marks the alert as triggered.
     */
    override suspend fun handlePriceUpdate(priceUpdate: PriceUpdateCurrency) {
        priceUpdate.priceUpdate.payload.forEach { cryptoPrice ->
            // Get any pending alerts for this crypto and currency.
            val alerts = priceAlertRepository.getAlertsForCrypto(cryptoPrice.id, priceUpdate.currency)
            alerts.forEach { alert ->
                if (!alert.triggered && cryptoPrice.price >= alert.alertPrice) {
                    // Build the JSON payload for the notification.
                    val notificationJson =
                        buildJsonObject {
                            put("userId", alert.userId)
                            put("cryptoId", alert.cryptoId)
                            put("alertPrice", alert.alertPrice)
                            put("currentPrice", cryptoPrice.price)
                            put("message", "Alert triggered for crypto: ${alert.cryptoId}")
                        }
                    // Notify the external event dispatcher.
                    eventDispatcher.notifyUser(notificationJson)
                    // Mark this alert as triggered.
                    priceAlertRepository.markAsTriggered(alert)
                }
            }
        }
    }

    /**
     * Create a new price alert by saving it to the repository.
     */
    override suspend fun createAlert(alert: PriceAlert) {
        priceAlertRepository.save(alert)
    }
}
