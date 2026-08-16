package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel

enum class MainTab(val title: String) {
    Dashboard("Dashboard"),
    Kasir("Kasir"),
    Inventaris("Inventaris"),
    Pengaturan("Pengaturan")
}

@Composable
fun MainScreen(viewModel: ThriftViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Dashboard) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == MainTab.Dashboard,
                    onClick = { selectedTab = MainTab.Dashboard },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(MainTab.Dashboard.title) }
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Kasir,
                    onClick = { selectedTab = MainTab.Kasir },
                    icon = { Icon(Icons.Default.PointOfSale, contentDescription = null) },
                    label = { Text(MainTab.Kasir.title) }
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Inventaris,
                    onClick = { selectedTab = MainTab.Inventaris },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                    label = { Text(MainTab.Inventaris.title) }
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Pengaturan,
                    onClick = { selectedTab = MainTab.Pengaturan },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(MainTab.Pengaturan.title) }
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            MainTab.Dashboard -> DashboardScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            MainTab.Kasir -> CashierScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            MainTab.Inventaris -> ThriftInventoryScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            MainTab.Pengaturan -> SettingsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}