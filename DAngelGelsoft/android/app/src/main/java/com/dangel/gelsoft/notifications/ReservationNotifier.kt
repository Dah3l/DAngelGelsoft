package com.dangel.gelsoft.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.dangel.gelsoft.MainActivity
import com.dangel.gelsoft.R

/**
 * System notification when the admin confirms or rejects a reservation.
 * Fired from MainActivity after comparing the cached estado with the DB.
 */
object ReservationNotifier {

    const val CHANNEL_ID = "dangel_reservas"
    private const val NOTIF_ID = 1002
    const val EXTRA_OPEN_RESERVAS = "abrir_mis_reservas"

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Estado de tus reservas",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = "Te avisamos cuando confirmen o rechacen tu reserva" }
            )
        }
    }

    fun notifyStatus(
        context: Context,
        fecha: String,
        bloqueNombre: String,
        estado: String,
    ) {
        ensureChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_OPEN_RESERVAS, true)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val (title, emoji) = if (estado == "confirmada") {
            "¡Reserva confirmada! ✅" to "✅"
        } else {
            "Reserva rechazada ❌" to "❌"
        }
        val text = "$emoji $fecha · Bloque $bloqueNombre — " +
            if (estado == "confirmada") {
                "el administrador confirmó tu reserva. ¡Nos vemos en el campo!"
            } else {
                "el administrador rechazó tu reserva. Elige otro bloque disponible."
            }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_dangel)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            manager.notify(NOTIF_ID, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted: the status still shows in "Mis reservas".
        }
    }
}
