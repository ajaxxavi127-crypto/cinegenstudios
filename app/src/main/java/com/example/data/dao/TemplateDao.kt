package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TemplateItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates ORDER BY isCustom DESC, createdAt DESC")
    fun getAllTemplates(): Flow<List<TemplateItem>>

    @Query("SELECT * FROM templates WHERE category = :category ORDER BY createdAt DESC")
    fun getTemplatesByCategory(category: String): Flow<List<TemplateItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TemplateItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<TemplateItem>)

    @Delete
    suspend fun deleteTemplate(template: TemplateItem)

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun deleteTemplateById(id: Long)

    @Query("SELECT COUNT(*) FROM templates")
    suspend fun countTemplates(): Int
}
