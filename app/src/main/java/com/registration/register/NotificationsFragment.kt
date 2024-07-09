package com.registration.register

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationsFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val appointmentList = mutableListOf<String>()
    private val appointmentIds = mutableListOf<String>() // List to store appointment IDs
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        listView = view.findViewById(R.id.appointments_list_view)
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, appointmentList)
        listView.adapter = adapter

        // Set item click listener to show a toast message for now
        listView.setOnItemClickListener { _, _, position, _ ->
            Toast.makeText(requireContext(), appointmentList[position], Toast.LENGTH_SHORT).show()
        }

        // Set long click listener to delete appointment
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val appointment = appointmentList[position]
            confirmAppointmentDeletion(appointment, position)
            true
        }

        loadAppointments()
    }

    private fun confirmAppointmentDeletion(appointment: String, position: Int) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Are you sure you want to delete this appointment?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                deleteAppointment(position)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }

        val alert = dialogBuilder.create()
        alert.setTitle("Delete Appointment")
        alert.show()
    }

    private fun deleteAppointment(position: Int) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("appointments")
                .document(appointmentIds[position]) // Assuming you have stored appointment IDs along with appointment list
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Appointment deleted", Toast.LENGTH_SHORT).show()
                    appointmentList.removeAt(position)
                    appointmentIds.removeAt(position) // Remove corresponding ID
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e("NotificationsFragment", "Error deleting appointment", e)
                    Toast.makeText(requireContext(), "Failed to delete appointment", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e("NotificationsFragment", "No authenticated user found")
            Toast.makeText(requireContext(), "Authentication error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAppointments() {
        appointmentList.clear()
        appointmentIds.clear()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("appointments")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val name = document.getString("name") ?: "Unknown"
                        val time = document.getLong("time") ?: 0L
                        val date = Date(time)
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        val formattedDate = dateFormat.format(date)
                        val appointmentInfo = "Appointment for $name at $formattedDate"
                        appointmentList.add(appointmentInfo)
                        appointmentIds.add(document.id) // Store the document ID
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e("NotificationsFragment", "Error loading appointments", e)
                }
        } else {
            Log.e("NotificationsFragment", "No authenticated user found")
        }
    }
}
