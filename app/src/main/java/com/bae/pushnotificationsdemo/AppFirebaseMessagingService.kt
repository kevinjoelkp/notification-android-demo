package com.bae.pushnotificationsdemo

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

object FirebaseDemoState {

    var status by mutableStateOf(
        "Comprobando configuración de Firebase…"
    )

    var token by mutableStateOf<String?>(null)
}

class AppFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "NotificationDemo"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(
            TAG,
            "onMessageReceived; origen=${message.from}"
        )

        Log.d(
            TAG,
            "notification payload: ${
                message.notification?.let {
                    "title=${it.title}, body=${it.body}"
                } ?: "ausente"
            }"
        )

        Log.d(
            TAG,
            "data payload: ${
                message.data.ifEmpty {
                    "ausente"
                }
            }"
        )

        val title =
            message.notification?.title
                ?: message.data["title"]

        val body =
            message.notification?.body
                ?: message.data["body"]

        if (!title.isNullOrBlank() && !body.isNullOrBlank()) {

            val movementId =
                message.data["movementId"]
                    ?.takeIf { it.isNotBlank() }
                    ?: NotificationHelper.DEMO_MOVEMENT_ID

            val published =
                NotificationHelper.publishTransferNotification(
                    context = this,
                    title = title,
                    body = body,
                    movementId = movementId
                )

            Log.d(
                TAG,
                "Notificación foreground publicada: " +
                        "$published; movementId=$movementId"
            )

        } else {
            Log.d(
                TAG,
                "Mensaje sin título/cuerpo suficientes; " +
                        "solo se registra en Logcat"
            )
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        Log.d(
            TAG,
            "onNewToken; FCM registration token actualizado: $token"
        )

        FirebaseDemoState.token = token

        FirebaseDemoState.status =
            "Firebase configurado; token disponible."
    }

}