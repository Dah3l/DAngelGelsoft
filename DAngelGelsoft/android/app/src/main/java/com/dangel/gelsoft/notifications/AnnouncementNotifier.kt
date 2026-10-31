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
import com.dangel.gelsoft.data.AnnouncementItem

/**
 * System notification for NEW announcements. Fired from MainActivity after
 * the background JSON refresh, only for IDs the user hasn't seen yet.
 */
object AnnouncementNotifier {

    const val CHANNEL_ID = "dangel_avisos"
    private const val NOTIF_ID = 1001
    const val EXTRA_OPEN_ANNOUNCEMENTS = "abrir_avisos"

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Avisos de D'Angel Gelsoft",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = "Noches especiales y avisos del campo" }
            )
        }
    }

    fun notifyNew(context: Context, fresh: List<AnnouncementItem>) {
        if (fresh.isEmpty()) return
        ensureChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_OPEN_ANNOUNCEMENTS, true)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val first = fresh.first()
        val title = if (fresh.size == 1) first.title
        else "${fresh.size} avisos nuevos en D'Angel Gelsoft"
        val text = if (fresh.size == 1) first.body else first.title
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
            // POST_NOTIFICATIONS not granted: the in-app bell badge still works.
        }
    }
}
