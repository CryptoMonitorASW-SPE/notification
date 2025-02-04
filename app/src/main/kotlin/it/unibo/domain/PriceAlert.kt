package it.unibo.domain

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
enum class AlertType {
    ABOVE,
    BELOW
}

@Serializable
data class PriceAlert(
    @Contextual @SerialName("_id") val id: ObjectId? = null,
    val userId: String,
    val cryptoId: String,
    val alertPrice: Double,
    val currency: Currency,
    val message: Message,
    val alertType: AlertType,
    val triggered: Boolean = false,
)

