package com.autio.android_app.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.autio.android_app.data.database.entities.StoryEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface StoryDao {

    //TODO(Check this)
    @Transaction
    fun setBookmarksData(storiesIds: List<String>) {
        setBookmarksToStories(storiesIds)
        removeBookmarksFromStories(storiesIds)
    }

    @Transaction
    fun setLikesData(storiesIds: List<String>) {
        setLikesToStories(storiesIds)
        removeLikesFromStories(storiesIds)
    }

    @Query("UPDATE story SET isBookmarked = 1 WHERE id = :id")
    fun setBookmarkToStory(id: Int)

    @Query("UPDATE story SET listenedAt = :listenedAt WHERE id = :storyId")
    fun setListenedAtData(storyId: Int, listenedAt: String)

    @Query("UPDATE story SET listenedAtLeast30Secs = true WHERE id = :storyId")
    fun markStoryAsListenedAtLeast30Secs(storyId: Int)

    @Query("UPDATE story SET listenedAt = '' WHERE id = :storyId")
    fun removeListenedAtData(storyId: Int)

    @Query("SELECT * FROM story WHERE listenedAt != '' ORDER BY listenedAt DESC")
    fun getHistory(): Flow<List<StoryEntity>>

    @Query("SELECT * FROM story WHERE isLiked = 1")//TODO(Turn this into boolean?)
    fun getFavoriteStories(): Flow<List<StoryEntity>>

    @Query("UPDATE story SET isBookmarked = 0")
    fun removeAllBookmarks()

    @Query("UPDATE story SET isBookmarked = 0 WHERE id NOT IN (:ids)")
    fun removeBookmarksFromStories(ids: List<String>)

    @Query("UPDATE story SET isBookmarked = 0 WHERE id = :id")
    fun removeBookmarkFromStory(id: Int)

    @Query("SELECT * FROM story WHERE isBookmarked = 1")
    fun getBookmarkedStories(): Flow<List<StoryEntity>>

    @Query("SELECT * FROM story WHERE modifiedDate = (SELECT MAX(modifiedDate) FROM story)")
    suspend fun readLastModifiedStory(): StoryEntity?

    @Query("UPDATE story SET isBookmarked = 1 WHERE id IN (:ids)")
    fun setBookmarksToStories(ids: List<String>)

    @Query("UPDATE story SET listenedAt = ''")
    fun clearStoryHistory()

    @Query("UPDATE story SET isLiked = 1 WHERE id IN (:ids)")
    fun setLikesToStories(ids: List<String>)

    @Query("UPDATE story SET isLiked = 1 WHERE id = :id")
    fun setLikeToStory(id: Int): Int

    @Query("UPDATE story SET isLiked = 0 WHERE id NOT IN (:ids)")
    fun removeLikesFromStories(ids: List<String>): Int

    @Query("UPDATE story SET isLiked = 0 WHERE id = :id")
    fun removeLikeFromStory(id: Int)

    @Query("UPDATE story SET recordUrl = :recordUrl WHERE id = :id")
    fun addRecordOfStory(id: String, recordUrl: String)

    @Query("UPDATE story SET recordUrl = :recordUrl WHERE id = :id")
    fun addRecordOfStory(id: Int, recordUrl: String)

    @Query("UPDATE story SET isLiked = 0, isBookmarked = 0, isDownloaded = 0, listenedAt = '', listenedAtLeast30Secs = false")
    fun clearUserData()

    @Query("UPDATE story SET recordUrl = ''")
    fun deleteRecordUrls()

    @Query("SELECT * FROM story ORDER BY id ASC")
    fun readLiveStories(): Flow<List<StoryEntity>>
}
