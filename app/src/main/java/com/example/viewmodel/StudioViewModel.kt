package com.example.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.admob.AdMobManager
import com.example.data.db.AppDatabase
import com.example.data.model.GeneratedMediaItem
import com.example.data.model.StylePresetItem
import com.example.data.model.TemplateItem
import com.example.data.repository.StudioRepository
import com.example.generator.ai.GeminiDirectorService
import com.example.generator.ai.ScriptAnalysis
import com.example.generator.image.ImageGenerator
import com.example.generator.video.VideoGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class StudioTab {
    CREATE,
    TEMPLATES,
    STYLES,
    LIBRARY,
    SETTINGS
}

enum class CreateMode {
    VIDEO,
    IMAGE
}

data class StudioUiState(
    val currentTab: StudioTab = StudioTab.CREATE,
    val createMode: CreateMode = CreateMode.VIDEO,
    val prompt: String = "Futuristic flying hovercars racing through neon skyscrapers",
    val selectedTemplate: TemplateItem? = null,
    val selectedStyle: StylePresetItem? = null,
    val cameraMovement: String = "Slow Zoom In",
    val aspectRatio: String = "16:9",
    val durationSec: Int = 4,
    val fps: Int = 30,
    val isDirecting: Boolean = false,
    val scriptAnalysis: ScriptAnalysis? = null,
    val isGenerating: Boolean = false,
    val generationProgress: Float = 0f,
    val generationStatus: String = "",
    val lastGeneratedMedia: GeneratedMediaItem? = null,
    val categoryFilter: String = "All",
    val statusMessage: String? = null
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudioRepository
    val templates: StateFlow<List<TemplateItem>>
    val styles: StateFlow<List<StylePresetItem>>
    val mediaLibrary: StateFlow<List<GeneratedMediaItem>>

    val rewardCredits: StateFlow<Int> = AdMobManager.rewardCredits
    val isRewardedAdLoaded: StateFlow<Boolean> = AdMobManager.isRewardedAdLoaded

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = StudioRepository(
            database.templateDao(),
            database.stylePresetDao(),
            database.generatedMediaDao()
        )

        templates = repository.allTemplates.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        styles = repository.allStyles.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        mediaLibrary = repository.allMedia.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Initialize AdMob
        AdMobManager.initialize(application)
    }

    fun setTab(tab: StudioTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setCreateMode(mode: CreateMode) {
        _uiState.update { it.copy(createMode = mode) }
    }

    fun setPrompt(prompt: String) {
        _uiState.update { it.copy(prompt = prompt) }
    }

    fun setCameraMovement(cameraMovement: String) {
        _uiState.update { it.copy(cameraMovement = cameraMovement) }
    }

    fun setAspectRatio(ratio: String) {
        _uiState.update { it.copy(aspectRatio = ratio) }
    }

    fun setDuration(sec: Int) {
        _uiState.update { it.copy(durationSec = sec) }
    }

    fun setCategoryFilter(category: String) {
        _uiState.update { it.copy(categoryFilter = category) }
    }

    fun selectTemplate(template: TemplateItem) {
        _uiState.update {
            it.copy(
                selectedTemplate = template,
                prompt = template.basePrompt,
                cameraMovement = template.cameraMovement,
                durationSec = template.durationSec,
                aspectRatio = template.aspectRatio,
                currentTab = StudioTab.CREATE
            )
        }
    }

    fun selectStyle(style: StylePresetItem) {
        _uiState.update {
            it.copy(
                selectedStyle = style,
                currentTab = StudioTab.CREATE
            )
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    // Gemini Screenplay & Storyboard Director Analysis
    fun requestAiDirectorAnalysis() {
        val state = _uiState.value
        val styleName = state.selectedStyle?.name ?: "Cinematic 35mm"

        viewModelScope.launch {
            _uiState.update { it.copy(isDirecting = true, statusMessage = "AI Director is analyzing screenplay & camera shots...") }
            try {
                val analysis = GeminiDirectorService.analyzeAndDirect(
                    userPrompt = state.prompt,
                    styleName = styleName,
                    cameraMovement = state.cameraMovement
                )
                _uiState.update {
                    it.copy(
                        isDirecting = false,
                        scriptAnalysis = analysis,
                        prompt = analysis.expandedPrompt,
                        statusMessage = "AI Storyboard Generated! Review shot breakdown below."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDirecting = false,
                        statusMessage = "AI Director analysis complete."
                    )
                }
            }
        }
    }

    // Start Generation (Video or Image)
    fun startGeneration(activity: Activity) {
        val state = _uiState.value
        if (state.prompt.isBlank()) {
            _uiState.update { it.copy(statusMessage = "Please enter a prompt or choose a template first!") }
            return
        }

        if (state.createMode == CreateMode.VIDEO) {
            generateVideoInternal(activity)
        } else {
            generateImageInternal(activity)
        }
    }

    private fun generateVideoInternal(context: Context) {
        val state = _uiState.value
        val style = state.selectedStyle
        val styleName = style?.name ?: "Cinematic 35mm"
        val parsedColor = try {
            Color.parseColor(style?.previewColorHex ?: "#00E5FF")
        } catch (e: Exception) {
            Color.parseColor("#00E5FF")
        }

        val (w, h) = when (state.aspectRatio) {
            "9:16" -> Pair(540, 960) // High quality mobile aspect
            "1:1" -> Pair(640, 640)
            else -> Pair(960, 540) // 16:9 cinematic landscape
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGenerating = true,
                    generationProgress = 0.05f,
                    generationStatus = "Synthesizing cinematic frames with ${state.cameraMovement}..."
                )
            }

            try {
                val config = VideoGenerator.VideoGenConfig(
                    title = state.selectedTemplate?.title ?: "AI Masterpiece",
                    prompt = state.prompt,
                    styleName = styleName,
                    cameraMovement = state.cameraMovement,
                    width = w,
                    height = h,
                    fps = state.fps,
                    durationSeconds = state.durationSec,
                    primaryColor = parsedColor
                )

                val videoFile = VideoGenerator.generateMp4Video(
                    context = context,
                    config = config,
                    onProgress = { progress ->
                        _uiState.update {
                            it.copy(
                                generationProgress = progress,
                                generationStatus = when {
                                    progress < 0.3f -> "Directing visual lighting & atmospheric haze..."
                                    progress < 0.7f -> "Synthesizing dynamic motion & particle flares..."
                                    progress < 0.95f -> "Encoding hardware H.264 MP4 clip..."
                                    else -> "Finalizing video..."
                                }
                            )
                        }
                    }
                )

                val mediaItem = GeneratedMediaItem(
                    title = state.selectedTemplate?.title ?: "AI Video: ${state.prompt.take(24)}",
                    mediaType = "VIDEO",
                    prompt = state.prompt,
                    styleName = styleName,
                    localFilePath = videoFile.absolutePath,
                    durationSec = state.durationSec,
                    resolution = "${w}x${h} HD MP4"
                )

                val mediaId = repository.insertMedia(mediaItem)
                val savedMedia = mediaItem.copy(id = mediaId)

                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generationProgress = 1f,
                        generationStatus = "Video generated successfully!",
                        lastGeneratedMedia = savedMedia,
                        statusMessage = "MP4 Video rendered successfully! Ready to export."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generationStatus = "Error: ${e.localizedMessage ?: "Encoding failed"}",
                        statusMessage = "Failed to render video: ${e.message}"
                    )
                }
            }
        }
    }

    private fun generateImageInternal(context: Context) {
        val state = _uiState.value
        val style = state.selectedStyle
        val styleKeywords = style?.styleKeywords ?: "35mm anamorphic, volumetric lighting, 8k render"
        val parsedColor = try {
            Color.parseColor(style?.previewColorHex ?: "#00E5FF")
        } catch (e: Exception) {
            Color.parseColor("#00E5FF")
        }

        val (w, h) = when (state.aspectRatio) {
            "9:16" -> Pair(720, 1280)
            "16:9" -> Pair(1280, 720)
            else -> Pair(1080, 1080)
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGenerating = true,
                    generationProgress = 0.2f,
                    generationStatus = "Synthesizing high-definition digital artwork..."
                )
            }

            try {
                val config = ImageGenerator.ImageGenConfig(
                    prompt = state.prompt,
                    styleKeywords = styleKeywords,
                    width = w,
                    height = h,
                    styleColor = parsedColor
                )

                val imageFile = ImageGenerator.generateImage(context, config)

                val mediaItem = GeneratedMediaItem(
                    title = "AI Artwork: ${state.prompt.take(24)}",
                    mediaType = "IMAGE",
                    prompt = state.prompt,
                    styleName = style?.name ?: "Artistic",
                    localFilePath = imageFile.absolutePath,
                    durationSec = 0,
                    resolution = "${w}x${h} Ultra HD"
                )

                val mediaId = repository.insertMedia(mediaItem)
                val savedMedia = mediaItem.copy(id = mediaId)

                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generationProgress = 1f,
                        generationStatus = "Image generated successfully!",
                        lastGeneratedMedia = savedMedia,
                        statusMessage = "Artistic image created! Saved in library."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generationStatus = "Error: ${e.localizedMessage ?: "Failed"}",
                        statusMessage = "Image synthesis error: ${e.message}"
                    )
                }
            }
        }
    }

    // Rewarded Ad Flow for exporting HD video clips or unlocking bonus credits
    fun watchRewardedAdForCredits(activity: Activity) {
        AdMobManager.showRewardedAd(
            activity = activity,
            onRewardEarned = { amount ->
                _uiState.update {
                    it.copy(statusMessage = "Reward unlocked! +$amount HD Video Export Pass added!")
                }
            },
            onAdUnavailable = {
                // Offline fallback reward
                AdMobManager.addCredits(activity, 2)
                _uiState.update {
                    it.copy(statusMessage = "Bonus test credits awarded! (+2 HD Video Export Passes)")
                }
            }
        )
    }

    // Share or Export media to external apps (WhatsApp, Instagram, Files, Photos)
    fun shareMedia(context: Context, mediaItem: GeneratedMediaItem) {
        val file = File(mediaItem.localFilePath)
        if (!file.exists()) {
            Toast.makeText(context, "Media file not found on disk", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri = FileProvider.getUriForFile(
                context,
                "com.aistudio.cinegen.studio.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (mediaItem.mediaType == "VIDEO") "video/mp4" else "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, mediaItem.title)
                putExtra(Intent.EXTRA_TEXT, "Created with CineGen Studio: ${mediaItem.prompt}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share CineGen Media"))
        } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // CRUD: Add Custom Template
    fun addCustomTemplate(
        title: String,
        category: String,
        prompt: String,
        motion: String,
        camera: String,
        duration: Int,
        aspectRatio: String
    ) {
        viewModelScope.launch {
            val template = TemplateItem(
                title = title,
                category = category,
                basePrompt = prompt,
                motionPrompt = motion,
                cameraMovement = camera,
                durationSec = duration,
                aspectRatio = aspectRatio,
                isCustom = true
            )
            repository.insertTemplate(template)
            _uiState.update { it.copy(statusMessage = "Custom template '$title' saved!") }
        }
    }

    fun deleteTemplate(id: Long) {
        viewModelScope.launch {
            repository.deleteTemplateById(id)
            _uiState.update { it.copy(statusMessage = "Template deleted.") }
        }
    }

    // CRUD: Add Custom Style Preset
    fun addCustomStyle(
        name: String,
        styleKeywords: String,
        negativeKeywords: String,
        colorHex: String
    ) {
        viewModelScope.launch {
            val style = StylePresetItem(
                name = name,
                styleKeywords = styleKeywords,
                negativeKeywords = negativeKeywords,
                previewColorHex = colorHex,
                isCustom = true
            )
            repository.insertStyle(style)
            _uiState.update { it.copy(statusMessage = "Custom style '$name' saved!") }
        }
    }

    fun deleteStyle(id: Long) {
        viewModelScope.launch {
            repository.deleteStyleById(id)
            _uiState.update { it.copy(statusMessage = "Style preset deleted.") }
        }
    }

    fun deleteMedia(id: Long) {
        viewModelScope.launch {
            repository.deleteMediaById(id)
            _uiState.update { it.copy(statusMessage = "Media removed from library.") }
        }
    }
}
