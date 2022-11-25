package com.autio.android_app.ui.view.usecases.home.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.autio.android_app.data.repository.PrefRepository
import com.autio.android_app.databinding.FragmentMyStoriesBinding
import com.autio.android_app.ui.view.usecases.login.SignInActivity
import com.autio.android_app.ui.view.usecases.login.SignUpActivity


class MyStoriesFragment :
    Fragment() {

    private var _binding: FragmentMyStoriesBinding? =
        null
    private val binding get() = _binding!!
    private val prefRepository by lazy {
        PrefRepository(
            requireContext()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentMyStoriesBinding.inflate(
                inflater,
                container,
                false
            )

        createView()
        intentFunctions()
        return binding.root
    }

    private fun createView() {
        if (isUserGuest()) {
            binding.lySignIn.visibility =
                View.VISIBLE
            binding.lyStoriesList.visibility =
                View.GONE
        } else {
            binding.lySignIn.visibility =
                View.GONE
            binding.lyStoriesList.visibility =
                View.VISIBLE
        }
    }

    private fun intentFunctions() {
        binding.btnSignIn.setOnClickListener {
            goToSignIn()
        }
        binding.btnSignup.setOnClickListener {
            goToSignUp()
        }
    }

    private fun goToSignIn() {
        val signInIntent =
            Intent(
                activity,
                SignInActivity::class.java
            )
        startActivity(
            signInIntent
        )
    }

    private fun goToSignUp() {
        val signUpIntent =
            Intent(
                activity,
                SignUpActivity::class.java
            )
        startActivity(
            signUpIntent
        )
    }

    private fun isUserGuest(): Boolean =
        prefRepository.isUserGuest


}