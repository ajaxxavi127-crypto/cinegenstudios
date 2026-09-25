package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CineNavBar
import com.example.ui.components.CineTopBar
import com.example.ui.screens.CreateScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StylesScreen
import com.example.ui.screens.TemplatesScreen
import com.example.ui.theme.CineGenStudioTheme
import com.example.ui.theme.CineSurfaceDark
import com.example.viewmodel.StudioTab
import com.example.viewmodel.StudioViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: StudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CineGenStudioTheme {
                CineGenStudioApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CineGenStudioApp(viewModel: StudioViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val styles by viewModel.styles.collectAsStateWithLifecycle()
    val mediaLibrary by viewModel.mediaLibrary.collectAsStateWithLifecycle()
    val rewardCredits by viewModel.rewardCredits.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Display transient status messages
    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // Android Navigation BackHandler for sub-tabs
    BackHandler(enabled = uiState.currentTab != StudioTab.CREATE) {
        viewModel.setTab(StudioTab.CREATE)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CineSurfaceDark),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CineTopBar(
                credits = rewardCredits,
                onCreditsClick = { viewModel.setTab(StudioTab.SETTINGS) }
            )
        },
        bottomBar = {
            CineNavBar(
                selectedTab = uiState.currentTab,
                onTabSelected = { viewModel.setTab(it) },
                creditCount = rewardCredits
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CineSurfaceDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                StudioTab.CREATE -> CreateScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    styles = styles,
                    templates = templates
                )
                StudioTab.TEMPLATES -> TemplatesScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    templates = templates
                )
                StudioTab.STYLES -> StylesScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    styles = styles
                )
                StudioTab.LIBRARY -> LibraryScreen(
                    viewModel = viewModel,
                    mediaList = mediaLibrary
                )
                StudioTab.SETTINGS -> SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
