package com.autio.android_app.ui.view.usecases.launch

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.autio.android_app.data.repository.PrefRepository
import com.autio.android_app.ui.view.usecases.home.BottomNavigation
import com.autio.android_app.ui.view.usecases.login.LoginActivity
import com.autio.android_app.ui.view.usecases.onboarding.OnBoardingActivity


class SplashActivity :
    AppCompatActivity() {

    private val prefRepository by lazy {
        PrefRepository(
            this
        )
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
        isNightModeOn()
        whereToGo()
    }

    //val sharedPreferences = this.getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
    //return sharedPreferences.getBoolean("Finished", false)
    private fun onBoardingFinished(): Boolean {
        val sharedPreferences =
            this.getSharedPreferences(
                "onBoarding",
                Context.MODE_PRIVATE
            )
        return sharedPreferences.getBoolean(
            "Finished",
            false
        )
    }

    private fun isNightModeOn() {
        when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
            Configuration.UI_MODE_NIGHT_NO -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
            Configuration.UI_MODE_NIGHT_UNDEFINED -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
        }
    }

    private fun getUserSession(): String =
        prefRepository.userApiToken

    private fun whereToGo() {
        val apiToken =
            getUserSession()
        if (onBoardingFinished()) {
            if (apiToken.isEmpty()) {
                startActivity(
                    Intent(
                        this,
                        LoginActivity::class.java
                    )
                )
                finish()
            } else {
                startActivity(
                    Intent(
                        this,
                        BottomNavigation::class.java
                    )
                )
                finish()
            }
        } else {
            startActivity(
                Intent(
                    this,
                    OnBoardingActivity::class.java
                )
            )
            finish()
        }
    }

}