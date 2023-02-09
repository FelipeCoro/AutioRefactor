package com.autio.android_app.data.database.dao

import androidx.room.*
import com.autio.android_app.data.model.story.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    fun addCategories(
        categories: Array<Category>
    ): Array<Long>

    @Query(
        "SELECT * FROM user_categories ORDER BY \"order\" ASC"
    )
    fun readUserCategories(): Flow<Array<Category>>

    @Update(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun update(
        items: Array<Category>
    )
}