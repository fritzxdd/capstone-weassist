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
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

class UserRegistration : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val passwordPattern = Pattern.compile(
        "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_for_users)

        // Initialize FirebaseAuth and Firestore instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        // Find views
        val userNameEditText = findViewById<EditText>(R.id.userName)
        val fullNameEditText = findViewById<EditText>(R.id.fullName)
        val passwordEditText = findViewById<EditText>(R.id.passWord)
        val confirmPasswordEditText = findViewById<EditText>(R.id.conPassword)
        val emailEditText = findViewById<EditText>(R.id.Email)
        val phoneNumberEditText = findViewById<EditText>(R.id.number)
        val locationEditText = findViewById<EditText>(R.id.location)
        val registerButton = findViewById<Button>(R.id.RegisterButton)

        // Set OnClickListener to the Register button
        registerButton.setOnClickListener {
            // Retrieve input values
            val userName = userNameEditText.text.toString()
            val fullName = fullNameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val email = emailEditText.text.toString()
            val phoneNumber = phoneNumberEditText.text.toString()
            val location = locationEditText.text.toString()

            // Perform validation
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isPasswordValid(password)) {
                Toast.makeText(
                    this,
                    "Password must have at least 8 characters including 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create user with Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Get user ID
                        val userId = auth.currentUser?.uid

                        // Create user data map including userType
                        val user = hashMapOf(
                            "username" to userName,
                            "fullname" to fullName,
                            "email" to email,
                            "phoneNumber" to phoneNumber,
                            "location" to location,
                            "userType" to "user" // Set userType for regular users
                        )

                        // Save user data to Firestore
                        userId?.let {
                            firestore.collection("users").document(it).set(user)
                                .addOnSuccessListener {
                                    // Save data to SharedPreferences
                                    val editor = sharedPreferences.edit()
                                    editor.putString("username", userName)
                                    editor.putString("password", password)
                                    editor.putString("email", email)
                                    editor.putString("fullname", fullName)
                                    editor.putString("location", location)
                                    editor.putString("phoneNumber", phoneNumber)
                                    editor.apply()

                                    // Registration successful
                                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                                    // Redirect to login activity
                                    val intent = Intent(this, Login::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "Registration failed: ${it.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    } else {
                        // Registration failed
                        Toast.makeText(
                            this,
                            "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return passwordPattern.matcher(password).matches()
    }
}
