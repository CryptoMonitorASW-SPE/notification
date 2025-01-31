package it.unibo.application

import it.unibo.domain.CryptoRepository
import it.unibo.domain.PriceUpdate

class NotificationServiceImpl(private val cryptoRepository: CryptoRepository) : NotificationService {
    override fun savePriceUpdate(priceUpdate: PriceUpdate) {
        cryptoRepository.saveLatest(priceUpdate)
    }
}
