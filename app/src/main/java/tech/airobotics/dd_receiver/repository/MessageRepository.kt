package tech.airobotics.dd_receiver.repository

import kotlinx.coroutines.flow.Flow
import tech.airobotics.dd_receiver.model.HttpMessage

interface MessageRepository {
    suspend fun save(message: HttpMessage)
    fun observeAll(): Flow<List<HttpMessage>>
    suspend fun clear()
}

class InMemoryMessageRepository : MessageRepository {
    private val data = mutableListOf<HttpMessage>()
    private val flow = kotlinx.coroutines.flow.MutableStateFlow<List<HttpMessage>>(emptyList())

    override suspend fun save(message: HttpMessage) {
        synchronized(this) {
            data.add(0, message)
            flow.value = data.toList()
        }
    }

    override fun observeAll(): Flow<List<HttpMessage>> = flow

    override suspend fun clear() {
        synchronized(this) {
            data.clear()
            flow.value = emptyList()
        }
    }
}

