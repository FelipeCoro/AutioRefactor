package com.autio.android_app.ui.launch

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import com.autio.android_app.R
import com.autio.android_app.data.repository.legacy.FirebaseStoryRepository
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.ui.login.fragments.LoginFragment
import com.autio.android_app.ui.onboarding.OnBoardingActivity
import com.autio.android_app.ui.stories.BottomNavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var prefRepository: PrefRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        isNightModeOn()
        FirebaseStoryRepository
        whereToGo()
    }

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

    /**
     * Redirects the user to the proper view based on two conditions:
     * - The user has opened the app for the first time, which then it'll
     *   be redirected to [OnBoardingActivity]
     * - The user is signed in, which will navigate the user to the [BottomNavigation]
     *   if user is logged, [LoginFragment] if false
     */
    private fun whereToGo() {
        if (onBoardingFinished()) {
            // Checks if user is logged in based on the preferences saved
            // It could be used any of the user's properties since all of them are saved
            // once the user logs in (either with an account or as a guest),
            // but it is found more properly to check for the API token and firebase key
            // since the communication with backend requires any of these two
            if (prefRepository.userApiToken.isEmpty() || prefRepository.firebaseKey.isEmpty()) {

              // val navHostFragment = supportFragmentManager.findFragmentById(R.id.authentication_nav_host) as NavHostFragment
              //  val navController = navHostFragment.navController

                findNavController.navigate(R.id.loginFragment)

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
