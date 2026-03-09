package tech.airobotics.dd_receiver.mvi

import tech.airobotics.dd_receiver.model.HttpMessage

data class ServerState(
    val isRunning: Boolean = false,
    val messages: List<HttpMessage> = emptyList(),
    val error: String? = null
)

