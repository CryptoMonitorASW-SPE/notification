package it.unibo.domain

import kotlinx.serialization.Serializable

@Serializable
enum class AlertType {
    ABOVE,
    BELOW,
}

@Serializable
data class PriceAlert(
    val id: String? = null,
    val userId: String,
    val cryptoId: String,
    val alertPrice: Double,
    val currency: Currency,
    val message: Message,
    val alertType: AlertType,
    val triggered: Boolean = false,
    val active: Boolean = true,
)
