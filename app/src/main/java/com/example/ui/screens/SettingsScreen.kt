package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoSettings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.admob.AdMobBannerView
import com.example.admob.AdMobManager
import com.example.ui.theme.CineAmberTertiary
import com.example.ui.theme.CineCyanPrimary
import com.example.ui.theme.CineSurfaceContainer
import com.example.ui.theme.CineSurfaceContainerHigh
import com.example.ui.theme.CineSurfaceDark
import com.example.ui.theme.CineTextMuted
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import com.example.ui.theme.CineVioletSecondary
import com.example.viewmodel.StudioViewModel

@Composable
fun SettingsScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollState = rememberScrollState()

    val rewardCredits by viewModel.rewardCredits.collectAsState()
    val isRewardedLoaded by viewModel.isRewardedAdLoaded.collectAsState()

    var showAdConfigDialog by remember { mutableStateOf(false) }

    var isTestMode by remember { mutableStateOf(AdMobManager.isUsingTestAds(context)) }
    val currentBannerId = remember(isTestMode, showAdConfigDialog) { AdMobManager.getBannerAdUnitId(context) }
    val currentRewardedId = remember(isTestMode, showAdConfigDialog) { AdMobManager.getRewardedAdUnitId(context) }

    val hasGeminiKey = remember {
        try {
            val k = BuildConfig.GEMINI_API_KEY
            k.isNotBlank() && k != "MY_GEMINI_API_KEY"
        } catch (e: Exception) {
            false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CineSurfaceDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Settings & AdMob Monetization",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Configure ad units, passes, AI engines & publishing options",
            fontSize = 12.sp,
            color = CineTextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // AdMob Monetization Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_admob_settings"),
            colors = CardDefaults.cardColors(containerColor = CineSurfaceContainer),
            shape = RoundedCornerShape(14.dp)
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
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33FFB300)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdsClick,
                                contentDescription = null,
                                tint = CineAmberTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Google AdMob Ads",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (isTestMode) "Active: Official Test Ad Units" else "Active: Custom Live Ad Units",
                                fontSize = 11.sp,
                                color = CineAmberTertiary
                            )
                        }
                    }

                    Button(
                        onClick = { showAdConfigDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283042)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_configure_admob")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = CineCyanPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit IDs", fontSize = 11.sp, color = CineCyanPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ad Units Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CineSurfaceContainerHigh)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Configured AdMob App ID (Manifest):",
                        fontSize = 11.sp,
                        color = CineTextMuted
                    )
                    Text(
                        text = AdMobManager.CONFIGURED_APP_ID,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = CineAmberTertiary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Active Banner Ad Unit ID:",
                        fontSize = 11.sp,
                        color = CineTextMuted
                    )
                    Text(
                        text = currentBannerId,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = CineCyanPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Active Rewarded Video Ad Unit ID:",
                        fontSize = 11.sp,
                        color = CineTextMuted
                    )
                    Text(
                        text = currentRewardedId,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = CineVioletSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rewarded Export Pass Balance & Test Ad Launcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HD Video Export Passes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Current Balance: $rewardCredits passes",
                            fontSize = 11.sp,
                            color = CineAmberTertiary
                        )
                    }

                    Button(
                        onClick = {
                            if (activity != null) {
                                viewModel.watchRewardedAdForCredits(activity)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CineAmberTertiary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_watch_rewarded_ad_test")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Watch Rewarded Ad", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // AI Engine Configuration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CineSurfaceContainer),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x3300E5FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CineCyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Director & Storyboard Engine",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (hasGeminiKey) "Gemini 3.5 Flash Active" else "Offline Cinematic Synthesizer Active",
                            fontSize = 11.sp,
                            color = if (hasGeminiKey) CineCyanPrimary else CineTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (hasGeminiKey) {
                        "Gemini API key is configured. High-level screenplay analysis, camera shots, and prompt enhancements are powered directly by Gemini 3.5 Flash."
                    } else {
                        "Running in standalone offline mode. Script directing uses built-in cinematography knowledge base. Add GEMINI_API_KEY in Secrets for live cloud inference."
                    },
                    fontSize = 12.sp,
                    color = CineTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // About CineGen Studio Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CineSurfaceContainer),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CineTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "About CineGen Studio",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Version: 1.0.0 (Release-Ready for AdMob & Play Store)",
                    fontSize = 11.sp,
                    color = CineTextMuted
                )
                Text(
                    text = "Hardware Video Encoder: Android MediaCodec (H.264 / AVC)",
                    fontSize = 11.sp,
                    color = CineTextMuted
                )
                Text(
                    text = "Target SDK: 36 (Android 15+)",
                    fontSize = 11.sp,
                    color = CineTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Banner Preview in Settings
        AdMobBannerView(modifier = Modifier.testTag("settings_screen_admob_banner"))

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showAdConfigDialog) {
        AdMobConfigDialog(
            currentBannerId = AdMobManager.getSavedCustomBannerId(context),
            currentRewardedId = AdMobManager.getSavedCustomRewardedId(context),
            isTestMode = AdMobManager.isUsingTestAds(context),
            onDismiss = { showAdConfigDialog = false },
            onSave = { customBanner, customRewarded, testMode ->
                AdMobManager.saveAdMobConfig(context, customBanner, customRewarded, testMode)
                isTestMode = testMode
                showAdConfigDialog = false
            }
        )
    }
}

@Composable
fun AdMobConfigDialog(
    currentBannerId: String,
    currentRewardedId: String,
    isTestMode: Boolean,
    onDismiss: () -> Unit,
    onSave: (customBanner: String, customRewarded: String, testMode: Boolean) -> Unit
) {
    var bannerId by remember { mutableStateOf(currentBannerId) }
    var rewardedId by remember { mutableStateOf(currentRewardedId) }
    var testMode by remember { mutableStateOf(isTestMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("AdMob Ad Unit Configuration", fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "App ID: ${AdMobManager.CONFIGURED_APP_ID} is active in AndroidManifest.xml.",
                    fontSize = 11.sp,
                    color = CineAmberTertiary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Use Google Test Ad Units", fontSize = 13.sp, color = Color.White)
                    Switch(
                        checked = testMode,
                        onCheckedChange = { testMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CineCyanPrimary,
                            checkedTrackColor = Color(0xFF1E3A45)
                        )
                    )
                }

                if (!testMode) {
                    OutlinedTextField(
                        value = bannerId,
                        onValueChange = { bannerId = it },
                        label = { Text("Live Banner Ad Unit ID") },
                        placeholder = { Text("ca-app-pub-XXXXX/YYYYY") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_custom_banner_id")
                    )

                    OutlinedTextField(
                        value = rewardedId,
                        onValueChange = { rewardedId = it },
                        label = { Text("Live Rewarded Ad Unit ID") },
                        placeholder = { Text("ca-app-pub-XXXXX/ZZZZZ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_custom_rewarded_id")
                    )
                } else {
                    Text(
                        text = "Test mode is active. Ads will safely display Google test creatives without policy issues.",
                        fontSize = 11.sp,
                        color = CineAmberTertiary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(bannerId, rewardedId, testMode) },
                colors = ButtonDefaults.buttonColors(containerColor = CineCyanPrimary)
            ) {
                Text("Save Configuration", color = Color.Black, fontWeight = FontWeight.Bold)
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
