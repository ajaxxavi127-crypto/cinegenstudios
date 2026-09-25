package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.StylePresetItem
import kotlinx.coroutines.flow.Flow

@Dao
interface StylePresetDao {
    @Query("SELECT * FROM style_presets ORDER BY isCustom DESC, createdAt DESC")
    fun getAllStyles(): Flow<List<StylePresetItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStyle(style: StylePresetItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(styles: List<StylePresetItem>)

    @Delete
    suspend fun deleteStyle(style: StylePresetItem)

    @Query("DELETE FROM style_presets WHERE id = :id")
    suspend fun deleteStyleById(id: Long)

    @Query("SELECT COUNT(*) FROM style_presets")
    suspend fun countStyles(): Int
}
