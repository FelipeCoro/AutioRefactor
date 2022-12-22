package com.autio.android_app.data.model.story

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import com.autio.android_app.R
import com.autio.android_app.util.Utils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class StoryClusterRenderer(
    val context: Context,
    val map: GoogleMap,
    clusterManager: ClusterManager<StoryClusterItem>
) : DefaultClusterRenderer<StoryClusterItem>(
    context,
    map,
    clusterManager
) {
    override fun shouldRenderAsCluster(
        cluster: Cluster<StoryClusterItem>
    ) = cluster.size > 1

    override fun onBeforeClusterItemRendered(
        item: StoryClusterItem,
        markerOptions: MarkerOptions
    ) {
        super.onBeforeClusterItemRendered(
            item,
            markerOptions
        )
        val pinDrawable =
            ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.ic_non_listened_pin,
                null
            )
        val pinIcon =
            Utils.getIconFromDrawable(
                pinDrawable
            )
        markerOptions.icon(
            pinIcon
        )
    }
}