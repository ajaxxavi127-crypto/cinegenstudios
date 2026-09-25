package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.GeneratedMediaDao
import com.example.data.dao.StylePresetDao
import com.example.data.dao.TemplateDao
import com.example.data.model.GeneratedMediaItem
import com.example.data.model.StylePresetItem
import com.example.data.model.TemplateItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TemplateItem::class,
        StylePresetItem::class,
        GeneratedMediaItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun templateDao(): TemplateDao
    abstract fun stylePresetDao(): StylePresetDao
    abstract fun generatedMediaDao(): GeneratedMediaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cinegen_studio_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val templateDao = database.templateDao()
            val styleDao = database.stylePresetDao()

            if (templateDao.countTemplates() == 0) {
                templateDao.insertAll(
                    listOf(
                        TemplateItem(
                            title = "Cyberpunk City Drive",
                            category = "Cyberpunk",
                            basePrompt = "Futuristic flying hovercars with trailing neon lights speeding through rainy neon-drenched towering skyscrapers",
                            motionPrompt = "Smooth forward chase camera, reflections on wet asphalt, shimmering neon signs",
                            cameraMovement = "Slow Zoom In",
                            durationSec = 4,
                            fps = 30,
                            aspectRatio = "16:9",
                            isCustom = false
                        ),
                        TemplateItem(
                            title = "Epic Fantasy Dragon Flight",
                            category = "Fantasy",
                            basePrompt = "Majestic obsidian dragon soaring above snow-capped mountain peaks during golden hour sunset",
                            motionPrompt = "Sweeping aerial panorama following the wingspan, volumetric cloud mist",
                            cameraMovement = "Drone Aerial",
                            durationSec = 5,
                            fps = 30,
                            aspectRatio = "16:9",
                            isCustom = false
                        ),
                        TemplateItem(
                            title = "Deep Space Celestial Nebula",
                            category = "Sci-Fi",
                            basePrompt = "Hypnotic celestial nebula expanding with swirling cosmic dust clouds and pulsating stellar flares",
                            motionPrompt = "Slow rotational orbital pan revealing distant star clusters and cosmic rings",
                            cameraMovement = "Orbit Right",
                            durationSec = 4,
                            fps = 30,
                            aspectRatio = "16:9",
                            isCustom = false
                        ),
                        TemplateItem(
                            title = "Cinematic Fashion Showcase",
                            category = "Commercial",
                            basePrompt = "Avant-garde haute couture model turning gracefully under dramatic high-contrast studio spotlights",
                            motionPrompt = "Slow motion fabric flutter, floating golden glitter bokeh",
                            cameraMovement = "Static Crane",
                            durationSec = 4,
                            fps = 30,
                            aspectRatio = "9:16",
                            isCustom = false
                        ),
                        TemplateItem(
                            title = "Serene Bioluminescent Forest",
                            category = "Nature",
                            basePrompt = "Gentle forest stream flowing through glowing blue mushrooms and cascading ancient willow trees under moonbeams",
                            motionPrompt = "Lateral tracking shot, floating bioluminescent spores shimmering softly",
                            cameraMovement = "Pan Left",
                            durationSec = 4,
                            fps = 30,
                            aspectRatio = "16:9",
                            isCustom = false
                        ),
                        TemplateItem(
                            title = "Anime Mecha Battle Charge",
                            category = "Anime",
                            basePrompt = "High-octane armored mecha powering up energy blades with intense electrical arcs and dynamic speed lines",
                            motionPrompt = "Dynamic action camera shake, energy burst particle explosion",
                            cameraMovement = "Slow Zoom In",
                            durationSec = 4,
                            fps = 30,
                            aspectRatio = "16:9",
                            isCustom = false
                        )
                    )
                )
            }

            if (styleDao.countStyles() == 0) {
                styleDao.insertAll(
                    listOf(
                        StylePresetItem(
                            name = "Cinematic 35mm",
                            styleKeywords = "35mm anamorphic film, Panavision lens, shallow depth of field, dramatic moody lighting, Kodak Portra color grading, ultra-detailed 8k",
                            negativeKeywords = "blurry, low quality, artifacts, watermark, flat lighting",
                            previewColorHex = "#FFB300",
                            isCustom = false
                        ),
                        StylePresetItem(
                            name = "Cyberpunk Neon",
                            styleKeywords = "cyberpunk aesthetic, vibrant neon cyan and magenta illumination, wet reflective surfaces, holographic glow, blade runner mood",
                            negativeKeywords = "daylight, washed out, low resolution, noisy artifacts",
                            previewColorHex = "#00E5FF",
                            isCustom = false
                        ),
                        StylePresetItem(
                            name = "Anime Shinkai",
                            styleKeywords = "Makoto Shinkai anime aesthetic, breathtaking sky with volumetric clouds, radiant sunbeams, hand-drawn detailing, vivid colors",
                            negativeKeywords = "realistic photo, 3d render, gritty, desaturated",
                            previewColorHex = "#7C4DFF",
                            isCustom = false
                        ),
                        StylePresetItem(
                            name = "Hyper-Realistic 8K",
                            styleKeywords = "octane render, unreal engine 5, ray tracing, ultra realistic, extreme texture detail, masterwork photography, Hasselblad medium format",
                            negativeKeywords = "cartoon, drawing, low resolution, bad anatomy",
                            previewColorHex = "#00E676",
                            isCustom = false
                        ),
                        StylePresetItem(
                            name = "Retro 80s Synthwave",
                            styleKeywords = "vintage VHS tape effect, synthwave grid, chromatic aberration, retro futuristic glow, analog warmth, scanlines",
                            negativeKeywords = "modern clean, desaturated, black and white",
                            previewColorHex = "#FF4081",
                            isCustom = false
                        ),
                        StylePresetItem(
                            name = "Dark Gothic Fantasy",
                            styleKeywords = "dark gothic fantasy, oil painting texture, chiaroscuro lighting, brooding atmospheric fog, intricate baroque armor, Elden Ring aesthetic",
                            negativeKeywords = "bright happy, cartoon, oversaturated, neon",
                            previewColorHex = "#9E9E9E",
                            isCustom = false
                        )
                    )
                )
            }
        }
    }
}
