package com.autio.android_app.data.model.Interest

object InterestProvider {

    fun getInterests(): List<InterestModel> {
        return interest
    }

    private val interest = listOf(
        InterestModel("History"),
        InterestModel("Local Insights"),
        InterestModel("Colorful Characters"),
        InterestModel("Culture"),
        InterestModel("Natural Wonders"),
        InterestModel("Special Places of Interest"),
        InterestModel("Sports"),
        InterestModel("Music")
    )
}