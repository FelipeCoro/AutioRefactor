package com.autio.android_app.data.database.dao

import androidx.room.*
import com.autio.android_app.data.model.story.Story
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    fun addStories(
        story: Array<Story>
    ): Array<Long>

    @Query(
        "SELECT * FROM stories"
    )
    suspend fun allStories(): List<Story>

    @Query(
        "SELECT * FROM stories ORDER BY id ASC"
    )
    fun readLiveStories(): Flow<List<Story>>

    @Query(
        "SELECT * FROM stories WHERE " +
                "CASE WHEN :lat1 < :lat2 THEN lat BETWEEN :lat1 AND :lat2 ELSE lat BETWEEN :lat2 AND :lat1 END" +
                " AND " +
                "CASE WHEN :lng1 < :lng2 THEN lon BETWEEN :lng1 AND :lng2 ELSE lon BETWEEN :lng2 AND :lng1 END"
    )
    suspend fun getStoriesInLatLngBoundaries(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): List<Story>

    @Query(
        "SELECT * FROM stories"
    )
    suspend fun readStories(): List<Story>

    @Query(
        "SELECT * FROM stories WHERE originalId IN (:ids)"
    )
    fun readStoriesWithIds(
        ids: Array<Int>
    ): Flow<Array<Story>>

    @Query(
        "SELECT * FROM stories WHERE id = (:id)"
    )
    suspend fun getStoryById(
        id: String
    ): Story?

    @Query(
        "SELECT * FROM stories WHERE modifiedDate = (SELECT MAX(modifiedDate) FROM stories)"
    )
    suspend fun readLastModifiedStory(): Story?

    @Query(
        "DELETE FROM stories"
    )
    fun deleteAllStories()

    @Query(
        "SELECT * FROM stories WHERE isBookmarked = 1"
    )
    fun getBookmarkedStories(): Flow<List<Story>>

    @Transaction
    fun setBookmarksData(
        storiesIds: List<String>
    ) {
        setBookmarksToStories(
            storiesIds
        )
        removeBookmarksFromStories(
            storiesIds
        )
    }

    @Query(
        "UPDATE stories SET isBookmarked = 1 WHERE id IN (:ids)"
    )
    fun setBookmarksToStories(
        ids: List<String>
    )

    @Query(
        "UPDATE stories SET isBookmarked = 1 WHERE id = :id"
    )
    fun setBookmarkToStory(
        id: String
    )

    @Query(
        "UPDATE stories SET isBookmarked = 0 WHERE id NOT IN (:ids)"
    )
    fun removeBookmarksFromStories(
        ids: List<String>
    )

    @Query(
        "UPDATE stories SET isBookmarked = 0 WHERE id = :id"
    )
    fun removeBookmarkFromStory(
        id: String
    )

    @Query(
        "UPDATE stories SET isBookmarked = 0"
    )
    fun removeAllBookmarks()

    @Query(
        "SELECT * FROM stories WHERE isLiked = 1"
    )
    fun getFavoriteStories(): Flow<List<Story>>

    @Transaction
    fun setLikesData(
        storiesIds: List<String>
    ) {
        setLikesToStories(
            storiesIds
        )
        removeLikesFromStories(
            storiesIds
        )
    }

    @Query(
        "SELECT * FROM stories WHERE listenedAt != '' ORDER BY listenedAt DESC"
    )
    fun getHistory(): Flow<List<Story>>

    @Query(
        "UPDATE stories SET listenedAt = :listenedAt WHERE id = :storyId"
    )
    fun setListenedAtData(
        storyId: String,
        listenedAt: String
    )

    @Query(
        "UPDATE stories SET listenedAtLeast30Secs = true WHERE id = :storyId"
    )
    fun markStoryAsListenedAtLeast30Secs(
        storyId: String
    )

    @Query(
        "UPDATE stories SET listenedAt = '' WHERE id = :storyId"
    )
    fun removeListenedAtData(
        storyId: String
    )

    @Query(
        "UPDATE stories SET listenedAt = ''"
    )
    fun clearStoryHistory()

    @Query(
        "UPDATE stories SET isLiked = 1 WHERE id IN (:ids)"
    )
    fun setLikesToStories(
        ids: List<String>
    )

    @Query(
        "UPDATE stories SET isLiked = 1 WHERE id = :id"
    )
    fun setLikeToStory(
        id: String
    ): Int

    @Query(
        "UPDATE stories SET isLiked = 0 WHERE id NOT IN (:ids)"
    )
    fun removeLikesFromStories(
        ids: List<String>
    ): Int

    @Query(
        "UPDATE stories SET isLiked = 0 WHERE id = :id"
    )
    fun removeLikeFromStory(
        id: String
    )

    @Query(
        "UPDATE stories SET recordUrl = :recordUrl WHERE id = :id"
    )
    fun addRecordOfStory(
        id: String,
        recordUrl: String
    )

    @Query(
        "UPDATE stories SET recordUrl = :recordUrl WHERE originalId = :id"
    )
    fun addRecordOfStory(
        id: Int,
        recordUrl: String
    )

    @Query(
        "UPDATE stories SET isLiked = 0, isBookmarked = 0, isDownloaded = 0, listenedAt = '', listenedAtLeast30Secs = false"
    )
    fun clearUserData()

    @Query(
        "UPDATE stories SET recordUrl = ''"
    )
    fun deleteRecordUrls()
}