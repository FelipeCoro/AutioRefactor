package com.autio.android_app.ui.view.usecases.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.autio.android_app.databinding.ActivityLoginBinding

class LoginActivity :
    AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
        binding =
            ActivityLoginBinding.inflate(
                layoutInflater
            )
        setContentView(
            binding.root
        )

        intentsButtons()
    }

    private fun intentsButtons() {
        binding.btnSignin.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SignInActivity::class.java
                )
            )
        }
        binding.btnSignup.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SignUpActivity::class.java
                )
            )
        }
    }

}