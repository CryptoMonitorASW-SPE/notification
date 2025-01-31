package it.unibo.domain

interface CryptoRepository {
    fun saveLatest(priceUpdate: PriceUpdate)
    fun getLatestPrices(): PriceUpdate?
    fun getLastPriceForId(id: String): CryptoPrice?
}