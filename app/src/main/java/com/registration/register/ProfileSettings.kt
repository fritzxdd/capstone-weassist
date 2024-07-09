package com.registration.register

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileSettings : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var editTextUsername: EditText
    private lateinit var editTextFullName: EditText
    private lateinit var editTextLocation: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextOldPassword: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var profileImageView: ImageView

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    // Original values fetched from Firestore
    private var originalUsername: String? = null
    private var originalFullName: String? = null
    private var originalLocation: String? = null
    private var originalEmail: String? = null
    private var originalProfileImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val arrowButton = findViewById<ImageButton>(R.id.arrow)
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextFullName = findViewById(R.id.editTextFullName)
        editTextLocation = findViewById(R.id.editTextLocation)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextOldPassword = findViewById(R.id.editTextOldPassword)
        editTextPassword = findViewById(R.id.editTextPassword)
        profileImageView = findViewById(R.id.profile_image_view)
        val profileImageFrame = findViewById<FrameLayout>(R.id.profile_image_frame)
        val buttonSave = findViewById<Button>(R.id.buttonSave)
        val buttonCancel = findViewById<Button>(R.id.buttonCancel)
        val textDeleteAccount = findViewById<TextView>(R.id.textDeleteAccount)
        val modeSwitch = findViewById<Switch>(R.id.mode_switch)

        arrowButton.setOnClickListener {
            finish()
        }

        profileImageFrame.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        buttonSave.setOnClickListener {
            if (validateInputs()) {
                if (noChangesDetected()) {
                    Toast.makeText(this, "No changes recorded", Toast.LENGTH_SHORT).show()
                } else {
                    if (selectedImageUri != null) {
                        uploadImageToFirebase(selectedImageUri!!)
                    } else {
                        updateUserProfile()
                    }
                }
            }
        }

        buttonCancel.setOnClickListener {
            finish()
        }

        textDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }

        modeSwitch.isChecked = isDarkMode()

        modeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                saveDarkModeState(true)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                saveDarkModeState(false)
            }
        }

        fetchAndDisplayUserData()
    }

    private fun validateInputs(): Boolean {
        val username = editTextUsername.text.toString().trim()
        val fullName = editTextFullName.text.toString().trim()
        val location = editTextLocation.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val oldPassword = editTextOldPassword.text.toString().trim()
        val newPassword = editTextPassword.text.toString().trim()

        if (username.isEmpty() || fullName.isEmpty() || location.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in all the required fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (oldPassword.isNotEmpty() && newPassword.isEmpty()) {
            Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show()
            return false
        }

        if (newPassword.isNotEmpty() && oldPassword.isEmpty()) {
            Toast.makeText(this, "Please enter your old password", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun noChangesDetected(): Boolean {
        val username = editTextUsername.text.toString().trim()
        val fullName = editTextFullName.text.toString().trim()
        val location = editTextLocation.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val oldPassword = editTextOldPassword.text.toString().trim()
        val newPassword = editTextPassword.text.toString().trim()

        return username == originalUsername &&
                fullName == originalFullName &&
                location == originalLocation &&
                email == originalEmail &&
                selectedImageUri == null &&
                oldPassword.isEmpty() &&
                newPassword.isEmpty()
    }

    private fun saveDarkModeState(isDarkMode: Boolean) {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isDarkMode", isDarkMode).apply()
    }

    private fun isDarkMode(): Boolean {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        return sharedPreferences.getBoolean("isDarkMode", false)
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val storageReference = storage.reference.child("profile_images/$userId.jpg")
            val uploadTask = storageReference.putFile(imageUri)

            uploadTask.addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    updateUserProfile(uri.toString())
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Error uploading image: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserProfile(imageUrl: String? = null) {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val userUpdates = hashMapOf(
                "username" to editTextUsername.text.toString(),
                "fullname" to editTextFullName.text.toString(),
                "location" to editTextLocation.text.toString(),
                "email" to editTextEmail.text.toString()
            )

            if (imageUrl != null) {
                userUpdates["profileImageUrl"] = imageUrl
            }

            firestore.collection("users").document(userId).set(userUpdates)
                .addOnSuccessListener {
                    val newPassword = editTextPassword.text.toString()
                    if (newPassword.isNotEmpty()) {
                        updatePassword(user, newPassword)
                    } else {
                        Toast.makeText(this, "User data updated", Toast.LENGTH_SHORT).show()
                        redirectToDashboard()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error updating user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updatePassword(user: FirebaseUser, newPassword: String) {
        val oldPassword = editTextOldPassword.text.toString()
        val email = user.email

        if (email != null) {
            val credential = EmailAuthProvider.getCredential(email, oldPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                    updateUserPasswordInFirestore(user.uid, newPassword)
                                    redirectToDashboard()
                                } else {
                                    Toast.makeText(this, "Error updating password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Error reauthenticating: ${reauthTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun updateUserPasswordInFirestore(userId: String, newPassword: String) {
        firestore.collection("users").document(userId)
            .update("password", newPassword)
            .addOnSuccessListener {
                Toast.makeText(this, "Password updated in Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error updating password in Firestore: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchAndDisplayUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        displayUserData(document)
                    } else {
                        fetchLawyerData(userId)
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error getting user documents: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchLawyerData(userId: String) {
        firestore.collection("lawyers").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    displayUserData(document)
                } else {
                    Toast.makeText(this, "No such document found in users or lawyers collection", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting lawyer document: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayUserData(document: DocumentSnapshot) {
        editTextUsername.setText(document.getString("username"))
        editTextFullName.setText(document.getString("fullname"))
        editTextLocation.setText(document.getString("location"))
        editTextEmail.setText(document.getString("email"))
        originalUsername = document.getString("username")
        originalFullName = document.getString("fullname")
        originalLocation = document.getString("location")
        originalEmail = document.getString("email")
        originalProfileImageUrl = document.getString("profileImageUrl")
        if (originalProfileImageUrl != null && originalProfileImageUrl!!.isNotEmpty()) {
            profileImageView.setImageURI(Uri.parse(originalProfileImageUrl))
        }
    }

    private fun showDeleteAccountConfirmationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_account, null)
        val oldPasswordInput = dialogView.findViewById<EditText>(R.id.oldPasswordInput)

        AlertDialog.Builder(this).apply {
            setTitle("Delete Account")
            setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            setView(dialogView)
            setPositiveButton("Delete") { dialog, _ ->
                val oldPassword = oldPasswordInput.text.toString()
                deleteAccount(oldPassword)
                dialog.dismiss()
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    private fun deleteAccount(oldPassword: String) {
        val user = auth.currentUser
        if (user != null) {
            val email = user.email

            if (email != null) {
                val credential = EmailAuthProvider.getCredential(email, oldPassword)
                user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        deleteUserDataFromFirestore(user.uid) {
                            deleteUserFromAuth(user)
                        }
                    } else {
                        Toast.makeText(this, "Reauthentication failed: ${reauthTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun deleteUserDataFromFirestore(userId: String, onComplete: () -> Unit) {
        firestore.collection("users").document(userId).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                firestore.collection("lawyers").document(userId).delete().addOnCompleteListener { lawyerTask ->
                    if (lawyerTask.isSuccessful) {
                        onComplete()
                    } else {
                        Toast.makeText(this, "Error deleting lawyer data: ${lawyerTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Error deleting user data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteUserFromAuth(user: FirebaseUser) {
        user.delete().addOnCompleteListener { deleteTask ->
            if (deleteTask.isSuccessful) {
                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                redirectToLogin()
            } else {
                Toast.makeText(this, "Error deleting account: ${deleteTask.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }

    private fun redirectToDashboard() {
        val intent = Intent(this, UserDashboard::class.java)
        startActivity(intent)
        finish()
    }
}