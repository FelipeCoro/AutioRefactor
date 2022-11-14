package com.autio.android_app.ui.view.usecases.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.autio.android_app.R
import com.autio.android_app.databinding.ActivityBottomNavigationBinding

class BottomNavigation :
    AppCompatActivity() {
    private lateinit var binding: ActivityBottomNavigationBinding
    private lateinit var navController: NavController

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
        binding =
            ActivityBottomNavigationBinding.inflate(
                layoutInflater
            )
        setContentView(
            binding.root
        )

        val navHostFragment =
            supportFragmentManager.findFragmentById(
                R.id.mainContainer
            ) as NavHostFragment
        navController =
            navHostFragment.navController
        setupWithNavController(
            binding.bottomNavigationView,
            navController
        )
    }
}