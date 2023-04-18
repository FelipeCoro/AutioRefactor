package com.autio.android_app.data

import com.autio.android_app.R
import com.autio.android_app.data.api.model.modelLegacy.LocationView


class Datasource {
    fun loadLocationViews(): List<LocationView> {
        return listOf(
            LocationView(R.drawable.golden_gate_bridge),
            LocationView(R.drawable.pyramid_lake),
            LocationView(R.drawable.sand_harbor),
            LocationView(R.drawable.valle_of_fire),
            LocationView(R.drawable.viviana_rishe),
            LocationView(R.drawable.sg9),
            LocationView(R.drawable.sg10),
            LocationView(R.drawable.sg11),
            LocationView(R.drawable.sg12),
            LocationView(R.drawable.sg13),
            LocationView(R.drawable.sg14),
            LocationView(R.drawable.sg15),
            LocationView(R.drawable.sg16),
            LocationView(R.drawable.sg17),
            LocationView(R.drawable.sg18),
            LocationView(R.drawable.sg19),
            LocationView(R.drawable.sg20),
            LocationView(R.drawable.sg21),
        )
    }
}
