package com.registration.register

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar

class Notification : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        // Example data
        val name = "John Doe"
        val timeInMillis = System.currentTimeMillis() + 3600000 // One hour from now

        // Convert time to readable format
        val appointmentTime = convertTimeInMillisToReadableFormat(timeInMillis)

        // Display notification
        showNotification(this, name, appointmentTime)
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
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
