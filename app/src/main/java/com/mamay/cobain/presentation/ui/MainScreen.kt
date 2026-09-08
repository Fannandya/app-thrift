package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mamay.cobain.presentation.ui.components.MessageDialog
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel
import kotlinx.coroutines.delay

enum class MainTab(val title: String) {
    Dashboard("Dashboard"),
    Kasir("Kasir"),
    Riwayat("Riwayat"),
    Inventaris("Inventaris"),
    Pengaturan("Pengaturan")
}

@Composable
fun MainScreen(viewModel: ThriftViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Dashboard) }
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()

    val initializing by viewModel.isInitializing.collectAsStateWithLifecycle()
    var minTimePassed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(400)
        minTimePassed = true
    }

    LaunchedEffect(errorMessage) {
        val message = errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeErrorMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Active tab uses the primary (indigo/lavender) role so the bottom nav
            // matches the adopted Stitch palette instead of the default teal.
            val navItemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == MainTab.Dashboard,
                    onClick = { selectedTab = MainTab.Dashboard },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(MainTab.Dashboard.title) },
                    colors = navItemColors
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Kasir,
                    onClick = { selectedTab = MainTab.Kasir },
                    icon = { Icon(Icons.Default.PointOfSale, contentDescription = null) },
                    label = { Text(MainTab.Kasir.title) },
                    colors = navItemColors
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Riwayat,
                    onClick = { selectedTab = MainTab.Riwayat },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) },
                    label = { Text(MainTab.Riwayat.title) },
                    colors = navItemColors
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Inventaris,
                    onClick = { selectedTab = MainTab.Inventaris },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                    label = { Text(MainTab.Inventaris.title) },
                    colors = navItemColors
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Pengaturan,
                    onClick = { selectedTab = MainTab.Pengaturan },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(MainTab.Pengaturan.title) },
                    colors = navItemColors
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            MainTab.Dashboard -> DashboardScreen(
                viewModel = viewModel,
                onOpenCashier = { selectedTab = MainTab.Kasir },
                onOpenHistory = { selectedTab = MainTab.Riwayat },
                modifier = Modifier.padding(innerPadding)
            )
            MainTab.Kasir -> CashierScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            MainTab.Riwayat -> TransactionHistoryScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            MainTab.Inventaris -> ThriftInventoryScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            MainTab.Pengaturan -> SettingsScreen(
                viewModel = viewModel,
                snackbarHostState = snackbarHostState,
                onResetDone = { selectedTab = MainTab.Dashboard },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

        successMessage?.let { message ->
            MessageDialog(
                title = "Berhasil",
                message = message,
                onDismiss = { viewModel.consumeSuccessMessage() }
            )
        }

        if (initializing || !minTimePassed) {
            LoadingScreen()
        }
    }
}