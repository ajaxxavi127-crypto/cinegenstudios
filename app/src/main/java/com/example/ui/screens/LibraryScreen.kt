package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.admob.AdMobBannerView
import com.example.data.model.GeneratedMediaItem
import com.example.ui.components.VideoPreviewPlayer
import com.example.ui.theme.CineCyanPrimary
import com.example.ui.theme.CineError
import com.example.ui.theme.CineSurfaceContainer
import com.example.ui.theme.CineSurfaceDark
import com.example.ui.theme.CineTextMuted
import com.example.ui.theme.CineTextSecondary
import com.example.ui.theme.CineVioletSecondary
import com.example.viewmodel.StudioViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LibraryScreen(
    viewModel: StudioViewModel,
    mediaList: List<GeneratedMediaItem>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: All, 1: Videos, 2: Images

    val filteredList = remember(mediaList, selectedFilterTab) {
        when (selectedFilterTab) {
            1 -> mediaList.filter { it.mediaType == "VIDEO" }
            2 -> mediaList.filter { it.mediaType == "IMAGE" }
            else -> mediaList
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CineSurfaceDark)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = "Exported Media Studio",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "${mediaList.size} generated clips & artworks stored on device",
                fontSize = 12.sp,
                color = CineTextSecondary
            )
        }

        TabRow(
            selectedTabIndex = selectedFilterTab,
            containerColor = CineSurfaceContainer,
            contentColor = CineCyanPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedFilterTab]),
                    color = CineCyanPrimary,
                    height = 2.dp
                )
            },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            listOf("All (${mediaList.size})", "Videos (${mediaList.count { it.mediaType == "VIDEO" }})", "Images (${mediaList.count { it.mediaType == "IMAGE" }})").forEachIndexed { index, title ->
                Tab(
                    selected = selectedFilterTab == index,
                    onClick = { selectedFilterTab = index },
                    text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("library_tab_$index")
                )
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = CineTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No media generated yet",
                        color = CineTextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Head to the Create tab to render your first AI video clip!",
                        color = CineTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList, key = { it.id }) { media ->
                    MediaLibraryCard(
                        media = media,
                        onShare = { viewModel.shareMedia(context, media) },
                        onDelete = { viewModel.deleteMedia(media.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    AdMobBannerView(modifier = Modifier.testTag("library_screen_admob_banner"))
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun MediaLibraryCard(
    media: GeneratedMediaItem,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(media.exportedAt) {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(media.exportedAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("media_card_${media.id}"),
        colors = CardDefaults.cardColors(containerColor = CineSurfaceContainer),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF252C3D))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (media.mediaType == "VIDEO") Icons.Default.Videocam else Icons.Default.Image,
                        contentDescription = media.mediaType,
                        tint = if (media.mediaType == "VIDEO") CineCyanPrimary else CineVioletSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = media.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "$dateStr • ${media.resolution}",
                            fontSize = 10.sp,
                            color = CineTextMuted
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = CineError,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Media Preview
            if (media.mediaType == "VIDEO") {
                VideoPreviewPlayer(
                    videoFilePath = media.localFilePath,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                AsyncImage(
                    model = File(media.localFilePath),
                    contentDescription = media.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\"${media.prompt}\"",
                fontSize = 12.sp,
                color = CineTextSecondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onShare,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("btn_share_media_${media.id}"),
                colors = ButtonDefaults.buttonColors(containerColor = CineCyanPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (media.mediaType == "VIDEO") "Export & Share MP4" else "Export & Share Image",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
