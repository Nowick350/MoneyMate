package com.example.moneymate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * MoneyMate notifications.
 *
 * 1. LOCAL notifications - budget alerts that the app posts for itself
 *    (MmRepo calls MmPush.notifyNow when a transaction pushes a budget
 *    past 80% or over the limit).
 *
 * 2. REMOTE push - MmPushService (registered in AndroidManifest.xml)
 *    receives Firebase Cloud Messaging (FCM) messages. During the demo
 *    video you send one from the Firebase console to this phone's FCM
 *    token and the notification appears in real time.
 *
 * The phone's FCM token is shown in Settings so the console demo is
 * possible without any server.
 */
object MmPush {
    const val CHANNEL_ID = "moneymate_alerts"
    private const val NOTIFICATION_ID = 1

    /** Creates the notification channel once (required from Android 8 / API 26 up). */
    fun ensureChannel(context: Context) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "MoneyMate alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Budget alerts and push messages" }
        manager.createNotificationChannel(channel)
    }

    /** Posts one notification right now. Never throws. */
    fun notifyNow(context: Context, title: String, body: String) {
        try {
            ensureChannel(context)
            val intent = Intent(context, MainActivity::class.java)
            val pending = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pending)
                .build()
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: Exception) {
            // Notifications must never crash the app.
        }
    }

    /**
     * The phone's FCM registration token. Empty string if it is not ready
     * yet (e.g. no internet at that moment) - the Settings card just
     * retries when it is opened again.
     */
    suspend fun fcmToken(): String = suspendCancellableCoroutine { cont ->
        try {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) cont.resume(task.result ?: "")
                    else cont.resume("")
                }
        } catch (_: Exception) {
            cont.resume("")
        }
    }
}

/**
 * The FCM listener. Firebase starts this service whenever a push message
 * addressed to this phone's token arrives.
 */
class MmPushService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: "MoneyMate"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: L.t("You have a new message.")
        MmPush.notifyNow(applicationContext, title, body)
    }
}
