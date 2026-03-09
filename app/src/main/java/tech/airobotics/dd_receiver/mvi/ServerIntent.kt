package tech.airobotics.dd_receiver.mvi

import tech.airobotics.dd_receiver.model.HttpMessage

sealed class ServerIntent {
    object StartServer : ServerIntent()
    object StopServer : ServerIntent()
    data class ReceivedMessage(val message: HttpMessage) : ServerIntent()
    object ClearMessages : ServerIntent()
}

