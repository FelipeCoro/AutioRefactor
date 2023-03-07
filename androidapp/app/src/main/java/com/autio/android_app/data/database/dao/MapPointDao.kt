package com.autio.android_app.data.database.dao

import androidx.room.*
import com.autio.android_app.data.database.entities.MapPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MapPointDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addStories(story: List<MapPointEntity>): List<Long>

    @Query("SELECT * FROM stories")
    suspend fun allStories(): List<MapPointEntity>?

    @Query("SELECT * FROM stories ORDER BY id ASC")
    fun readLiveStories(): Flow<List<MapPointEntity>>

    @Query("SELECT * FROM stories WHERE " + "CASE WHEN :lat1 < :lat2 THEN lat BETWEEN :lat1 AND :lat2 ELSE lat BETWEEN :lat2 AND :lat1 END" + " AND " + "CASE WHEN :lng1 < :lng2 THEN lon BETWEEN :lng1 AND :lng2 ELSE lon BETWEEN :lng2 AND :lng1 END")
    suspend fun getStoriesInLatLngBoundaries(
        lat1: Double, lng1: Double, lat2: Double, lng2: Double
    ): List<MapPointEntity>

    @Query("SELECT * FROM stories")
    suspend fun readStories(): List<MapPointEntity>

    @Query("SELECT * FROM stories WHERE originalId IN (:ids)")
    fun readStoriesWithIds(ids: List<Int>): Flow<List<MapPointEntity>>

    @Query("SELECT * FROM stories WHERE id = (:id)")
    suspend fun getMapPointById(id: String): MapPointEntity?

    @Query("SELECT * FROM stories WHERE modifiedDate = (SELECT MAX(modifiedDate) FROM stories)")
    suspend fun readLastModifiedStory(): MapPointEntity?

    @Query("DELETE FROM stories")
    fun deleteAllStories()

    @Query("SELECT * FROM stories WHERE isBookmarked = 1")
    fun getBookmarkedStories(): Flow<List<MapPointEntity>>

    @Transaction
    fun setBookmarksData(storiesIds: List<String>) {
        setBookmarksToStories(storiesIds)
        removeBookmarksFromStories(storiesIds)
    }

    @Query("UPDATE stories SET isBookmarked = 1 WHERE id IN (:ids)")
    fun setBookmarksToStories(ids: List<String>)

    @Query("UPDATE stories SET isBookmarked = 1 WHERE id = :id")
    fun setBookmarkToStory(id: Int)

    @Query("UPDATE stories SET isBookmarked = 0 WHERE id NOT IN (:ids)")
    fun removeBookmarksFromStories(ids: List<String>)

    @Query("UPDATE stories SET isBookmarked = 0 WHERE id = :id")
    fun removeBookmarkFromStory(id: Int)

    @Query("UPDATE stories SET isBookmarked = 0")
    fun removeAllBookmarks()

    @Query("SELECT * FROM stories WHERE isLiked = 1")//TODO(Turn this into boolean?)
    fun getFavoriteStories(): Flow<List<MapPointEntity>>

    @Transaction
    fun setLikesData(storiesIds: List<String>) {
        setLikesToStories(storiesIds)
        removeLikesFromStories(storiesIds)
    }

    @Query("SELECT * FROM stories WHERE listenedAt != '' ORDER BY listenedAt DESC")
    fun getHistory(): Flow<List<MapPointEntity>>

    @Query("UPDATE stories SET listenedAt = :listenedAt WHERE id = :storyId")
    fun setListenedAtData(storyId: Int, listenedAt: String)

    @Query("UPDATE stories SET listenedAtLeast30Secs = true WHERE id = :storyId")
    fun markStoryAsListenedAtLeast30Secs(storyId: Int)

    @Query("UPDATE stories SET listenedAt = '' WHERE id = :storyId")
    fun removeListenedAtData(storyId: Int)

    @Query("UPDATE stories SET listenedAt = ''")
    fun clearStoryHistory()

    @Query("UPDATE stories SET isLiked = 1 WHERE id IN (:ids)")
    fun setLikesToStories(ids: List<String>)

    @Query("UPDATE stories SET isLiked = 1 WHERE id = :id")
    fun setLikeToStory(id: Int): Int

    @Query("UPDATE stories SET isLiked = 0 WHERE id NOT IN (:ids)")
    fun removeLikesFromStories(ids: List<String>): Int

    @Query("UPDATE stories SET isLiked = 0 WHERE id = :id")
    fun removeLikeFromStory(id: Int)

    @Query("UPDATE stories SET recordUrl = :recordUrl WHERE id = :id")
    fun addRecordOfStory(id: String, recordUrl: String)

    @Query("UPDATE stories SET recordUrl = :recordUrl WHERE originalId = :id")
    fun addRecordOfStory(id: Int, recordUrl: String)

    @Query("UPDATE stories SET isLiked = 0, isBookmarked = 0, isDownloaded = 0, listenedAt = '', listenedAtLeast30Secs = false")
    fun clearUserData()

    @Query("UPDATE stories SET recordUrl = ''")
    fun deleteRecordUrls()
}
