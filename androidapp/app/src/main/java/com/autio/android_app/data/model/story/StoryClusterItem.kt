package com.autio.android_app.data.model.story

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class StoryClusterItem(
    private val story: Story
) : ClusterItem {

    private val position: LatLng =
        LatLng(story.lat, story.lon)
    private val title: String =
        story.title
    private val snippet: String = ""

    fun getStory() = story

    override fun getPosition() = position

    override fun getTitle(): String? = null

    override fun getSnippet(): String? = null

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