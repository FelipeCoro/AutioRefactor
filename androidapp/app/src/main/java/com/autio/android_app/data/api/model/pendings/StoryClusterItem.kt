package com.autio.android_app.data.api.model.pendings

import android.graphics.Bitmap
import com.autio.android_app.data.database.entities.MapPoint
import com.autio.android_app.ui.stories.models.Story
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterItem

class StoryClusterItem(
    mStoryDto: MapPoint
) : ClusterItem {

    private val position: LatLng =
        LatLng(
            mStoryDto.lat,
            mStoryDto.lon
        )
    private val title: String =
        mStoryDto.title
    private val snippet: String =
        ""

    var story =
        mStoryDto
        private set
    var marker: Marker? =
        null
    var bitmap: Bitmap? =
        null

    fun updateStory(
        storyDto: Story
    ) {
        if (storyDto.id != this.story.id) {
            throw Exception(
                "Story is not the same!"
            )
        }
        this.story =
            storyDto
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
