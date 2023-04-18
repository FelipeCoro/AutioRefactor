package com.autio.android_app.data.database.dao

import androidx.room.*
import com.autio.android_app.data.database.entities.CategoryEntity

import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCategories(categories: List<CategoryEntity>): List<Long>

    @Query("SELECT * FROM user_categories ORDER BY \"order\" ASC")
    fun readUserCategories(): Flow<List<CategoryEntity>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(items: List<CategoryEntity>)
}
