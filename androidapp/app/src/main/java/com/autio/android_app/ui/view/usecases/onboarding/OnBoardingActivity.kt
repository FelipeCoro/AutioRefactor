package com.autio.android_app.ui.view.usecases.onboarding

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.autio.android_app.R
import com.autio.android_app.databinding.ActivityOnBoardingBinding

class OnBoardingActivity :
    AppCompatActivity() {

    private lateinit var binding: ActivityOnBoardingBinding

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
        binding =
            ActivityOnBoardingBinding.inflate(
                layoutInflater
            )
        setContentView(
            binding.root
        )
    }

    override fun onWindowFocusChanged(
        hasFocus: Boolean
    ) {
        super.onWindowFocusChanged(
            hasFocus
        )
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
        window.navigationBarColor =
            ResourcesCompat.getColor(
                resources,
                R.color.autio_blue,
                null
            )
    }
}