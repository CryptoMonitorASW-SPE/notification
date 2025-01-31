package it.unibo.domain

data class PriceAlert(
    val userId: String,
    val cryptoId: String,
    val price: Double,
    val currency: Currency,
    val message: Message
)