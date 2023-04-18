package com.autio.android_app.data.api.model.pendings

import android.graphics.Bitmap
import com.autio.android_app.ui.stories.models.Story
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterItem

data class StoryClusterItem(var story: Story, var marker: Marker? = null) : ClusterItem {

    private val position: LatLng = LatLng(story.lat, story.lng)
    private val title: String = story.title
    private val snippet: String = ""
    var bitmap: Bitmap? = null

    fun updateStory(story: Story) {
        if (story.id != this.story.id) {
            throw Exception("Story is not the same!")
        }
        this.story = story
    }

    override fun getPosition() = position
    override fun getTitle(): String? = title
    override fun getSnippet(): String? = snippet
    override fun getZIndex(): Float? = 0f


    override fun toString(): String {
        return "StoryClusterItem(position=$position, title='$title', snippet='$snippet', story=$story, marker=$marker, bitmap=$bitmap)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StoryClusterItem

        if (story.id != other.story.id) return false

        return true
    }
}
