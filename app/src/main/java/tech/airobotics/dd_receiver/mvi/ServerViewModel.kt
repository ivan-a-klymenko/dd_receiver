package tech.airobotics.dd_receiver.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ServerViewModel(private val store: ServerStore = ServerStoreProvider.store) : ViewModel() {
    val uiState: StateFlow<ServerState> = store.states

    fun dispatch(intent: ServerIntent) {
        viewModelScope.launch {
            store.dispatch(intent)
        }
    }
}
