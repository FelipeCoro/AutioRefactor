package com.autio.android_app.data.model.story

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.autio.android_app.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class StoryClusterRenderer(
    val context: Context,
    val map: GoogleMap,
    clusterManager: ClusterManager<StoryClusterItem>,
    currentZoomLevel: Float,
    private val maxZoomLevel: Float
) : DefaultClusterRenderer<StoryClusterItem>(
    context,
    map,
    clusterManager
),
    GoogleMap.OnCameraMoveListener {
    private var currentZoomLevel: Float

    init {
        this.currentZoomLevel =
            currentZoomLevel
    }

    override fun shouldRenderAsCluster(
        cluster: Cluster<StoryClusterItem>
    ) =
        super.shouldRenderAsCluster(
            cluster
        ) && currentZoomLevel < maxZoomLevel

    override fun onBeforeClusterItemRendered(
        item: StoryClusterItem,
        markerOptions: MarkerOptions
    ) {
        super.onBeforeClusterItemRendered(
            item,
            markerOptions
        )

        val markerBitmap =
            ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.ic_non_listened_pin,
                null
            )
                ?.toBitmap()
        if (markerBitmap != null) {
            val pinIcon =
                BitmapDescriptorFactory.fromBitmap(
                    markerBitmap
                )
            markerOptions.icon(
                pinIcon
            )
        }
    }

    override fun onCameraMove() {
        currentZoomLevel =
            map.cameraPosition.zoom
    }
}