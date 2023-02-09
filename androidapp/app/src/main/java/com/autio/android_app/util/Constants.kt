package com.autio.android_app.util

import com.autio.android_app.R


object Constants {
    const val USER_PREFERENCES =
        "USER_PREFERENCES"
    const val ON_BOARDING_FINISHED =
        "ON_BOARDING_FINISHED"
    const val USER_ID =
        "USER_ID"
    const val USER_NAME =
        "USER_NAME"
    const val USER_EMAIL =
        "USER_EMAIL"
    const val USER_API_TOKEN =
        "USER_API_TOKEN"
    const val USER_IS_GUEST =
        "USER_IS_GUEST"
    const val USER_FIREBASE_KEY =
        "USER_FIREBASE_KEY"
    const val REMAINING_STORIES =
        "REMAINING_STORIES"

    val categoryIcons =
        hashMapOf(
            "HISTORY" to R.drawable.ic_category_history,
            "LOCAL INSIGHTS" to R.drawable.ic_category_local_insights,
            "COLORFUL CHARACTERS" to R.drawable.ic_category_colorful_characters,
            "CULTURE" to R.drawable.ic_category_culture,
            "NATURAL WONDERS" to R.drawable.ic_category_natural_wonders,
            "SPECIAL PLACES OF INTEREST" to R.drawable.ic_category_special_places,
            "SPORTS" to R.drawable.ic_category_sports,
            "MUSIC" to R.drawable.ic_category_music
        )

    // In-App Products
    // The following SKU strings must match the ones we have in the Google Play developer console.
    // SKUs for non-subscription purchases
    const val SINGLE_TRIP_PRODUCT =
        "autio_single_trip_30d_one_time"
    const val ADVENTURER_TRIP_PRODUCT =
        "autio_adventurer_3y_one_time"
    const val TRAVELER_TRIP_SUBSCRIPTION =
        "autio_traveler_sub_1y_cancellable"

    const val REVENUE_CAT_ENTITLEMENT =
        "Unlimited Stories"
}

const val DEFAULT_LOCATION_LAT =
    34.420830
const val DEFAULT_LOCATION_LNG =
    -119.698189