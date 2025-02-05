package it.unibo.infrastructure.adapter

import com.mongodb.client.model.Filters
import it.unibo.domain.Currency
import it.unibo.domain.PriceAlert
import it.unibo.domain.PriceAlertRepository
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.setValue

class MongoPriceAlertRepository : PriceAlertRepository {
    // “price_alerts” is the Mongo collection name.
    private val database: CoroutineDatabase

    init {
        val mongoConnectionString = "mongodb://mongodb:27017/dbsa"
        val client = KMongo.createClient(mongoConnectionString)
        database = client.getDatabase("dbsa").coroutine
    }

    private val alertsCollection: CoroutineCollection<PriceAlert> = database.getCollection()

    override suspend fun save(alert: PriceAlert) {
        val result = alertsCollection.insertOne(alert)
        val insertedId =
            result.insertedId?.asObjectId()?.value
                ?: throw IllegalStateException("Failed to retrieve inserted ID")
        val filter =
            Filters.eq("_id", insertedId)

        alertsCollection.replaceOne(filter, alert.copy(id = insertedId.toString()))
    }

    override suspend fun getAlertsForCrypto(
        cryptoId: String,
        currency: Currency,
    ): List<PriceAlert> {
        return alertsCollection.find(
            and(
                PriceAlert::cryptoId eq cryptoId,
                PriceAlert::currency eq currency,
            ),
        ).toList()
    }

    override suspend fun markAsTriggered(alert: PriceAlert) {
        if (alert.id != null) {
            alertsCollection.updateOne(
                PriceAlert::id eq alert.id,
                setValue(PriceAlert::triggered, true),
            )
        }
    }

    override suspend fun getAlertsForUser(userId: String): List<PriceAlert> {
        return alertsCollection.find(PriceAlert::userId eq userId).toList()
    }

    override suspend fun deleteAlert(alertId: String) {
        alertsCollection.deleteOne(PriceAlert::id eq alertId)
    }
}
