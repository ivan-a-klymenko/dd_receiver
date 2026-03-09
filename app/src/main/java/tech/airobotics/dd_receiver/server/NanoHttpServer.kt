package tech.airobotics.dd_receiver.server

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import tech.airobotics.dd_receiver.model.HttpMessage
import tech.airobotics.dd_receiver.repository.MessageRepository

class NanoHttpServer(
    private val repo: MessageRepository,
    private val port: Int = 8080,
    private val bindAddress: String = "0.0.0.0",
    private val coroutineScope: CoroutineScope
) : NanoHTTPD(bindAddress, port) {

    private val TAG = "NanoHttpServer"

    override fun serve(session: IHTTPSession): Response {
        try {
            val uri = session.uri
            val method = session.method
            Log.d(TAG, "serve: method=$method uri=$uri remote=${session.remoteIpAddress}")
            if (method == Method.POST && uri == "/report") {
                val contentLength = session.headers["content-length"]?.toIntOrNull() ?: -1
                val input = session.inputStream
                val body = if (contentLength >= 0) {
                    val buf = ByteArray(contentLength)
                    var read = 0
                    while (read < contentLength) {
                        val r = input.read(buf, read, contentLength - read)
                        if (r <= 0) break
                        read += r
                    }
                    String(buf, Charsets.UTF_8)
                } else {
                    input.bufferedReader().use { it.readText() }
                }

                Log.d(TAG, "body: $body")
                try {
                    val je = Json.parseToJsonElement(body).jsonObject
                    val id = je["id"]?.jsonPrimitive?.contentOrNull ?: java.util.UUID.randomUUID()
                        .toString()
                    val timestamp = je["timestamp"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
                        ?: System.currentTimeMillis()
                    val payload = mutableMapOf<String, String>()
                    val payloadObj = je["payload"]?.jsonObject
                    payloadObj?.forEach { (k, v) ->
                        payload[k] = v.jsonPrimitive.contentOrNull ?: ""
                    }
                    val msg = HttpMessage(id = id, timestamp = timestamp, payload = payload)
                    val withIp = msg.copy(sourceIp = session.remoteIpAddress)
                    // save using provided scope
                    coroutineScope.launch {
                        try {
                            repo.save(withIp)
                        } catch (e: Exception) {
                            Log.e(TAG, "repo.save error", e)
                        }
                    }
                    return newFixedLengthResponse(
                        Response.Status.OK,
                        "application/json",
                        "{\"status\":\"ok\"}"
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "json parse error", e)
                    return newFixedLengthResponse(
                        Response.Status.BAD_REQUEST,
                        "text/plain",
                        "invalid json"
                    )
                }
            }

            if (method == Method.GET && uri == "/status") {
                return newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    "{\"status\":\"running\"}"
                )
            }

            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "not found")
        } catch (e: Exception) {
            Log.e(TAG, "serve exception", e)
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "error")
        }
    }
}
