package com.registration.register

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AppointmentsNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.registration.ACTION_SET_APPOINTMENT") {
            val name = intent.getStringExtra("NAME")
            val timeInMillis = intent.getLongExtra("TIME", 0)

            val appointmentTime = convertTimeInMillisToReadableFormat(timeInMillis)

            // Display a notification for the appointment reminder
            val appContext = context.applicationContext // Access application context
            showNotification(appContext, name ?: "Unknown", appointmentTime)
        }
    }

    private fun showNotification(context: Context, name: String, appointmentTime: String) {
        val channelId = "appointment_reminder"
        val notificationId = 123

        // Create a notification channel for devices running Android Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Appointment Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Inflate custom layout for notification
        val remoteViews = RemoteViews(context.packageName, R.layout.activity_notification)
        remoteViews.setTextViewText(R.id.notification_title, "Appointment Reminder")
        remoteViews.setTextViewText(
            R.id.notification_content,
            "Your appointment with $name is due at $appointmentTime."
        )

        // Build the notification
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContent(remoteViews) // Set custom content view

        // Show the notification
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Handle permission request
            return
        }
        notificationManagerCompat.notify(notificationId, builder.build())
    }

    private fun convertTimeInMillisToReadableFormat(timeInMillis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}
