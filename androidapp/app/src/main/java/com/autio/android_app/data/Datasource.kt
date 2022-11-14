package com.autio.android_app.data

import com.autio.android_app.R
import com.autio.android_app.data.model.design.LocationView

class Datasource {
    fun loadLocationViews(): List<LocationView> {
        return listOf(
            LocationView(
                R.drawable.golden_gate_bridge
            ),
            LocationView(
                R.drawable.pyramid_lake
            ),
            LocationView(
                R.drawable.sand_harbor
            ),
            LocationView(
                R.drawable.valle_of_fire
            ),
            LocationView(
                R.drawable.viviana_rishe
            ),
        )
    }
}