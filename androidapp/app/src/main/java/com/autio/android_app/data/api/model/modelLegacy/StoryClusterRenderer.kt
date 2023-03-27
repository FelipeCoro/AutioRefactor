package com.autio.android_app.data.api.model.modelLegacy

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.autio.android_app.R
import com.autio.android_app.data.api.model.pendings.StoryClusterItem
import com.autio.android_app.ui.stories.fragments.MapFragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer


class StoryClusterRenderer(
    private val context: Context, map: GoogleMap,
    clusterManager: ClusterManager<StoryClusterItem>,
    private val mapFragment: MapFragment
) : DefaultClusterRenderer<StoryClusterItem>(context, map, clusterManager) {

    var nearestStory: StoryClusterItem? = null


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

    private val largerBitmapDefault by lazy {
        defaultMarkerIcon?.let {
            getLargerBitmap(it)
        }
    }

    private val largerBitmapListened by lazy {
        listenedMarkerIcon?.let {
            getLargerBitmap(it)
        }
    }

    override fun getColor(clusterSize: Int): Int {
        return ResourcesCompat.getColor(context.resources, R.color.autio_blue, null)
    }

    override fun getClusterTextAppearance(clusterSize: Int): Int {
        return R.style.ClusterMapStyle
    }

    override fun getMinClusterSize(): Int {
        return super.getMinClusterSize()
    }
    /**
     * Method called before the cluster item (i.e. the marker) is rendered. This is where marker
     * options should be set
     */
    override fun onBeforeClusterItemRendered(
        item: StoryClusterItem, markerOptions: MarkerOptions,
    ) {
        var markerBitmap: Bitmap? =
            if (item.story.listenedAtLeast30Secs == true) {
                listenedMarkerIcon
            } else {
                defaultMarkerIcon
            }

        if (markerBitmap != null) {
            item.bitmap = markerBitmap


            nearestStory?.let {
                if (nearestStory == item) {
                    //TODO(HERE LOGIC HIGHLIGHT MARKER / NEAREST POINT)
                    item.bitmap = getBitmap(item.bitmap)
                    markerBitmap = item.bitmap
                    mapFragment.tapClusterItem(it)
                }
            }
            val pinIcon = BitmapDescriptorFactory.fromBitmap(markerBitmap!!)
            markerOptions.icon(pinIcon)
        }
    }

    /**
     * Method called right after the cluster item (i.e. the marker) is rendered. This is where
     * properties for the Marker object should be set.
     */
    override fun onClusterItemRendered(item: StoryClusterItem, marker: Marker) {
        super.onClusterItemRendered(item, marker)
        marker.tag = item
        item.marker = marker
    }

    private fun getBitmap(bitmap: Bitmap?): Bitmap? {
        return when (bitmap) {
            defaultMarkerIcon -> largerBitmapDefault
            listenedMarkerIcon -> largerBitmapListened
            else -> defaultMarkerIcon
        }
    }

    private fun getLargerBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width * 1.5
        val height = bitmap.height * 1.5F
        return Bitmap.createScaledBitmap(bitmap, width.toInt(), height.toInt(), false)
    }

    private fun getOriginalBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }


    override fun shouldRenderAsCluster(cluster: Cluster<StoryClusterItem>): Boolean {
        return cluster.size > 4
    }
}
