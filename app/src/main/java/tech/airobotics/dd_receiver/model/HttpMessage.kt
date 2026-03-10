package tech.airobotics.dd_receiver.model

data class HttpMessage(
    val id: String,                       // уникальный ID сообщения
    val clientId: String? = null,        // постоянный ID клиента
    val timestamp: Long,
    val payload: Map<String, String>,
    val sourceIp: String? = null
)