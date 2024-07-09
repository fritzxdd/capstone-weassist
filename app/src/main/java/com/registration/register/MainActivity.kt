package com.registration.register

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button: Button = findViewById(R.id.lawyersButton)
        // Set OnClickListener to the button
        button.setOnClickListener {
            // Start the new activity here
            val intent = Intent(this, LawyerRegistration::class.java)
            startActivity(intent)

            val button: Button = findViewById(R.id.usersButton)
            // Set OnClickListener to the button
            button.setOnClickListener {
                // Start the new activity here
                val intent = Intent(this, UserRegistration::class.java)
                startActivity(intent)


            }

        }
            // Initialize Firebase
            FirebaseApp.initializeApp(this)
        }
    }


