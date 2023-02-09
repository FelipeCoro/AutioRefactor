package com.autio.android_app.data.model.story

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterItem

class StoryClusterItem(
    mStory: Story
) : ClusterItem {

    private val position: LatLng =
        LatLng(
            mStory.lat,
            mStory.lon
        )
    private val title: String =
        mStory.title
    private val snippet: String =
        ""

    var story =
        mStory
        private set
    var marker: Marker? =
        null
    var bitmap: Bitmap? =
        null

    fun updateStory(
        story: Story
    ) {
        if (story.id != this.story.id) {
            throw Exception(
                "Story is not the same!"
            )
        }
        this.story =
            story
    }

    override fun getPosition() =
        position

    override fun getTitle(): String? =
        null

    override fun getSnippet(): String? =
        null

    override fun toString(): String {
        return """
            StoryClusterItem: {
                position: $position,
                title: $title,
                snippet: $snippet
            }
        """.trimIndent()
    }
}