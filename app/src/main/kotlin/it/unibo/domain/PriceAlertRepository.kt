package it.unibo.domain

interface PriceAlertRepository {
    suspend fun save(alert: PriceAlert)

    suspend fun getAlertsForCrypto(
        cryptoId: String,
        currency: Currency,
    ): List<PriceAlert>

    suspend fun markAsTriggered(alert: PriceAlert)

    suspend fun getAlertsForUser(userId: String): List<PriceAlert>

    suspend fun deleteAlert(alertId: String)
}
