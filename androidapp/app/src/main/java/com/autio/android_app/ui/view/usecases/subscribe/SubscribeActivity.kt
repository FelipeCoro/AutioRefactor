package com.autio.android_app.ui.view.usecases.subscribe

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.autio.android_app.data.repository.PrefRepository
import com.autio.android_app.databinding.ActivitySubscribeBinding
import com.autio.android_app.databinding.MothPopupBinding
import com.autio.android_app.extensions.makeLinks
import com.autio.android_app.ui.view.usecases.home.BottomNavigation
import com.autio.android_app.ui.view.usecases.login.LoginActivity
import com.smarteist.autoimageslider.SliderView

class SubscribeActivity :
    AppCompatActivity() {
    private val prefRepository by lazy {
        PrefRepository(
            this
        )
    }

    private lateinit var binding: ActivitySubscribeBinding
    private val textListTitle =
        arrayListOf(
            "Take your journey to the next level with location-based audio stories",
            "Go anywhere with nationwide coverage",
            "Always more to discover with new original content",
            "A collection of narrators as unique as the stories"
        )
    private val textList =
        arrayListOf(
            "Explore our collection of over 10,000+ stories exclusive to Autio.",
            "Road trip across the country with new stories to discover wherever you travel.",
            "New, unique stories added weekly.",
            "Listen to some of your favorite voices, like Kevin Costner, Phil Jackson, & John Lithgow."
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
        binding =
            ActivitySubscribeBinding.inflate(
                layoutInflater
            )
        setContentView(
            binding.root
        )

        binding.tvMothInvitation.movementMethod =
            LinkMovementMethod.getInstance()

        binding.imageSliderSubscribe.apply {
            autoCycleDirection =
                SliderView.LAYOUT_DIRECTION_LTR
            setSliderAdapter(
                SliderAdapter(
                    textListTitle,
                    textList
                )
            )
            scrollTimeInSec =
                6
            isAutoCycle =
                true
            setInfiniteAdapterEnabled(
                true
            )
            startAutoCycle()
        }

        setListeners()
    }

    private fun setListeners() {
        if (prefRepository.userApiToken.isEmpty() || prefRepository.firebaseKey.isEmpty()) {
            binding.tvSignIn.setOnClickListener {
                goToLoginActivity()
            }
        } else {
            binding.tvSignIn.visibility =
                View.GONE
        }
        binding.tvMothInvitation.makeLinks(
            Pair(
                "Learn more",
                View.OnClickListener {
                    val dialogBinding =
                        MothPopupBinding.inflate(
                            layoutInflater
                        )
                    val dialog =
                        Dialog(
                            this
                        )
                    dialog.window?.setBackgroundDrawable(
                        ColorDrawable(
                            Color.TRANSPARENT
                        )
                    )
                    dialog.setContentView(
                        dialogBinding.root
                    )
                    dialogBinding.btnBack.setOnClickListener {
                        dialog.dismiss()
                    }
                    dialogBinding.btnLearnMore.setOnClickListener {
                        val intent =
                            Intent(
                                Intent.ACTION_VIEW
                            )
                        intent.data =
                            Uri.parse(
                                "https://themoth.org/community"
                            )
                        startActivity(
                            intent
                        )
                    }
                    dialog.show()
                })
        )

        binding.btnChoosePlan.setOnClickListener {
            binding.root.smoothScrollTo(
                0,
                binding.cardView3month.scrollY
            )
        }

        binding.btnStoriesFree1.setOnClickListener {
            isSessionAlive()
        }

        binding.btnStoriesFree2.setOnClickListener {
            isSessionAlive()
        }
    }

    private fun isSessionAlive() {
        val isSessionAlive =
            prefRepository.userApiToken

        if (isSessionAlive.isEmpty()) {
            goToLoginActivity()
        } else {
            getToMainMenu()
        }
    }

    private fun getToMainMenu() {
        val intent =
            Intent(
                this,
                BottomNavigation::class.java
            )
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
        startActivity(
            intent
        )
        finish()
    }

    private fun goToLoginActivity() {
        startActivity(
            Intent(
                this,
                LoginActivity::class.java
            )
        )
        finish()
    }
}