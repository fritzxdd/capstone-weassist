package com.registration.register

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class ProfileActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        firestore = FirebaseFirestore.getInstance()

        // Retrieve the data from the intent
        val name = intent.getStringExtra("NAME")
        val idNumber = intent.getStringExtra("ID_NUMBER")
        val role = intent.getStringExtra("ROLE")
        val imageResource = intent.getIntExtra("IMAGE_RESOURCE", -1)

        // Find the views in the layout
        val nameTextView: TextView = findViewById(R.id.name_text_view)
        val idNumberTextView: TextView = findViewById(R.id.id_number_text_view)
        val roleTextView: TextView = findViewById(R.id.role_text_view)
        val imageView: ImageView = findViewById(R.id.profile_image_view)
        val setAppointmentButton: Button = findViewById(R.id.set_appointment_button)

        // Set the data to the views
        nameTextView.text = name
        idNumberTextView.text = idNumber
        roleTextView.text = role
        imageView.setImageResource(imageResource)

        // Set button click listener
        setAppointmentButton.setOnClickListener {
            val calendar = Calendar.getInstance()

            // Initialize the DatePickerDialog with the current date
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

                            // Save the appointment details to Firestore
                            saveAppointmentToFirestore(name, calendar.timeInMillis)

                            // Notify user and finish activity
                            Toast.makeText(this, "Appointment set for $name on ${calendar.time}", Toast.LENGTH_SHORT).show()
                            finish()
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

            // Set the minimum date to today
            datePickerDialog.datePicker.minDate = calendar.timeInMillis
            datePickerDialog.show()
        }
    }

    private fun saveAppointmentToFirestore(name: String?, timeInMillis: Long) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val appointment = hashMapOf(
                "name" to name,
                "time" to timeInMillis,
                "userId" to currentUser.uid
            )
            firestore.collection("appointments")
                .add(appointment)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this, "Appointment recorded with ID: ${documentReference.id}", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving appointment: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}
