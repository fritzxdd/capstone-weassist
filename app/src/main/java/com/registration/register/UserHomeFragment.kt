package com.registration.register

import CustomAdapter
import Quadruple
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class UserHomeFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: CustomAdapter
    private val dataList = mutableListOf<Quadruple<String, String, String, Int>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        val recyclerView: RecyclerView = view.findViewById(R.id.lawyers_recycler_view)
        adapter = CustomAdapter(requireContext(), dataList) { data ->
            val intent = Intent(requireContext(), ProfileActivity::class.java).apply {
                putExtra("NAME", data.first)
                putExtra("ID_NUMBER", data.second)
                putExtra("ROLE", data.third)
                putExtra("IMAGE_RESOURCE", data.fourth)
            }
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        fetchLawyers()
    }

    private fun fetchLawyers() {
        firestore.collection("lawyers")
            .get()
            .addOnSuccessListener { result ->
                handleFetchSuccess(result)
            }
            .addOnFailureListener { e ->
                Log.e("UserHomeFragment", "Error fetching data", e)
            }
    }

    private fun handleFetchSuccess(result: QuerySnapshot) {
        dataList.clear() // Clear existing data
        if (result.isEmpty) {
            Log.d("UserHomeFragment", "No profiles found.")
        } else {
            for (document in result) {
                val name = document.getString("fullname") ?: "Unknown"
                val rollNumber = document.getString("rollNumber") ?: "Unknown"
                val role = "Lawyer" // Static value or fetch if you have different roles
                val image = R.drawable.profile // Replace with actual image logic

                Log.d("UserHomeFragment", "Document ID: ${document.id}")
                Log.d("UserHomeFragment", "Profile: $name, $rollNumber")

                dataList.add(Quadruple("Name: $name", "Roll Number: $rollNumber", role, image))
            }
            adapter.notifyDataSetChanged() // Notify adapter of data change
            Log.d("UserHomeFragment", "DataList size: ${dataList.size}")
        }
    }
}
