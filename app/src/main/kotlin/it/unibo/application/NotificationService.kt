package it.unibo.application

import it.unibo.domain.PriceUpdate

interface NotificationService {
    fun savePriceUpdate(priceUpdate: PriceUpdate)
}