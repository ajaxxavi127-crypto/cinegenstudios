package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.admob.AdMobBannerView
import com.example.admob.AdMobManager
import com.example.data.model.StylePresetItem
import com.example.data.model.TemplateItem
import com.example.ui.components.VideoPreviewPlayer
import com.example.ui.theme.CineAmberTertiary
import com.example.ui.theme.CineCyanPrimary
import com.example.ui.theme.CineGradientEnd
import com.example.ui.theme.CineGradientMid
import com.example.ui.theme.CineGradientStart
import com.example.ui.theme.CineSurfaceContainer
import com.example.ui.theme.CineSurfaceContainerHigh
import com.example.ui.theme.CineTextMuted
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import com.example.ui.theme.CineVioletSecondary
import com.example.viewmodel.CreateMode
import com.example.viewmodel.StudioTab
import com.example.viewmodel.StudioUiState
import com.example.viewmodel.StudioViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    viewModel: StudioViewModel,
    uiState: StudioUiState,
    styles: List<StylePresetItem>,
    templates: List<TemplateItem>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollState = rememberScrollState()

    val cameraMovements = listOf("Slow Zoom In", "Drone Aerial", "Orbit Right", "Pan Left", "Static Crane")
    val aspectRatios = listOf("16:9", "9:16", "1:1")
    val durations = listOf(3, 4, 5)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp)
    ) {
        // Hero Studio Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF242C3F), RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.hero_cine_studio_1790374756699),
                contentDescription = "Studio Hero Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xCC090C12),
                                Color(0x88090C12),
                                Color(0x33000000)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CineCyanPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI GENERATION SUITE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CineCyanPrimary,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Text-to-Video & Image Creator",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Custom templates, styles & hardware MP4 export",
                    fontSize = 12.sp,
                    color = CineTextSecondary
                )
            }
        }

        // Mode Selector: AI Video vs AI Image
        TabRow(
            selectedTabIndex = if (uiState.createMode == CreateMode.VIDEO) 0 else 1,
            containerColor = CineSurfaceContainer,
            contentColor = CineCyanPrimary,
            indicator = { tabPositions ->
                val index = if (uiState.createMode == CreateMode.VIDEO) 0 else 1
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                    color = CineCyanPrimary,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = uiState.createMode == CreateMode.VIDEO,
                onClick = { viewModel.setCreateMode(CreateMode.VIDEO) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Mode",
                            tint = if (uiState.createMode == CreateMode.VIDEO) CineCyanPrimary else CineTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Video Creator",
                            fontWeight = FontWeight.SemiBold,
                            color = if (uiState.createMode == CreateMode.VIDEO) CineCyanPrimary else CineTextMuted
                        )
                    }
                },
                modifier = Modifier.testTag("tab_video_creator")
            )
            Tab(
                selected = uiState.createMode == CreateMode.IMAGE,
                onClick = { viewModel.setCreateMode(CreateMode.IMAGE) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Image Mode",
                            tint = if (uiState.createMode == CreateMode.IMAGE) CineCyanPrimary else CineTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Image Studio",
                            fontWeight = FontWeight.SemiBold,
                            color = if (uiState.createMode == CreateMode.IMAGE) CineCyanPrimary else CineTextMuted
                        )
                    }
                },
                modifier = Modifier.testTag("tab_image_creator")
            )
        }

        // Prompt Input & AI Director Trigger
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = CineSurfaceContainer),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Creation Prompt",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CineTextPrimary
                    )

                    // Gemini Director button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF261C4E), Color(0xFF132F3D))))
                            .clickable(enabled = !uiState.isDirecting) { viewModel.requestAiDirectorAnalysis() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("btn_ai_director"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.isDirecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = CineCyanPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Director",
                                tint = CineCyanPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.isDirecting) "Directing..." else "AI Director ✨",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CineCyanPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.prompt,
                    onValueChange = { viewModel.setPrompt(it) },
                    placeholder = {
                        Text(
                            text = "Describe your cinematic vision or select a template...",
                            fontSize = 13.sp,
                            color = CineTextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prompt_text_field"),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CineCyanPrimary,
                        unfocusedBorderColor = Color(0xFF2E3547),
                        focusedContainerColor = CineSurfaceContainerHigh,
                        unfocusedContainerColor = CineSurfaceContainerHigh,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Quick preset tags or active template pill
                if (uiState.selectedTemplate != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x3300E5FF))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Template",
                            tint = CineCyanPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Template: ${uiState.selectedTemplate.title} (${uiState.selectedTemplate.category})",
                            fontSize = 11.sp,
                            color = CineCyanPrimary
                        )
                    }
                }
            }
        }

        // Storyboard Breakdown (if available)
        AnimatedVisibility(visible = uiState.scriptAnalysis != null) {
            val analysis = uiState.scriptAnalysis
            if (analysis != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141926)),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CineCyanPrimary, CineVioletSecondary)))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = "Director Storyboard",
                                tint = CineCyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Director Screenplay & Storyboard",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Cinematography: ${analysis.cinematographyNotes}",
                            fontSize = 12.sp,
                            color = CineVioletSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        analysis.storyboard.forEach { shot ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CineSurfaceContainerHigh)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(CineCyanPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${shot.shotNumber}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = shot.shotType,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = CineCyanPrimary
                                    )
                                    Text(
                                        text = shot.visualDescription,
                                        fontSize = 11.sp,
                                        color = CineTextSecondary
                                    )
                                    Text(
                                        text = "Camera: ${shot.cameraAngle} • ${shot.lightingMood}",
                                        fontSize = 10.sp,
                                        color = CineTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Style Selector Horizontal Scroll
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cinematic Style",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CineTextPrimary
                )
                Text(
                    text = "See All Styles",
                    fontSize = 11.sp,
                    color = CineCyanPrimary,
                    modifier = Modifier.clickable { viewModel.setTab(StudioTab.STYLES) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                styles.forEach { style ->
                    val isSelected = uiState.selectedStyle?.id == style.id
                    val styleColor = try {
                        Color(android.graphics.Color.parseColor(style.previewColorHex))
                    } catch (e: Exception) {
                        CineCyanPrimary
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0x3300E5FF) else CineSurfaceContainer)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) CineCyanPrimary else Color(0xFF262C3D),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.selectStyle(style) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("style_chip_${style.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(styleColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = style.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else CineTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Video Settings (Camera Movement, Aspect Ratio, Duration)
        if (uiState.createMode == CreateMode.VIDEO) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = CineSurfaceContainer),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Camera Motion Choreography",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CineTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cameraMovements.forEach { cam ->
                            val isSelected = uiState.cameraMovement == cam
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setCameraMovement(cam) },
                                label = { Text(cam, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0x3300E5FF),
                                    selectedLabelColor = CineCyanPrimary,
                                    containerColor = CineSurfaceContainerHigh,
                                    labelColor = CineTextSecondary
                                ),
                                modifier = Modifier.testTag("camera_chip_$cam")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Aspect Ratio
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Aspect Ratio",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CineTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                aspectRatios.forEach { ratio ->
                                    val isSelected = uiState.aspectRatio == ratio
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) CineCyanPrimary else CineSurfaceContainerHigh)
                                            .clickable { viewModel.setAspectRatio(ratio) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = ratio,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else CineTextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Duration
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Duration",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CineTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                durations.forEach { sec ->
                                    val isSelected = uiState.durationSec == sec
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) CineVioletSecondary else CineSurfaceContainerHigh)
                                            .clickable { viewModel.setDuration(sec) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${sec}s",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else CineTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Image Aspect Ratio
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = CineSurfaceContainer),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Image Canvas Ratio",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CineTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        aspectRatios.forEach { ratio ->
                            val isSelected = uiState.aspectRatio == ratio
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CineCyanPrimary else CineSurfaceContainerHigh)
                                    .clickable { viewModel.setAspectRatio(ratio) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (ratio == "16:9") "16:9 Landscape" else if (ratio == "9:16") "9:16 Portrait" else "1:1 Square",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else CineTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Generate Masterpiece Action Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Button(
                onClick = {
                    if (activity != null) {
                        viewModel.startGeneration(activity)
                    }
                },
                enabled = !uiState.isGenerating && uiState.prompt.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_generate_masterpiece"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(CineGradientStart, CineGradientMid, CineGradientEnd)
                            ),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isGenerating) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Synthesizing...",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (uiState.createMode == CreateMode.VIDEO) Icons.Default.Videocam else Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.createMode == CreateMode.VIDEO) "Render MP4 Video Clip" else "Create AI Artwork",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // Live Generation Progress Indicator
        AnimatedVisibility(visible = uiState.isGenerating) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = CineSurfaceContainer),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = uiState.generationStatus,
                            fontSize = 12.sp,
                            color = CineCyanPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(uiState.generationProgress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = uiState.generationProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CineCyanPrimary,
                        trackColor = Color(0xFF202636)
                    )
                }
            }
        }

        // Generated Media Preview & Action Card
        if (uiState.lastGeneratedMedia != null) {
            val media = uiState.lastGeneratedMedia
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("card_generated_result"),
                colors = CardDefaults.cardColors(containerColor = CineSurfaceContainer),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CineCyanPrimary, CineVioletSecondary)))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = media.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = "${media.resolution} • ${media.styleName}",
                                fontSize = 11.sp,
                                color = CineCyanPrimary
                            )
                        }

                        // Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x3300E5FF))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = media.mediaType,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CineCyanPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (media.mediaType == "VIDEO") {
                        VideoPreviewPlayer(
                            videoFilePath = media.localFilePath,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        AsyncImage(
                            model = File(media.localFilePath),
                            contentDescription = "Generated Artwork",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons: Export / Share & Watch Rewarded Ad for bonus
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.shareMedia(context, media) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_export_share_media"),
                            colors = ButtonDefaults.buttonColors(containerColor = CineCyanPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (media.mediaType == "VIDEO") "Export MP4" else "Export Image",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                if (activity != null) {
                                    viewModel.watchRewardedAdForCredits(activity)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_watch_ad_bonus"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CineAmberTertiary),
                            shape = RoundedCornerShape(10.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(CineAmberTertiary, Color(0xFFFF8F00))))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Watch Ad",
                                tint = CineAmberTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Watch Ad (+Pass)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CineAmberTertiary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // AdMob Banner Placement
        AdMobBannerView(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .testTag("create_screen_admob_banner")
        )
    }
}
