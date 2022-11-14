package com.autio.android_app.ui.view.usecases.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
}