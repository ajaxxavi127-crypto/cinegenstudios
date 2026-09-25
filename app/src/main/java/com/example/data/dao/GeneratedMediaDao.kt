package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.GeneratedMediaItem
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneratedMediaDao {
    @Query("SELECT * FROM generated_media ORDER BY exportedAt DESC")
    fun getAllMedia(): Flow<List<GeneratedMediaItem>>

    @Query("SELECT * FROM generated_media WHERE mediaType = :type ORDER BY exportedAt DESC")
    fun getMediaByType(type: String): Flow<List<GeneratedMediaItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: GeneratedMediaItem): Long

    @Delete
    suspend fun deleteMedia(media: GeneratedMediaItem)

    @Query("DELETE FROM generated_media WHERE id = :id")
    suspend fun deleteMediaById(id: Long)
}
