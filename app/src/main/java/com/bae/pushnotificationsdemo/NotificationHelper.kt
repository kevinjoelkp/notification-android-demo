package com.bae.pushnotificationsdemo

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationHelper {

    // NotificationChannel
    const val TRANSFERS_CHANNEL_ID = "transfers"

    // notificationId
    const val TRANSFER_NOTIFICATION_ID = 78421

    // movementId local / FCM
    const val DEMO_MOVEMENT_ID = "78421"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // NotificationChannel
            val channel = NotificationChannel(
                TRANSFERS_CHANNEL_ID,
                "Transferencias",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Movimientos y transferencias de tu cuenta"
            }

            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun notificationsEnabled(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun buildTransferNotification(
        context: Context,
        title: String,
        body: String,
        movementId: String
    ): Notification {

        // Deep Link
        val deepLink = Uri.Builder()
            .scheme("notificationdemo")
            .authority("movements")
            .appendPath(movementId)
            .build()

        val intent = Intent(
            Intent.ACTION_VIEW,
            deepLink,
            context,
            MainActivity::class.java
        )

        // PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            context,
            movementId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // NotificationCompat.Builder
        return NotificationCompat.Builder(
            context,
            TRANSFERS_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_notification_transfer)
            .setContentTitle(title)
            .setContentText(body)

            // Interacción
            .setContentIntent(pendingIntent)

            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    //Publicar
    fun publishTransferNotification(
        context: Context,
        title: String,
        body: String,
        movementId: String
    ): Boolean {

        // POST_NOTIFICATIONS
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        if (!notificationsEnabled(context)) {
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = context
                .getSystemService(NotificationManager::class.java)
                .getNotificationChannel(TRANSFERS_CHANNEL_ID)

            if (channel?.importance == NotificationManager.IMPORTANCE_NONE) {
                return false
            }
        }

        createChannel(context)

        val notification = buildTransferNotification(
            context,
            title,
            body,
            movementId
        )

        return try {
            // NotificationManagerCompat
            NotificationManagerCompat
                .from(context)
                .notify(
                    TRANSFER_NOTIFICATION_ID,
                    notification
                )
            true
        } catch (e: SecurityException) {
            Log.w(
                "NotificationDemo",
                "Permiso retirado antes de publicar",
                e
            )

            false
        }
    }
}