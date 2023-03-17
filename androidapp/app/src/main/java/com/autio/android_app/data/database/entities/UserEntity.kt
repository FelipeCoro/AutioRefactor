package com.autio.android_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_entity")
data class UserEntity(
    @PrimaryKey
    var userId: Int = 0,
    var userName: String = "",
    var userEmail: String = "",
    var userApiToken: String = "",
    var isGuestUser: Boolean = true,
    var remainingStories: Int = 5,
    var userSubIsActive: Boolean = false,
    var isPremiumUser: Boolean = false

)
