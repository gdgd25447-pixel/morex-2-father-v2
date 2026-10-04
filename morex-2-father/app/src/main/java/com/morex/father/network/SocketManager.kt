package com.morex.father.network

import android.content.Context
import android.util.Log
import com.morex.father.data.Prefs
import com.morex.father.utils.Constants
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import java.net.URI

/** حدث لحظي من السيرفر: الاسم + الحمولة (JSON). */
data class SocketEvent(val name: String, val data: JSONObject?)

/**
 * اتصال Socket.io للأب. السيرفر يوثّق الأب من JWT في handshake.auth.token ويضمّه تلقائياً لغرفته (parent:<id>)،
 * فلا حاجة لإرسال حدث انضمام.
 *
 * الأحداث التي يرسلها السيرفر للأب فعلياً:
 *  alert:new, location:update, device:online, device:offline, command:ack,
 *  subscription:activated, subscription:expired, support:new-message, broadcast
 * (SOS يصل كـ alert:new بنوع SOS.)
 *
 * الشاشات تشترك عبر [events] وتفلتر بالاسم.
 */
object SocketManager {

    private const val TAG = "SocketManager"

    object Events {
        const val ALERT_NEW = "alert:new"
        const val LOCATION_UPDATE = "location:update"
        const val DEVICE_ONLINE = "device:online"
        const val DEVICE_OFFLINE = "device:offline"
        const val COMMAND_ACK = "command:ack"
        const val SUBSCRIPTION_ACTIVATED = "subscription:activated"
        const val SUBSCRIPTION_EXPIRED = "subscription:expired"
        const val SUPPORT_NEW_MESSAGE = "support:new-message"
        const val BROADCAST = "broadcast"

        val ALL = listOf(
            ALERT_NEW, LOCATION_UPDATE, DEVICE_ONLINE, DEVICE_OFFLINE, COMMAND_ACK,
            SUBSCRIPTION_ACTIVATED, SUBSCRIPTION_EXPIRED, SUPPORT_NEW_MESSAGE, BROADCAST
        )
    }

    private val _events = MutableSharedFlow<SocketEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<SocketEvent> = _events.asSharedFlow()

    private var socket: Socket? = null
    private var isConnecting = false

    @Synchronized
    fun connect(context: Context) {
        if (isConnecting || socket?.connected() == true) return
        isConnecting = true

        try {
            val token = Prefs.getToken(context)
            if (token.isNullOrBlank()) {
                Log.w(TAG, "no token, skipping socket connect")
                isConnecting = false
                return
            }

            val options = IO.Options().apply {
                reconnection = true
                reconnectionAttempts = Int.MAX_VALUE
                reconnectionDelay = 2000
                timeout = 20000
                forceNew = true
                transports = arrayOf("websocket", "polling")
                auth = mapOf("token" to token) // لا يُسجَّل ولا يُرسل في الـ query
            }

            socket = IO.socket(URI.create(Constants.SOCKET_URL), options).apply {
                on(Socket.EVENT_CONNECT) { Log.d(TAG, "socket connected") }
                on(Socket.EVENT_DISCONNECT) { args -> Log.d(TAG, "socket disconnected: ${args.firstOrNull()}") }
                on(Socket.EVENT_CONNECT_ERROR) { args ->
                    // لا نطبع الحمولة كاملة: قد تحوي تفاصيل المصادقة
                    Log.e(TAG, "socket error: ${args.firstOrNull()?.javaClass?.simpleName}")
                }
                Events.ALL.forEach { name ->
                    on(name) { args ->
                        _events.tryEmit(SocketEvent(name, args.firstOrNull() as? JSONObject))
                    }
                }
            }
            socket?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "socket connect error: ${e.javaClass.simpleName}")
        } finally {
            isConnecting = false
        }
    }

    @Synchronized
    fun disconnect() {
        try {
            socket?.disconnect()
            socket?.off()
            socket = null
        } catch (e: Exception) {
            Log.e(TAG, "disconnect error: ${e.javaClass.simpleName}")
        }
    }

    fun isConnected(): Boolean = socket?.connected() == true
}
