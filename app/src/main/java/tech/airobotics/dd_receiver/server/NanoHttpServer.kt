package tech.airobotics.dd_receiver.server

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.json.JSONObject
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

                    val id = je["id"]?.jsonPrimitive?.contentOrNull
                        ?: java.util.UUID.randomUUID().toString()

                    val clientId = je["clientId"]?.jsonPrimitive?.contentOrNull

                    val timestamp = je["timestamp"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
                        ?: System.currentTimeMillis()

                    val payload = je["payload"]?.jsonObject ?: buildJsonObject { }

                    val msg = HttpMessage(
                        id = id,
                        clientId = clientId,
                        timestamp = timestamp,
                        payload = payload
                    )

                    val withIp = msg.copy(sourceIp = session.remoteIpAddress)

                    try {
                        runBlocking {
                            repo.save(withIp)
                        }

                        val ack = JSONObject()
                        ack.put("status", "ok")
                        ack.put("id", id)

                        return newFixedLengthResponse(
                            Response.Status.OK,
                            "application/json",
                            ack.toString()
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "repo.save error", e)

                        val ack = JSONObject()
                        ack.put("status", "error")
                        ack.put("id", id)

                        return newFixedLengthResponse(
                            Response.Status.INTERNAL_ERROR,
                            "application/json",
                            ack.toString()
                        )
                    }
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

            return newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                "text/plain",
                "not found"
            )
        } catch (e: Exception) {
            Log.e(TAG, "serve exception", e)
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "text/plain",
                "error"
            )
        }
    }
}