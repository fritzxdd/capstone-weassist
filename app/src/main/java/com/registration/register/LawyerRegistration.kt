package com.registration.register

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern

class LawyerRegistration : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firestore: FirebaseFirestore
    private val passwordPattern = Pattern.compile(
        "^" +                        // start-of-string
                "(?=.*[0-9])" +              // a digit must occur at least once
                "(?=.*[a-z])" +              // a lower case letter must occur at least once
                "(?=.*[A-Z])" +              // an upper case letter must occur at least once
                "(?=.*[@#\$%^&+=])" +        // a special character must occur at least once
                "(?=\\S+\$)" +               // no whitespace allowed in the entire string
                ".{6,}" +                    // at least 6 characters
                "$"                          // end-of-string
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_for_lawyers)

        // Initialize SharedPreferences and Firestore
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        firestore = FirebaseFirestore.getInstance()

        // Find views
        val userNameEditText = findViewById<EditText>(R.id.editTextUserName)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.editTextComfirmPassword)
        val fullNameEditText = findViewById<EditText>(R.id.editTextfullName)
        val specializationEditText = findViewById<EditText>(R.id.editTextSpecialization)
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val phoneNumberEditText = findViewById<EditText>(R.id.editTextPhoneNumber)
        val locationEditText = findViewById<EditText>(R.id.editTextlocation)
        val registerButton = findViewById<Button>(R.id.buttonRegister)
        val auth: FirebaseAuth = Firebase.auth

        // Set OnClickListener to the Register button
        registerButton.setOnClickListener {
            // Retrieve input values
            val userName = userNameEditText.text.toString()
            val fullName = fullNameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val specialization = specializationEditText.text.toString()
            val email = emailEditText.text.toString()
            val phoneNumber = phoneNumberEditText.text.toString()
            val location = locationEditText.text.toString()

            // Perform validation
            if (userName.isEmpty() || fullName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                specialization.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!passwordPattern.matcher(password).matches()) {
                Toast.makeText(this, "Password must contain at least one uppercase letter, one number, and one special character", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save data to SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putString("username", userName)
            editor.putString("fullname", fullName)
            editor.putString("password", password)
            editor.putString("email", email)
            editor.putString("phoneNumber", phoneNumber)
            editor.putString("location", location)
            editor.apply()

            // Create user with Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        // Update user's display name
                        val profileUpdates = userProfileChangeRequest {
                            displayName = fullName
                        }
                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    // Get user ID
                                    val userId = user.uid

                                    // Create user data map
                                    val userData = hashMapOf(
                                        "username" to userName,
                                        "fullname" to fullName,
                                        "email" to email,
                                        "specialization" to specialization,
                                        "phoneNumber" to phoneNumber,
                                        "location" to location,
                                        "userType" to "lawyer" // Add userType field
                                    )

                                    // Save user data to Firestore
                                    firestore.collection("lawyers").document(userId).set(userData)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                                            // Redirect to login activity
                                            val intent = Intent(this, Login::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(this, "Failed to update profile: ${profileTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
