package tech.airobotics.dd_receiver.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object ServerStoreProvider {
    val store: ServerStore by lazy { ServerStore(CoroutineScope(Dispatchers.Default)) }
}

