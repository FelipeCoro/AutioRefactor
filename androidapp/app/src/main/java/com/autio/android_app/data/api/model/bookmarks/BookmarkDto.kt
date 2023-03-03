package com.autio.android_app.data.api.model.bookmarks

data class BookmarkDto(
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
