package com.autio.android_app.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.autio.android_app.data.database.entities.StoryEntitie

@Dao
interface StoryDao {

    @Insert(
        onConflict = OnConflictStrategy.IGNORE
    )
    suspend fun addPointer(
        storyEntitie: StoryEntitie
    )

    @Query(
        "SELECT * FROM map_points ORDER BY id ASC"
    )
    fun readAllData(): LiveData<List<StoryEntitie>>

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