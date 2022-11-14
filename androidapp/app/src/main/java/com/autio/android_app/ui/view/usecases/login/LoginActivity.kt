package com.autio.android_app.ui.view.usecases.login

import android.content.Intent
import android.os.Bundle
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.adapter.ImageAdapter
import com.autio.android_app.data.Datasource
import com.autio.android_app.databinding.ActivityLoginBinding
import com.autio.android_app.setAutomaticScroll

class LoginActivity :
    AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var firstRecyclerView: RecyclerView
    private lateinit var secondRecyclerView: RecyclerView
    private lateinit var thirdRecyclerView: RecyclerView

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

        setUpBackgroundAnimation()
        intentsButtons()
    }

    private fun setUpBackgroundAnimation() {
        val imageDataset =
            Datasource().loadLocationViews()
        firstRecyclerView =
            findViewById(
                R.id.rvFirstColumn
            )
        secondRecyclerView =
            findViewById(
                R.id.rvSecondColumn
            )
        thirdRecyclerView =
            findViewById(
                R.id.rvThirdColumn
            )

        firstRecyclerView.adapter =
            ImageAdapter(
                imageDataset.shuffled()
            )
        firstRecyclerView.layoutManager!!.scrollToPosition(
            Integer.MAX_VALUE / 2
        )

        secondRecyclerView.adapter =
            ImageAdapter(
                imageDataset.shuffled()
            )
        secondRecyclerView.layoutManager!!.scrollToPosition(
            Integer.MAX_VALUE / 2
        )

        thirdRecyclerView.adapter =
            ImageAdapter(
                imageDataset.shuffled()
            )
        thirdRecyclerView.layoutManager!!.scrollToPosition(
            Integer.MAX_VALUE / 2
        )

        firstRecyclerView.setAutomaticScroll()
        secondRecyclerView.setAutomaticScroll(
            ScrollView.FOCUS_UP
        )
        thirdRecyclerView.setAutomaticScroll()
    }

    private fun intentsButtons() {
        binding.btnSignIn.setOnClickListener {
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