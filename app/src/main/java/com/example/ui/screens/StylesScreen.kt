package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.StylePresetItem
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
fun StylesScreen(
    viewModel: StudioViewModel,
    uiState: StudioUiState,
    styles: List<StylePresetItem>,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CineSurfaceDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CineVioletSecondary,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_style")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Custom Style")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Cinematic Style Library",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Curated visual aesthetics, lenses, color grades & rendering tags",
                    fontSize = 12.sp,
                    color = CineTextSecondary
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(styles, key = { it.id }) { style ->
                    val isSelected = uiState.selectedStyle?.id == style.id
                    StylePresetCard(
                        style = style,
                        isSelected = isSelected,
                        onSelectStyle = { viewModel.selectStyle(style) },
                        onDeleteStyle = { viewModel.deleteStyle(style.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    AdMobBannerView(modifier = Modifier.testTag("styles_screen_admob_banner"))
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddStyleDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, keywords, negKeywords, colorHex ->
                viewModel.addCustomStyle(name, keywords, negKeywords, colorHex)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun StylePresetCard(
    style: StylePresetItem,
    isSelected: Boolean,
    onSelectStyle: () -> Unit,
    onDeleteStyle: () -> Unit
) {
    val styleColor = try {
        Color(android.graphics.Color.parseColor(style.previewColorHex))
    } catch (e: Exception) {
        CineCyanPrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectStyle() }
            .testTag("style_card_${style.id}"),
        colors = CardDefaults.cardColors(containerColor = CineSurfaceContainer),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isSelected) CineCyanPrimary else Color(0xFF242C3F)
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(styleColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = style.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    if (style.isCustom) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x33B388FF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CUSTOM",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CineVioletSecondary
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(CineCyanPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (style.isCustom) {
                        IconButton(
                            onClick = onDeleteStyle,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Style",
                                tint = CineError,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Keywords: ${style.styleKeywords}",
                fontSize = 12.sp,
                color = CineTextSecondary
            )

            if (style.negativeKeywords.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Negative: ${style.negativeKeywords}",
                    fontSize = 11.sp,
                    color = CineTextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onSelectStyle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .testTag("btn_select_style_${style.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) CineCyanPrimary else Color(0xFF1E2638)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isSelected) "Active in Studio" else "Apply Style to Studio",
                    color = if (isSelected) Color.Black else CineCyanPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AddStyleDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, keywords: String, negativeKeywords: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }
    var negKeywords by remember { mutableStateOf("blurry, low quality, artifacts, watermark") }
    var colorHex by remember { mutableStateOf("#00E5FF") }

    val presetColors = listOf("#00E5FF", "#B388FF", "#FFB300", "#00E676", "#FF4081", "#FF5252")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create Custom Style Preset",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Style Name (e.g. Claymation, Noir Detective)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_style_name")
                )

                OutlinedTextField(
                    value = keywords,
                    onValueChange = { keywords = it },
                    label = { Text("Style Keywords (lighting, optics, aesthetic)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("input_style_keywords")
                )

                OutlinedTextField(
                    value = negKeywords,
                    onValueChange = { negKeywords = it },
                    label = { Text("Negative Prompt Filter") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_style_negative")
                )

                Text("Accent Color Theme:", fontSize = 12.sp, color = CineTextSecondary)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    presetColors.forEach { hex ->
                        val parsed = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = colorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(parsed)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable { colorHex = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && keywords.isNotBlank()) {
                        onSave(name, keywords, negKeywords, colorHex)
                    }
                },
                enabled = name.isNotBlank() && keywords.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CineCyanPrimary)
            ) {
                Text("Save Style", color = Color.Black, fontWeight = FontWeight.Bold)
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
