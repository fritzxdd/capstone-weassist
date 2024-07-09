package com.registration.register

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val repAct: TextView = view.findViewById(R.id.republic_act)
        val execOrder: TextView = view.findViewById(R.id.exec_order)
        val presProc: TextView = view.findViewById(R.id.pres_proc)
        val batasPam: TextView = view.findViewById(R.id.batas_pambansa)
        val comWea: TextView = view.findViewById(R.id.commonwealth_act)

        val hamburgerIcon: ImageButton = view.findViewById(R.id.hamburger_icon)
        val searchView: SearchView = view.findViewById(R.id.search_view)

        // Bring views to front
        hamburgerIcon.bringToFront()
        searchView.bringToFront()

        // Set click listener for the hamburger icon to start Remedio_ThreeLines activity
        hamburgerIcon.setOnClickListener {
            val intent = Intent(requireActivity(), Settings::class.java)
            startActivity(intent)
        }

        repAct.setOnClickListener {
            openUrl("https://lawphil.net/statutes/repacts/repacts.html")
        }

        execOrder.setOnClickListener {
            openUrl("https://lawphil.net/executive/execord/execord.html")
        }

        presProc.setOnClickListener {
            openUrl("https://lawphil.net/statutes/presdecs/legis_pd.html")
        }

        batasPam.setOnClickListener {
            openUrl("https://lawphil.net/statutes/bataspam/bataspam.html")
        }
        comWea.setOnClickListener {
            openUrl("https://lawphil.net/statutes/bataspam/bataspam.html")
        }
        return view
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}