package com.autio.android_app.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.autio.android_app.data.model.story.Story

@Dao
interface StoryDao {
    @Insert(
        onConflict = OnConflictStrategy.IGNORE
    )
    fun addStories(
        story: List<Story>
    ): List<Long>

    @Query(
        "SELECT * FROM stories ORDER BY id ASC"
    )
    fun readAllStories(): LiveData<List<Story>>

    @Query("SELECT * FROM stories WHERE id IN (:ids)")
    fun readStoriesWithIds(ids: Array<String>): LiveData<Array<Story>>

    @Query(
        "SELECT * FROM stories WHERE publishedDate = (SELECT MAX(publishedDate) FROM stories)"
    )
    fun readLastFetchedStory(): Story

    @Query(
        "DELETE FROM stories"
    )
    fun deleteAllStories()

//    @Query("SELECT * FROM user")
//    fun getAll(): List<User>
//
//    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): List<User>
//
//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): User
//
//    @Insert
//    fun insertAll(vararg users: User)
//
//    @Delete
//    fun delete(user: User)
}