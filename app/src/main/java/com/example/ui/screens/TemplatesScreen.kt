package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admob.AdMobBannerView
import com.example.data.model.TemplateItem
import com.example.ui.theme.CineCyanPrimary
import com.example.ui.theme.CineError
import com.example.ui.theme.CineSurfaceContainer
import com.example.ui.theme.CineSurfaceContainerHigh
import com.example.ui.theme.CineSurfaceDark
import com.example.ui.theme.CineTextMuted
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import com.example.ui.theme.CineVioletSecondary
import com.example.viewmodel.StudioUiState
import com.example.viewmodel.StudioViewModel

@Composable
fun TemplatesScreen(
    viewModel: StudioViewModel,
    uiState: StudioUiState,
    templates: List<TemplateItem>,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Cinematic", "Sci-Fi", "Cyberpunk", "Fantasy", "Nature", "Anime", "Commercial", "Custom")

    val filteredTemplates = remember(templates, uiState.categoryFilter) {
        if (uiState.categoryFilter == "All") {
            templates
        } else if (uiState.categoryFilter == "Custom") {
            templates.filter { it.isCustom }
        } else {
            templates.filter { it.category.equals(uiState.categoryFilter, ignoreCase = true) }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CineSurfaceDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CineCyanPrimary,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_template")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Custom Template")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header info
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Prompt & Motion Templates",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Director-grade scene templates with camera trajectories",
                    fontSize = 12.sp,
                    color = CineTextSecondary
                )
            }

            // Categories horizontal filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = uiState.categoryFilter == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setCategoryFilter(cat) },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0x3300E5FF),
                            selectedLabelColor = CineCyanPrimary,
                            containerColor = CineSurfaceContainer,
                            labelColor = CineTextSecondary
                        ),
                        modifier = Modifier.testTag("filter_chip_$cat")
                    )
                }
            }

            // Template Cards List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTemplates, key = { it.id }) { template ->
                    TemplateCard(
                        template = template,
                        onUseTemplate = { viewModel.selectTemplate(template) },
                        onDeleteTemplate = { viewModel.deleteTemplate(template.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    AdMobBannerView(modifier = Modifier.testTag("templates_screen_admob_banner"))
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddTemplateDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, cat, prompt, motion, cam, duration, aspect ->
                viewModel.addCustomTemplate(title, cat, prompt, motion, cam, duration, aspect)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TemplateCard(
    template: TemplateItem,
    onUseTemplate: () -> Unit,
    onDeleteTemplate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("template_card_${template.id}"),
        colors = CardDefaults.cardColors(containerColor = CineSurfaceContainer),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF232B3D)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = template.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CineCyanPrimary
                        )
                        Text(
                            text = " • ${template.cameraMovement} • ${template.durationSec}s • ${template.aspectRatio}",
                            fontSize = 11.sp,
                            color = CineTextMuted
                        )
                    }
                }

                if (template.isCustom) {
                    IconButton(
                        onClick = onDeleteTemplate,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Template",
                            tint = CineError,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = template.basePrompt,
                fontSize = 12.sp,
                color = CineTextSecondary,
                maxLines = 2
            )

            if (template.motionPrompt.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Motion: ${template.motionPrompt}",
                    fontSize = 11.sp,
                    color = CineVioletSecondary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onUseTemplate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("btn_use_template_${template.id}"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E283C)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = CineCyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Use Template in Studio",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CineCyanPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun AddTemplateDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, prompt: String, motion: String, camera: String, duration: Int, aspect: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Cinematic") }
    var prompt by remember { mutableStateOf("") }
    var motion by remember { mutableStateOf("") }
    var camera by remember { mutableStateOf("Slow Zoom In") }
    var duration by remember { mutableIntStateOf(4) }
    var aspect by remember { mutableStateOf("16:9") }

    val cameraOptions = listOf("Slow Zoom In", "Drone Aerial", "Orbit Right", "Pan Left", "Static Crane")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New User Template",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Template Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_template_title")
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Cyberpunk, Fantasy, Commercial)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_template_category")
                )

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("Base Creation Prompt") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("input_template_prompt")
                )

                OutlinedTextField(
                    value = motion,
                    onValueChange = { motion = it },
                    label = { Text("Motion & Trajectory Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_template_motion")
                )

                Text("Camera Movement:", fontSize = 12.sp, color = CineTextSecondary)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    cameraOptions.forEach { cam ->
                        FilterChip(
                            selected = camera == cam,
                            onClick = { camera = cam },
                            label = { Text(cam, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && prompt.isNotBlank()) {
                        onSave(title, category, prompt, motion, camera, duration, aspect)
                    }
                },
                enabled = title.isNotBlank() && prompt.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CineCyanPrimary)
            ) {
                Text("Save Template", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CineTextMuted)
            }
        },
        containerColor = CineSurfaceContainerHigh
    )
}
