package com.example.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.CineSurfaceContainer
import com.example.ui.theme.CineSurfaceDark
import java.io.File

@Composable
fun VideoPreviewPlayer(
    videoFilePath: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videoFile = remember(videoFilePath) { File(videoFilePath) }
    var isPlaying by remember { mutableStateOf(false) }

    if (!videoFile.exists()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(CineSurfaceContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Video file not found", color = Color.Gray, fontSize = 14.sp)
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF2B3245), RoundedCornerShape(12.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .testTag("video_preview_player"),
            factory = { ctx ->
                VideoView(ctx).apply {
                    val uri = Uri.fromFile(videoFile)
                    setVideoURI(uri)
                    val mediaController = MediaController(ctx)
                    mediaController.setAnchorView(this)
                    setMediaController(mediaController)

                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                        start()
                        isPlaying = true
                    }

                    setOnErrorListener { _, _, _ ->
                        isPlaying = false
                        true
                    }
                }
            },
            update = { view ->
                // Ensure correct file
            }
        )
    }
}
