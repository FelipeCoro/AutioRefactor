package com.autio.android_app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.autio.android_app.data.database.entities.UserEntity

@Dao
interface UserDao {
    @Query("SELECT remainingStories FROM UserEntity")
    fun getRemainingStories(): Int

    @Insert(onConflict = REPLACE)
    fun createNewUser(newUser: UserEntity)



    @Query("UPDATE UserEntity SET remainingStories = remainingStories+1")
    fun addListenedStory()

    @Query("SELECT * FROM UserEntity LIMIT 1")
    fun getCurrentUser(): UserEntity?

    @Query("SELECT isPremiumUser FROM UserEntity WHERE isPremiumUser = true LIMIT 1")
    fun isPremiumUser(): Boolean

    @Query("SELECT isGuestUser FROM UserEntity WHERE isGuestUser = true LIMIT 1")
    fun isGuestUser(): Boolean

    @Query("SELECT CASE WHEN remainingStories < 0 THEN true ELSE false FROM UserEntity LIMIT 1")
    fun requiresPayment(): Boolean
}