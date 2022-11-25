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
import com.autio.android_app.makeLinks
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

    private lateinit var dialogBinding: MothPopupBinding
    private lateinit var dialog: Dialog

    private lateinit var binding: ActivitySubscribeBinding
    private lateinit var sliderView: SliderView
    private lateinit var sliderAdapter: SliderAdapter
    private val textListTitle =
        arrayListOf(
            "Take your journey to the next level\nwith location-based audio stories",
            "Go anywhere with nationwide\ncoverage",
            "Always more to discover with new\noriginal content",
            "A collection of narrators as unique as\nthe stories"
        )
    private val textList =
        arrayListOf(
            "Explore our collection of over 8,500+ stories\nexclusive to HearHere.",
            "Road trip across the country with new stories to\ndiscover wherever you travel.",
            "New, unique stories added weekly.",
            "Listen to some of your favorite voices, like Kevin\nCostner, Phil Jackson, & John Lithgow."
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

        sliderView =
            binding.imageSliderSubscribe

        sliderAdapter =
            SliderAdapter(
                textListTitle,
                textList
            )
        sliderView.autoCycleDirection =
            SliderView.LAYOUT_DIRECTION_LTR
        sliderView.setSliderAdapter(
            sliderAdapter
        )
        sliderView.scrollTimeInSec =
            3
        sliderView.isAutoCycle =
            true
        sliderView.startAutoCycle()

        setListeners()
    }

    private fun setListeners() {
        binding.tvMothInvitation.makeLinks(
            Pair(
                "Learn more",
                View.OnClickListener {
                    dialogBinding =
                        MothPopupBinding.inflate(
                            layoutInflater
                        )
                    dialog =
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