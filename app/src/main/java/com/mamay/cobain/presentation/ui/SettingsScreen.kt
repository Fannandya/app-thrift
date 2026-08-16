package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel

private enum class SettingsSection { NONE, CATEGORIES, SIZES }

@Composable
fun SettingsScreen(
    viewModel: ThriftViewModel,
    modifier: Modifier = Modifier
) {
    var section by remember { mutableStateOf(SettingsSection.NONE) }

    when (section) {
        SettingsSection.CATEGORIES -> CategoryManagementScreen(
            viewModel = viewModel,
            onBack = { section = SettingsSection.NONE },
            modifier = modifier
        )

        SettingsSection.SIZES -> SizeManagementScreen(
            viewModel = viewModel,
            onBack = { section = SettingsSection.NONE },
            modifier = modifier
        )

        SettingsSection.NONE -> SettingsMenuScreen(
            onCategoriesClick = { section = SettingsSection.CATEGORIES },
            onSizesClick = { section = SettingsSection.SIZES },
            modifier = modifier
        )
    }
}

@Composable
private fun SettingsMenuScreen(
    onCategoriesClick: () -> Unit,
    onSizesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Pengaturan Toko",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Pilih pengaturan yang ingin kamu ubah",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingOptionCard(
            icon = Icons.Default.Category,
            title = "Kelola Kategori",
            description = "Tambah atau hapus kategori pakaian",
            onClick = onCategoriesClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingOptionCard(
            icon = Icons.Default.Straighten,
            title = "Kelola Ukuran",
            description = "Tambah atau hapus ukuran pakaian",
            onClick = onSizesClick
        )
    }
}

@Composable
private fun SettingOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}