package it.unibo.domain.ports

import kotlinx.serialization.json.JsonElement

interface EventDispatcher {
    fun notifyUser(data: JsonElement)
}
