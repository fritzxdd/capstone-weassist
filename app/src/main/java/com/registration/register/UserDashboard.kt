package com.registration.register

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class UserDashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.nav_home -> selectedFragment = UserHomeFragment()
                R.id.nav_notifications -> selectedFragment = UserNotificationsFragment()
                R.id.nav_messages -> selectedFragment = UserMessagesFragment()
                R.id.nav_appointments -> selectedFragment = UserAppointmentsFragment()
                R.id.nav_profile -> selectedFragment = UserProfileFragment()
            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit()
            }
            true
        }

        // Set default selection
        bottomNavigationView.selectedItemId = R.id.nav_home
    }
}
