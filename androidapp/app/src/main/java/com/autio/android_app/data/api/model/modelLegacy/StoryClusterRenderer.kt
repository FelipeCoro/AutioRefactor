package com.autio.android_app.data.api.model.modelLegacy

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.autio.android_app.R
import com.autio.android_app.data.api.model.pendings.StoryClusterItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer


class StoryClusterRenderer(
    private val context: Context, map: GoogleMap,
    clusterManager: ClusterManager<StoryClusterItem>
) : DefaultClusterRenderer<StoryClusterItem>(context, map, clusterManager) {

    private val defaultMarkerIcon by lazy {
        ResourcesCompat.getDrawable(
            context.resources, R.drawable.ic_non_listened_pin, null
        )?.toBitmap()
    }

    private val listenedMarkerIcon by lazy {
        ResourcesCompat.getDrawable(
            context.resources, R.drawable.ic_listened_pin, null
        )?.toBitmap()
    }

    private val currentPlayingMarkerIcon by lazy {
        ResourcesCompat.getDrawable(
            context.resources, R.drawable.ic_now_listening_pin, null
        )?.toBitmap()
    }

    override fun getClusterTextAppearance(clusterSize: Int): Int {
        return R.style.ClusterMapStyle
    }

    /**
     * Method called before the cluster item (i.e. the marker) is rendered. This is where marker
     * options should be set
     */
    override fun onBeforeClusterItemRendered(
        item: StoryClusterItem, markerOptions: MarkerOptions
    ) {

        val markerBitmap: Bitmap? =
            if (item.story.listenedAtLeast30Secs == true) {
                listenedMarkerIcon
            } else {
                defaultMarkerIcon
            }

        if (markerBitmap != null) {
            item.bitmap = markerBitmap
            val pinIcon = BitmapDescriptorFactory.fromBitmap(markerBitmap)
            markerOptions.icon(pinIcon)
        }
    }

    /**
     * Method called right after the cluster item (i.e. the marker) is rendered. This is where
     * properties for the Marker object should be set.
     */
    override fun onClusterItemRendered(
        item: StoryClusterItem, marker: Marker
    ) {
        super.onClusterItemRendered(item, marker)
        marker.tag = item
        item.marker = marker

        if (item.story.listenedAtLeast30Secs == true) {
            listenedMarkerIcon?.let {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(it))
            }
        }
    }

    override fun shouldRenderAsCluster(cluster: Cluster<StoryClusterItem>): Boolean {
        return cluster.size > 5
    }
}
