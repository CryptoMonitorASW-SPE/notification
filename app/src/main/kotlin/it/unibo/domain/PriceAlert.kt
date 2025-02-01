package it.unibo.domain

import kotlinx.serialization.Serializable

@Serializable
data class PriceAlert(
    val userId: String,
    val cryptoId: String,
    val alertPrice: Double,
    val currency: Currency,
    val message: Message,
    // A flag that tells whether the alert has already been triggered
    val triggered: Boolean = false,
)
