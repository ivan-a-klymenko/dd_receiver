package tech.airobotics.dd_receiver.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServerStore(scope: CoroutineScope? = null) {
    private val internalScope = scope ?: CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _state = MutableStateFlow(ServerState())
    val states: StateFlow<ServerState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ServerEffect>()
    val effects: SharedFlow<ServerEffect> = _effects.asSharedFlow()

    fun dispatch(intent: ServerIntent) {
        internalScope.launch {
            when (intent) {
                is ServerIntent.StartServer -> {
                    _state.value = ServerReducer.reduce(_state.value, intent)
                    _effects.emit(ServerEffect.NotificationUpdate("Server started"))
                }

                is ServerIntent.StopServer -> {
                    _state.value = ServerReducer.reduce(_state.value, intent)
                    _effects.emit(ServerEffect.NotificationUpdate("Server stopped"))
                }

                is ServerIntent.ReceivedMessage -> {
                    _state.value = ServerReducer.reduce(_state.value, intent)
                }

                is ServerIntent.ClearMessages -> {
                    _state.value = ServerReducer.reduce(_state.value, intent)
                }
            }
        }
    }
}

