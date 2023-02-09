package com.autio.android_app.data.model.story

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "user_categories"
)
data class Category(
    @PrimaryKey
    var id: Int = 0,
    @Transient
    var firebaseId: String = "",
    var title: String = "",
    var order: Int = 0
) : Serializable