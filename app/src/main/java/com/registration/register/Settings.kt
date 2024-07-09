package com.registration.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Settings : AppCompatActivity() {

    // Firestore instance
    private val db = FirebaseFirestore.getInstance()
    // Collection references
    private val usersCollection = db.collection("users")
    private val lawyersCollection = db.collection("lawyers")
    // Reference to the TextView
    private lateinit var fullNameTextView: TextView

    // Declaring ACCOUNT_SETTINGS_REQUEST_CODE
    private val ACCOUNT_SETTINGS_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize fullNameTextView
        fullNameTextView = findViewById(R.id.full_name_text)

        // Get the user's document from Firestore
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            retrieveFullName(user.uid)
        }

        // Get reference to the "Account Settings" TextView
        val accountSettingsText: TextView = findViewById(R.id.account_settings_text)

        // Set OnClickListener for the "Account Settings" TextView
        accountSettingsText.setOnClickListener {
            startActivityForResult(Intent(this, ProfileSettings::class.java), ACCOUNT_SETTINGS_REQUEST_CODE)
        }

        // Get reference to the "Help and Support" TextView
        val helpSupportText: TextView = findViewById(R.id.help_support_text)

        // Set OnClickListener for the "Help and Support" TextView
        helpSupportText.setOnClickListener {
            startActivity(Intent(this, HelpAndSupport::class.java))
        }

        // Get reference to the "About WeAssist" TextView
        val aboutText: TextView = findViewById(R.id.about_text)

        // Set OnClickListener for the "About WeAssist" TextView
        aboutText.setOnClickListener {
            startActivity(Intent(this, AboutWeAssist::class.java))
        }

        // Get reference to the "Log Out" TextView
        val logoutText: TextView = findViewById(R.id.logout_text)

        // Set OnClickListener for the "Log Out" TextView
        logoutText.setOnClickListener {
            // Sign out the user
            FirebaseAuth.getInstance().signOut()

            // Redirect to the Login Activity
            startActivity(Intent(this, Login::class.java).apply {
                // Clear the back stack to prevent going back to Dashboard
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            // Finish the current activity
            finish()
        }
    }

    private fun retrieveFullName(uid: String) {
        // Check if the user is in the users collection
        usersCollection.document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("fullname")
                    fullNameTextView.text = username
                } else {
                    // If not found in users collection, check lawyers collection
                    retrieveLawyerFullName(uid)
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting user document", exception)
            }
    }

    private fun retrieveLawyerFullName(uid: String) {
        // Check if the user is in the lawyers collection
        lawyersCollection.document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val lawyerName = document.getString("fullname")
                    fullNameTextView.text = lawyerName
                } else {
                    Log.d(TAG, "User not found in users or lawyers collection")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting lawyer document", exception)
            }
    }

    companion object {
        private const val TAG = "SettingsActivity"
    }
}
