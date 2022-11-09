package com.autio.android_app.ui.view.usecases.subscribe

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.autio.android_app.data.repository.PrefRepository
import com.autio.android_app.databinding.ActivitySubscribeBinding
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
    private lateinit var textListTitle: ArrayList<String>
    private lateinit var textList: ArrayList<String>
    private lateinit var sliderView: SliderView
    private lateinit var sliderAdapter: SliderAdapter

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

        sliderView =
            binding.imageSliderSubscribe

        textListTitle =
            getTitleList()
        textList =
            getTextListSub()

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

        binding.btnStoriesFree1.setOnClickListener {
            isSessionAlive()
        }

        binding.btnStoriesFree2.setOnClickListener {
            isSessionAlive()
        }

    }

    private fun isSessionAlive() {
        val isSessionAlive =
            prefRepository.getUserApiToken()

        if (isSessionAlive.isEmpty()) {
            goToLoginActivity()
        } else {
            getToMainMenu()
        }
    }

    private fun getToMainMenu() {
        startActivity(
            Intent(
                this,
                BottomNavigation::class.java
            )
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

    private fun getTitleList(): ArrayList<String> {
        textListTitle =
            ArrayList()
        textListTitle =
            (textListTitle + "Take your journey to the next level\nwith location-based audio stories") as ArrayList<String>
        textListTitle =
            (textListTitle + "Go anywhere with nationwide\ncoverage") as ArrayList<String>
        textListTitle =
            (textListTitle + "Always more to discover with new\noriginal content") as ArrayList<String>
        textListTitle =
            (textListTitle + "A collection of narrators as unique as\nthe stories") as ArrayList<String>

        return textListTitle
    }

    private fun getTextListSub(): ArrayList<String> {
        textList =
            ArrayList()
        textList =
            (textList + "Explore our collection of over 8,500+ stories\nexclusive to HearHere.") as ArrayList<String>
        textList =
            (textList + "Road trip across the country with new stories to\ndiscover wherever you travel.") as ArrayList<String>
        textList =
            (textList + "New, unique stories added weekly.") as ArrayList<String>
        textList =
            (textList + "Listen to some of your favorite voices, like Kevin\nCostner, Phil Jackson, & John Lithgow.") as ArrayList<String>

        return textList
    }

}