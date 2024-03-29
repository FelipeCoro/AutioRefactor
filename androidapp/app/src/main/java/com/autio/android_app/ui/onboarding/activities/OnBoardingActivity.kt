package com.autio.android_app.ui.onboarding.activities

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.navArgs
import com.autio.android_app.R
import com.autio.android_app.databinding.ActivityOnBoardingBinding
import com.autio.android_app.ui.onboarding.fragments.OnBoardingFragmentArgs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnBoardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_on_boarding)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        } else {
            window.decorView.windowInsetsController?.hide(
                android.view.WindowInsets.Type.statusBars()
            )
        }
        window.navigationBarColor = ResourcesCompat.getColor(
            resources, R.color.autio_blue, null
        )
    }

    companion object {
        const val FINISHED_ONBOARDING = " finished_onboarding"
    }

}
