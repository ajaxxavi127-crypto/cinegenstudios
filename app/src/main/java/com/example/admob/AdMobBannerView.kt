package com.example.admob

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.ui.theme.CineTextMuted
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdMobBannerView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bannerAdUnitId = remember { AdMobManager.getBannerAdUnitId(context) }
    val isTestMode = remember { AdMobManager.isUsingTestAds(context) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CineSurfaceContainer)
            .border(1.dp, Color(0xFF22283A), RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("admob_banner_view"),
                factory = { ctx ->
                    AdView(ctx).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = bannerAdUnitId
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )

            if (isTestMode) {
                Text(
                    text = "AdMob Banner • Test Mode ($bannerAdUnitId)",
                    fontSize = 9.sp,
                    color = CineTextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
