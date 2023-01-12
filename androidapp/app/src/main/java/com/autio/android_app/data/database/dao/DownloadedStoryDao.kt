package com.autio.android_app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.autio.android_app.data.model.story.DownloadedStory
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedStoryDao {
    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    fun addStory(
        story: DownloadedStory
    ): Long

    @Query(
        "SELECT * FROM downloaded_stories ORDER BY id ASC"
    )
    fun readLiveStories(): Flow<List<DownloadedStory>>

    @Query(
        "SELECT * FROM downloaded_stories WHERE id = :id"
    )
    suspend fun getStoryById(
        id: String
    ): DownloadedStory?

    @Query(
        "UPDATE downloaded_stories SET isBookmarked = 1 WHERE id = :id"
    )
    fun setBookmarkToStory(
        id: String
    )

    @Query(
        "UPDATE downloaded_stories SET isBookmarked = 0 WHERE id = :id"
    )
    fun removeBookmarkFromStory(
        id: String
    )

    @Query(
        "UPDATE downloaded_stories SET isLiked = 1 WHERE id = :id"
    )
    fun setLikeToStory(
        id: String
    )

    @Query(
        "UPDATE downloaded_stories SET isLiked = 0 WHERE id = :id"
    )
    fun removeLikeFromStory(
        id: String
    )

    @Query(
        "UPDATE downloaded_stories SET listenedAt = :listenedAt WHERE id = :storyId"
    )
    fun setListenedAtData(
        storyId: String,
        listenedAt: String
    )

    @Query(
        "UPDATE downloaded_stories SET listenedAt = '' WHERE id = :storyId"
    )
    fun removeListenedAtData(
        storyId: String
    )

    @Query(
        "UPDATE downloaded_stories SET listenedAt = ''"
    )
    fun clearStoryHistory()

    @Query(
        "DELETE FROM downloaded_stories WHERE id = :id"
    )
    fun removeStory(
        id: String
    )
}