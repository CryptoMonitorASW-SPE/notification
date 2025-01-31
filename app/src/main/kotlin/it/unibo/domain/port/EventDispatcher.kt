package it.unibo.domain.port

import kotlinx.serialization.json.JsonElement

interface EventDispatcher {
    fun notifyUser(data: JsonElement)
}