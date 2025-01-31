package it.unibo.infrastructure.adapter

import it.unibo.domain.CryptoPrice
import it.unibo.domain.CryptoRepository
import it.unibo.domain.PriceUpdate

class InMemoryCryptoRepository : CryptoRepository {
    private var latestPriceUpdate: PriceUpdate? = null
    private val lock = Any()

    override fun saveLatest(priceUpdate: PriceUpdate) {
        synchronized(lock) {
            val current = latestPriceUpdate
            if (current == null || priceUpdate.timestamp > current.timestamp) {
                latestPriceUpdate = priceUpdate
            }
        }
    }

    override fun getLatestPrices(): PriceUpdate? {
        synchronized(lock) {
            return latestPriceUpdate
        }
    }

    override fun getLastPriceForId(id: String): CryptoPrice? {
        synchronized(lock) {
            return latestPriceUpdate?.payload?.find { it.id == id }
        }
    }
}
