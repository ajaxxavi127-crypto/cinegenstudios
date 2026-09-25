package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CineCyanPrimary
import com.example.ui.theme.CineSurfaceContainer
import com.example.ui.theme.CineTextMuted
import com.example.ui.theme.CineTextPrimary
import com.example.viewmodel.StudioTab

data class NavItem(
    val tab: StudioTab,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun CineNavBar(
    selectedTab: StudioTab,
    onTabSelected: (StudioTab) -> Unit,
    creditCount: Int,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(StudioTab.CREATE, "Create", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_create"),
        NavItem(StudioTab.TEMPLATES, "Templates", Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary, "nav_templates"),
        NavItem(StudioTab.STYLES, "Styles", Icons.Filled.Palette, Icons.Outlined.Palette, "nav_styles"),
        NavItem(StudioTab.LIBRARY, "Library", Icons.Filled.Collections, Icons.Outlined.Collections, "nav_library"),
        NavItem(StudioTab.SETTINGS, "Ad & Setup", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
    )

    NavigationBar(
        modifier = modifier.testTag("cine_bottom_nav"),
        containerColor = CineSurfaceContainer,
        contentColor = CineTextPrimary
    ) {
        items.forEach { item ->
            val isSelected = selectedTab == item.tab
            NavigationBarItem(
                modifier = Modifier.testTag(item.testTag),
                selected = isSelected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    if (item.tab == StudioTab.SETTINGS && creditCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = CineCyanPrimary,
                                    contentColor = Color.Black
                                ) {
                                    Text(text = "$creditCount", fontSize = 10.sp)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title
                        )
                    }
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        color = if (isSelected) CineCyanPrimary else CineTextMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CineCyanPrimary,
                    selectedTextColor = CineCyanPrimary,
                    unselectedIconColor = CineTextMuted,
                    unselectedTextColor = CineTextMuted,
                    indicatorColor = Color(0x2200E5FF)
                )
            )
        }
    }
}
