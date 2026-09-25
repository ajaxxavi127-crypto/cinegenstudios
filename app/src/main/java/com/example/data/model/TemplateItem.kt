package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class TemplateItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val basePrompt: String,
    val motionPrompt: String,
    val cameraMovement: String,
    val durationSec: Int = 4,
    val fps: Int = 30,
    val aspectRatio: String = "16:9",
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
