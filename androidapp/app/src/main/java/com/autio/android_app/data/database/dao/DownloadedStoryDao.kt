package com.autio.android_app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedStoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addStory(story: DownloadedStoryEntity): Long

    @Query("SELECT * FROM downloaded_stories ORDER BY id ASC")
    fun readLiveStories(): Flow<List<DownloadedStoryEntity>>

    @Query("SELECT * FROM downloaded_stories WHERE id = :id")
    suspend fun getStoryById(id: Int): DownloadedStoryEntity?

    @Query("UPDATE downloaded_stories SET isBookmarked = 1 WHERE id = :id")
    fun setBookmarkToStory(id: Int)

    @Query("UPDATE downloaded_stories SET isBookmarked = 0 WHERE id = :id")
    fun removeBookmarkFromStory(id: Int)

    @Query("UPDATE downloaded_stories SET isBookmarked = 0")
    fun removeAllBookmarks()

    @Query("UPDATE downloaded_stories SET isLiked = 1 WHERE id = :id")
    fun setLikeToStory(id: Int)

    @Query("UPDATE downloaded_stories SET isLiked = 0 WHERE id = :id")
    fun removeLikeFromStory(id: Int)

    @Query("UPDATE downloaded_stories SET listenedAt = :listenedAt WHERE id = :storyId")
    fun setListenedAtData(storyId: Int, listenedAt: String)

    @Query("UPDATE downloaded_stories SET listenedAtLeast30Secs = true WHERE id = :storyId")
    fun markStoryAsListenedAtLeast30Secs(storyId: Int)

    @Query("UPDATE downloaded_stories SET listenedAt = '' WHERE id = :storyId")
    fun removeListenedAtData(storyId: Int)

    @Query("UPDATE downloaded_stories SET listenedAt = ''")
    fun clearStoryHistory()

    @Query("DELETE FROM downloaded_stories WHERE id = :id")
    fun removeStory(id: Int)

    @Query("DELETE FROM downloaded_stories")
    fun clearTable()
}
