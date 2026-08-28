package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.components.AdBanner

@Composable
fun MainTabScreen(
    onLogout: () -> Unit = {},
    onChangeApartment: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            Column {
                AdBanner()
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CameraAlt, contentDescription = "스캐너") },
                        label = { Text("스캐너") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.testTag("tab_scanner")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Leaderboard, contentDescription = "대시보드") },
                        label = { Text("대시보드") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.testTag("tab_dashboard")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Store, contentDescription = "포인트샵") },
                        label = { Text("포인트샵") },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        modifier = Modifier.testTag("tab_shop")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "설정") },
                        label = { Text("설정") },
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        modifier = Modifier.testTag("tab_settings")
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> AiScannerScreen()
                1 -> DashboardScreen()
                2 -> PointShopScreen()
                3 -> SettingsScreen(
                    onLogout = onLogout,
                    onChangeApartment = onChangeApartment
                )
            }
        }
    }
}
