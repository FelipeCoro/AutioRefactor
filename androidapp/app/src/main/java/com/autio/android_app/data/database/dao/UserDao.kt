package com.autio.android_app.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.autio.android_app.data.database.entities.UserEntity

@Dao
interface UserDao {
    @Query("SELECT u.remainingStories FROM user_entity u LIMIT 1")
    suspend fun getRemainingStories(): Int

    @Insert(onConflict = REPLACE)
    suspend fun createNewUser(newUser: UserEntity)

    @Query("UPDATE user_entity SET remainingStories = remainingStories+1")
    suspend fun addListenedStory()

    @Query("SELECT * FROM user_entity LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("SELECT isPremiumUser FROM user_entity WHERE isPremiumUser = true LIMIT 1")
    suspend fun isPremiumUser(): Boolean

    @Query("SELECT isGuestUser FROM user_entity WHERE isGuestUser = true LIMIT 1")
    suspend fun isGuestUser(): Boolean

    @Query("SELECT CASE WHEN remainingStories < 0 THEN true ELSE false END FROM user_entity LIMIT 1")
    suspend fun requiresPayment(): Boolean

    @Query("DELETE FROM user_entity ")
    fun clearUserData()
}
