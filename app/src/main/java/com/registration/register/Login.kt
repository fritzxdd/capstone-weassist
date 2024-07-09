package com.registration.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONObject

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var usernameEditText: AutoCompleteTextView
    private lateinit var passwordEditText: EditText
    private lateinit var rememberMeCheckBox: CheckBox
    private var isPasswordVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Find views
        usernameEditText = findViewById(R.id.email_input)
        passwordEditText = findViewById(R.id.password_input)
        val loginButton = findViewById<Button>(R.id.login_button)
        val togglePasswordVisibility = findViewById<ImageView>(R.id.toggle_password_visibility)
        rememberMeCheckBox = findViewById(R.id.remember_me_checkbox)

        // Load saved credentials and set up suggestions
        loadCredentials()

        // Set OnClickListener to the login button
        loginButton.setOnClickListener {
            // Retrieve input values
            val email = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Authenticate user
            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (rememberMeCheckBox.isChecked) {
                    saveCredentials(email, password)
                }
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Find the TextView for "Don't have an Account?"
        val noAccountTextView = findViewById<TextView>(R.id.noaccount)

        // Set OnClickListener to handle clicks on "Don't have an Account?" TextView
        noAccountTextView.setOnClickListener {
            // Create an intent to navigate back to the main activity
            val intent = Intent(this@Login, MainActivity::class.java)
            startActivity(intent)
            // Finish the current activity if needed
            finish()
        }

        // Find the TextView for "Forgot Password?"
        val forgotPasswordTextView = findViewById<TextView>(R.id.nopassword)

        // Set OnClickListener to handle clicks on "Forgot Password?" TextView
        forgotPasswordTextView.setOnClickListener {
            // Handle the click event here
            // For example, navigate to a ForgotPasswordActivity
            // or show a dialog to reset the password
            // For demonstration, let's navigate to a new activity
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }

        // Set OnClickListener to toggle password visibility
        togglePasswordVisibility.setOnClickListener {
            if (isPasswordVisible) {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePasswordVisibility.setImageResource(R.drawable.icons_eye_closed)
            } else {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT
                togglePasswordVisibility.setImageResource(R.drawable.ic_eye_opened)
            }
            // Move the cursor to the end of the text
            passwordEditText.setSelection(passwordEditText.text.length)
            isPasswordVisible = !isPasswordVisible
        }

        // Set OnItemClickListener to populate password when email is selected
        usernameEditText.setOnItemClickListener { _, _, position, _ ->
            val selectedEmail = usernameEditText.adapter.getItem(position) as String
            val password = getPasswordForEmail(selectedEmail)
            passwordEditText.setText(password)
        }
    }

    // Method to authenticate user using Firebase Auth
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, check user role and redirect
                    val user = auth.currentUser
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    checkUserRoleAndRedirect(email)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Method to check user role and redirect to appropriate dashboard
    private fun checkUserRoleAndRedirect(email: String) {
        val usersRef = firestore.collection("users")
            .whereEqualTo("email", email)
            .limit(1)

        usersRef.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents[0]
                val userType = document.getString("userType") ?: "user" // Default to "user" if userType is not set

                if (userType == "user") {
                    // User is a regular user
                    val intent = Intent(this, UserDashboard::class.java)
                    startActivity(intent)
                    finish()
                } else if (userType == "lawyer") {
                    // User is a lawyer
                    val intent = Intent(this, LawyerDashboard::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Unknown userType or userType not set
                    Toast.makeText(this, "User role not found or invalid", Toast.LENGTH_SHORT).show()
                }
            } else {
                // No user document found, check lawyers collection
                checkLawyerRoleAndRedirect(email)
            }
        }.addOnFailureListener { exception ->
            // Handle failures
            Toast.makeText(this, "Failed to retrieve user document: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLawyerRoleAndRedirect(email: String) {
        val lawyersRef = firestore.collection("lawyers")
            .whereEqualTo("email", email)
            .limit(1)

        lawyersRef.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents[0]
                val userType = document.getString("userType") ?: "lawyer" // Default to "lawyer" if userType is not set

                if (userType == "lawyer") {
                    // User is a lawyer
                    val intent = Intent(this, LawyerDashboard::class.java)
                    startActivity(intent)
                    finish()
                } else if (userType == "user") {
                    // User is a regular user
                    val intent = Intent(this, UserDashboard::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Unknown userType or userType not set
                    Toast.makeText(this, "User role not found or invalid", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Document not found
                Toast.makeText(this, "User document not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            // Handle failures
            Toast.makeText(this, "Failed to retrieve user document: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }





    // Method to save credentials
    private fun saveCredentials(email: String, password: String) {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val credentials = JSONArray(sharedPref.getString("credentials", "[]"))

        // Check if the email already exists in saved credentials
        var emailExists = false
        for (i in 0 until credentials.length()) {
            val credential = credentials.getJSONObject(i)
            if (credential.getString("email") == email) {
                credential.put("password", password)
                emailExists = true
                break
            }
        }

        // If email does not exist, add new credentials
        if (!emailExists) {
            val newCredential = JSONObject()
            newCredential.put("email", email)
            newCredential.put("password", password)
            credentials.put(newCredential)
        }

        with(sharedPref.edit()) {
            putString("credentials", credentials.toString())
            apply()
        }
    }

    // Method to load credentials and set up AutoCompleteTextView
    private fun loadCredentials() {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val credentials = JSONArray(sharedPref.getString("credentials", "[]"))
        val emails = mutableListOf<String>()

        for (i in 0 until credentials.length()) {
            val credential = credentials.getJSONObject(i)
            emails.add(credential.getString("email"))
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, emails)
        usernameEditText.setAdapter(adapter)
    }

    // Method to get password for a given email
    private fun getPasswordForEmail(email: String): String? {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val credentials = JSONArray(sharedPref.getString("credentials", "[]"))

        for (i in 0 until credentials.length()) {
            val credential = credentials.getJSONObject(i)
            if (credential.getString("email") == email) {
                return credential.getString("password")
            }
        }
        return null
    }
}
