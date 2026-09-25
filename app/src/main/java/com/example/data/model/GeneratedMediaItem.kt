package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generated_media")
data class GeneratedMediaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val mediaType: String, // "VIDEO" or "IMAGE"
    val prompt: String,
    val styleName: String,
    val localFilePath: String,
    val durationSec: Int = 4,
    val resolution: String = "1080p Full HD",
    val thumbnailPath: String? = null,
    val exportedAt: Long = System.currentTimeMillis(),
    val isHdExported: Boolean = true
)
