package com.example.data.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val CHANNEL_ID = "finance_alerts_channel"
    private const val CHANNEL_NAME = "Budget Alerts"
    private const val CHANNEL_DESC = "Notifications for category spending limit warnings"
    private const val NOTIFICATION_ID_BASE = 1000

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showLimitWarning(context: Context, categoryName: String, percent: Int, amountSpent: Double, limit: Double) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val amountStr = String.format("$%,.0f COP", amountSpent)
        val limitStr = String.format("$%,.0f COP", limit)
        
        val title = if (percent >= 100) {
            "🔴 ¡Presupuesto Excedido en $categoryName!"
        } else {
            "⚠️ Alerta de Presupuesto en $categoryName"
        }

        val text = if (percent >= 100) {
            "Has gastado $amountStr de un límite mensual de $limitStr ($percent%)."
        } else {
            "Has alcanzado el $percent% de tu presupuesto ($amountStr gastados de $limitStr)."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Basic tag-based notification generation
        notificationManager.notify(categoryName.hashCode(), builder.build())
    }
}
