package it.unibo.infrastructure.adapter

import it.unibo.domain.Currency
import it.unibo.domain.PriceAlert
import it.unibo.domain.PriceAlertRepository
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo

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
        alertsCollection.insertOne(alert)
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
    }
}
