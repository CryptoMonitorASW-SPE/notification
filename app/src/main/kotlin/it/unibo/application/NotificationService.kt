package it.unibo.application

import it.unibo.domain.PriceAlert
import it.unibo.domain.PriceUpdateCurrency

interface NotificationService {
    suspend fun handlePriceUpdate(priceUpdate: PriceUpdateCurrency)

    suspend fun createAlert(alert: PriceAlert)

    suspend fun getAlerts(userId: String): List<PriceAlert>

    suspend fun deleteAlert(alertId: String)
}
