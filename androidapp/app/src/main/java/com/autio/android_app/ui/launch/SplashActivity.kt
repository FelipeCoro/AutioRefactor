package com.autio.android_app.ui.launch

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.autio.android_app.R
import com.autio.android_app.ui.login.fragments.LoginFragment
import com.autio.android_app.ui.onboarding.activities.OnBoardingActivity
import com.autio.android_app.ui.stories.BottomNavigation
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        isNightModeOn()
        navigate()
    }

    private fun isNightModeOn() {
        when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            Configuration.UI_MODE_NIGHT_NO ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Configuration.UI_MODE_NIGHT_UNDEFINED ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    /**
     * Redirects the user to the proper view based on two conditions:
     * - The user has opened the app for the first time, which then it'll
     *   be redirected to [OnBoardingActivity]
     * - The user is signed in, which will navigate the user to the [BottomNavigation]
     *   if user is logged, [LoginFragment] if false
     */
    private fun navigate() {
        startActivity(Intent(this, OnBoardingActivity::class.java))
    }
}
