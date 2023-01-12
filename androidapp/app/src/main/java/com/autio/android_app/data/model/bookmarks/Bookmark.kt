package com.autio.android_app.data.model.bookmarks

data class Bookmark(
    val storyId: String,
    val isOwn: String,
    val title: String
) {
    override fun toString(): String {
        return """
            Bookmark: {
                storyId: $storyId,
                isOwn: $isOwn,
                title: $title
            }
        """.trimIndent()
    }
}