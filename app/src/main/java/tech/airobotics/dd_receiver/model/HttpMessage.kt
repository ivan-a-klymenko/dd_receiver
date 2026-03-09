package tech.airobotics.dd_receiver.model

data class HttpMessage(
    val id: String,
    val timestamp: Long,
    val payload: Map<String, String>,
    val sourceIp: String? = null
)
