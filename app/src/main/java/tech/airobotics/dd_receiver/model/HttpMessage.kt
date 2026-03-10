package tech.airobotics.dd_receiver.model

import kotlinx.serialization.json.JsonObject

data class HttpMessage(
    val id: String,
    val clientId: String? = null,
    val timestamp: Long,
    val payload: JsonObject,
    val sourceIp: String? = null
)