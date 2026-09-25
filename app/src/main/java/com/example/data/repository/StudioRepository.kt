package com.example.data.repository

import com.example.data.dao.GeneratedMediaDao
import com.example.data.dao.StylePresetDao
import com.example.data.dao.TemplateDao
import com.example.data.model.GeneratedMediaItem
import com.example.data.model.StylePresetItem
import com.example.data.model.TemplateItem
import kotlinx.coroutines.flow.Flow

class StudioRepository(
    private val templateDao: TemplateDao,
    private val stylePresetDao: StylePresetDao,
    private val mediaDao: GeneratedMediaDao
) {
    val allTemplates: Flow<List<TemplateItem>> = templateDao.getAllTemplates()
    val allStyles: Flow<List<StylePresetItem>> = stylePresetDao.getAllStyles()
    val allMedia: Flow<List<GeneratedMediaItem>> = mediaDao.getAllMedia()

    suspend fun insertTemplate(template: TemplateItem): Long =
        templateDao.insertTemplate(template)

    suspend fun deleteTemplateById(id: Long) =
        templateDao.deleteTemplateById(id)

    suspend fun insertStyle(style: StylePresetItem): Long =
        stylePresetDao.insertStyle(style)

    suspend fun deleteStyleById(id: Long) =
        stylePresetDao.deleteStyleById(id)

    suspend fun insertMedia(media: GeneratedMediaItem): Long =
        mediaDao.insertMedia(media)

    suspend fun deleteMediaById(id: Long) =
        mediaDao.deleteMediaById(id)
}
