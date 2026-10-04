package com.morex.father

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.morex.father.alerts.AlertsFragment
import com.morex.father.children.ChildrenFragment
import com.morex.father.dashboard.DashboardFragment
import com.morex.father.databinding.ActivityMainBinding
import com.morex.father.location.MapFragment
import com.morex.father.settings.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNav()

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
            binding.bottomNav.selectedItemId = R.id.nav_home
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> DashboardFragment()
                R.id.nav_children -> ChildrenFragment()
                R.id.nav_map -> MapFragment()
                R.id.nav_alerts -> AlertsFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> DashboardFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun navigateTo(itemId: Int) {
        binding.bottomNav.selectedItemId = itemId
    }
}
