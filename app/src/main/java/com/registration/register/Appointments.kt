/*
package com.registration.register


import Quadruple
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class Appointments : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val listView: ListView = findViewById(R.id.list_view)
        val dataList = listOf(
            Quadruple("Name: January", "ID Number: 001", "Defense Attorney", R.drawable.alejandro),
            Quadruple("Name: February", "ID Number: 002", "Defense Attorney", R.drawable.stinkly),
            Quadruple("Name: March", "ID Number: 003", "Defense Attorney", R.drawable.hanni),
            Quadruple("Name: April", "ID Number: 004", "Defense Attorney", R.drawable.stupid)
        )
        val adapter = CustomAdapter(this, dataList)
        listView.adapter = adapter

        // Set item click listener on ListView
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            // Retrieve the data corresponding to the clicked item
            val data = dataList[position]

            // Prompt the user to select a date and time for the appointment
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, monthOfYear, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, monthOfYear)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val timePickerDialog = TimePickerDialog(
                        this,
                        { _, hourOfDay, minute ->
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            calendar.set(Calendar.MINUTE, minute)

                            // Schedule an alarm for the selected date and time
                            val name = data.first // Replace with the actual name from the data
                            scheduleAlarm(name, calendar.timeInMillis)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false
                    )
                    timePickerDialog.show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    private fun scheduleAlarm(name: String, timeInMillis: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AppointmentsNotificationReceiver::class.java).apply {
            action = "com.registration.ACTION_SET_APPOINTMENT"
            putExtra("NAME", name)
            putExtra("TIME", timeInMillis)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Schedule the alarm
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)

        Toast.makeText(this, "Appointment set for $name", Toast.LENGTH_SHORT).show()
    }
}
*/
