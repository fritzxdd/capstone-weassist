package com.registration.register

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class HelpAndSupport : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_and_support)


        // Get reference to the arrow button
        val arrowButton: ImageView = findViewById(R.id.arrow)

        // Set OnClickListener for the arrow button
        arrowButton.setOnClickListener {
            // Navigate back to the RemedioThreeLines activity
            onBackPressed()
        }
    }
}
