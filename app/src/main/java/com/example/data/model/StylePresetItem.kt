package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "style_presets")
data class StylePresetItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val styleKeywords: String,
    val negativeKeywords: String = "blurry, low quality, artifacts, watermark, distorted",
    val previewColorHex: String = "#00E5FF",
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
