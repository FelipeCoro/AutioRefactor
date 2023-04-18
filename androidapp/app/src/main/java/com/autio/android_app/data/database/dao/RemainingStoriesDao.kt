package com.autio.android_app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.autio.android_app.data.database.entities.RemainingStoriesEntity


@Dao
interface RemainingStoriesDao {

    @Query("UPDATE remainingStories SET remainingStories =:amount")
    suspend fun updateStoryCount(amount: Int)

    @Query("Select * FROM remainingStories")
    suspend fun getCurrentStoryCount(): RemainingStoriesEntity

    @Insert
    suspend fun insertStories(remainingStories:RemainingStoriesEntity)
}
