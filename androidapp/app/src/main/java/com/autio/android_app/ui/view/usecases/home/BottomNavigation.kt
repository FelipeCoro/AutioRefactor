package com.autio.android_app.ui.view.usecases.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.get
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.autio.android_app.R
import com.autio.android_app.databinding.ActivityBottomNavigationBinding
import com.autio.android_app.ui.view.usecases.subscribe.SubscribeActivity

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
        setListeners()
    }

    private fun setListeners() {
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
        navController.addOnDestinationChangedListener { controller, destination, _ ->
            if (controller.graph[R.id.player] == destination) {
                hidePlayerComponent()
            } else if (binding.floatingPersistentPlayer.visibility != View.VISIBLE) {
                showPlayerComponent()
            }
        }
        binding.relativeLayoutSeePlans.setOnClickListener {
            val subscribeIntent =
                Intent(
                    this,
                    SubscribeActivity::class.java
                )
            startActivity(
                subscribeIntent
            )
        }
    }

    private fun hidePlayerComponent() {
        binding.floatingPersistentPlayer.animate()
            .alpha(
                0.0f
            )
            .translationY(
                binding.floatingPersistentPlayer.height.toFloat()
            )
            .withEndAction {
                binding.mainContainer.requestLayout()
                binding.floatingPersistentPlayer.visibility =
                    View.GONE
            }
    }

    private fun showPlayerComponent() {
        binding.floatingPersistentPlayer.visibility =
            View.VISIBLE
        binding.floatingPersistentPlayer.animate()
            .alpha(
                1.0f
            )
            .translationYBy(
                -binding.floatingPersistentPlayer.height.toFloat()
            )
            .withEndAction {
                binding.mainContainer.requestLayout()
            }
    }
}