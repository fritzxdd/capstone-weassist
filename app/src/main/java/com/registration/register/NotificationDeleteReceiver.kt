import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class NotificationDeleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.registration.ACTION_DELETE_NOTIFICATION") {
            val notificationId = intent.getIntExtra("NOTIFICATION_ID", -1)
            if (notificationId != -1) {
                // Cancel the notification
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.cancel(notificationId)
            }
        }
    }
}
