package tech.airobotics.dd_receiver.mvi

object ServerReducer {
    fun reduce(state: ServerState, intent: ServerIntent): ServerState = when (intent) {
        is ServerIntent.StartServer -> state.copy(isRunning = true, error = null)
        is ServerIntent.StopServer -> state.copy(isRunning = false)
        is ServerIntent.ReceivedMessage -> state.copy(messages = listOf(intent.message) + state.messages)
        is ServerIntent.ClearMessages -> state.copy(messages = emptyList())
    }
}

