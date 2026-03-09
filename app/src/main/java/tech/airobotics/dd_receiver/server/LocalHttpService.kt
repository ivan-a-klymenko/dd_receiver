package tech.airobotics.dd_receiver.server

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tech.airobotics.dd_receiver.mvi.ServerIntent
import tech.airobotics.dd_receiver.mvi.ServerStoreProvider
import tech.airobotics.dd_receiver.repository.InMemoryMessageRepository

class LocalHttpService : Service() {

    private val TAG = "LocalHttpService"
    private var server: NanoHttpServer? = null
    private val repo = InMemoryMessageRepository()
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val seenIds = mutableSetOf<String>()

    companion object {
        const val CHANNEL_ID = "local_http_server_channel"
        const val NOTIF_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        try {
            createNotificationChannel()
        } catch (e: Exception) {
            Log.e(TAG, "createNotificationChannel failed", e)
        }

        // subscribe to repository changes
        serviceScope.launch {
            try {
                repo.observeAll().collect { list ->
                    for (msg in list) {
                        if (seenIds.add(msg.id)) {
                            ServerStoreProvider.store.dispatch(ServerIntent.ReceivedMessage(msg))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "repo.observeAll collect failed", e)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d(TAG, "onStartCommand action=$action")
        try {
            when (action) {
                ACTION_START -> startServer()
                ACTION_STOP -> stopServer()
                else -> {
                    // default: start
                    startServer()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onStartCommand failed", e)
            // попытка уведомить пользователя и остановить сервис
            try {
                Toast.makeText(this, "Server failed to start: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            } catch (_: Exception) {
            }
            stopSelf()
        }
        return START_STICKY
    }

    private fun startServer() {
        if (server != null) return

        // build notification
        val notif = try {
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Local HTTP Server")
                .setContentText("Server starting...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "build notification failed", e)
            null
        }

        try {
            if (notif != null) startForeground(NOTIF_ID, notif)
        } catch (e: Exception) {
            Log.e(TAG, "startForeground failed", e)
            // не даём падать — показываем тост и останавливаемся
            try {
                Toast.makeText(this, "startForeground failed: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            } catch (_: Exception) {
            }
            stopSelf()
            return
        }

        serviceScope.launch {
            try {
                server = NanoHttpServer(repo, 8080, "127.0.0.1", serviceScope)
                server?.start()
                Log.i(TAG, "server started on 127.0.0.1:8080")
            } catch (e: Exception) {
                Log.e(TAG, "server start error", e)
                // если старт сервера упал — убираем foreground и останавливаем сервис
                try {
                    stopForeground(true)
                } catch (_: Exception) {
                }
                try {
                    stopSelf()
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun stopServer() {
        serviceScope.launch {
            try {
                server?.stop()
                server = null
                try {
                    stopForeground(true)
                } catch (_: Exception) {
                }
                stopSelf()
                Log.i(TAG, "server stopped")
            } catch (e: Exception) {
                Log.e(TAG, "server stop error", e)
            }
        }
    }

    override fun onDestroy() {
        try {
            server?.stop()
        } catch (_: Exception) {
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
            if (nm == null) {
                Log.w(TAG, "NotificationManager is null")
                return
            }
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Local HTTP Server",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(channel)
        }
    }

}

const val ACTION_START = "tech.airobotics.dd_receiver.ACTION_START_SERVER"
const val ACTION_STOP = "tech.airobotics.dd_receiver.ACTION_STOP_SERVER"
