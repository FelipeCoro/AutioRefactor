package com.autio.android_app.data.database.dao

import androidx.room.*
import com.autio.android_app.data.database.entities.MapPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MapPointDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addStories(story: List<MapPointEntity>): List<Long>

    @Query("SELECT * FROM map_points")
    suspend fun allStories(): List<MapPointEntity>?

    @Query("SELECT * FROM map_points WHERE " + "CASE WHEN :lat1 < :lat2 THEN lat BETWEEN :lat1 AND :lat2 ELSE lat BETWEEN :lat2 AND :lat1 END" + " AND " + "CASE WHEN :lng1 < :lng2 THEN lng BETWEEN :lng1 AND :lng2 ELSE lng BETWEEN :lng2 AND :lng1 END")
    suspend fun getStoriesInLatLngBoundaries(
        lat1: Double, lng1: Double, lat2: Double, lng2: Double
    ): List<MapPointEntity>

    @Query("SELECT * FROM map_points")
    suspend fun readStories(): List<MapPointEntity>

    @Query("SELECT * FROM map_points WHERE id IN (:ids)")
    fun readStoriesWithIds(ids: List<Int>): Flow<List<MapPointEntity>>

    @Query("SELECT * FROM map_points WHERE id = (:id)")
    suspend fun getMapPointById(id: String): MapPointEntity?

    @Query("DELETE FROM map_points")
    fun deleteAllStories()
}
