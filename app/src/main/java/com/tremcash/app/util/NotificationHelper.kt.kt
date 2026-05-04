package com.tremcash.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {
    fun showReminderNotification() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "tremcash_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Mudamos para IMPORTANCE_HIGH para ela "pular" na tela
            val channel = NotificationChannel(
                channelId,
                "Lembretes TremCash",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para lembretes de lançamentos financeiros"
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("TremCash: Hora de Lançar!")
            .setContentText("Não esqueça de registrar seu cafezinho ou lanche de hoje.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            // Para versões mais antigas do Android também "pularem"
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        manager.notify(1, notification)
    }
}