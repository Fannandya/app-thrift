package com.mamay.cobain.presentation.ui

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mamay.cobain.CobainApplication
import com.mamay.cobain.domain.ExcelExportRequest
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class SettingsSection { NONE, STORE, CATEGORIES, ATTRIBUTES, DISCOUNTS }

@Composable
fun SettingsScreen(
    viewModel: ThriftViewModel,
    snackbarHostState: SnackbarHostState,
    onResetDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Nama enum, bukan enum-nya: rememberSaveable butuh tipe yang bisa masuk Bundle.
    // runCatching: string "SIZES" yang tersimpan dari versi lama tidak lagi valid.
    var sectionName by rememberSaveable { mutableStateOf(SettingsSection.NONE.name) }
    val section = runCatching { SettingsSection.valueOf(sectionName) }.getOrDefault(SettingsSection.NONE)

    when (section) {
        SettingsSection.STORE -> StoreProfileScreen(
            viewModel = viewModel,
            onBack = { sectionName = SettingsSection.NONE.name },
            modifier = modifier
        )

        SettingsSection.CATEGORIES -> CategoryManagementScreen(
            viewModel = viewModel,
            onBack = { sectionName = SettingsSection.NONE.name },
            modifier = modifier
        )

        SettingsSection.ATTRIBUTES -> AttributeManagementScreen(
            viewModel = viewModel,
            onBack = { sectionName = SettingsSection.NONE.name },
            modifier = modifier
        )

        SettingsSection.DISCOUNTS -> DiscountManagementScreen(
            viewModel = viewModel,
            onBack = { sectionName = SettingsSection.NONE.name },
            modifier = modifier
        )

        SettingsSection.NONE -> SettingsMenuScreen(
            viewModel = viewModel,
            snackbarHostState = snackbarHostState,
            onStoreClick = { sectionName = SettingsSection.STORE.name },
            onCategoriesClick = { sectionName = SettingsSection.CATEGORIES.name },
            onAttributesClick = { sectionName = SettingsSection.ATTRIBUTES.name },
            onDiscountsClick = { sectionName = SettingsSection.DISCOUNTS.name },
            onResetDone = onResetDone,
            modifier = modifier
        )
    }
}

@Composable
private fun SettingsMenuScreen(
    viewModel: ThriftViewModel,
    snackbarHostState: SnackbarHostState,
    onStoreClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onAttributesClick: () -> Unit,
    onDiscountsClick: () -> Unit,
    onResetDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val container = (context.applicationContext as CobainApplication).container
    val scope = rememberCoroutineScope()
    val itemTerm = viewModel.storeProfile.collectAsStateWithLifecycle().value.itemTerm
    val termLower = itemTerm.lowercase(Locale.forLanguageTag("id"))

    var showExportDialog by remember { mutableStateOf(false) }
    var pendingRequest by remember { mutableStateOf<ExcelExportRequest?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    val createDoc = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    ) { uri ->
        val request = pendingRequest
        pendingRequest = null
        if (uri != null && request != null) {
            scope.launch {
                val result = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        viewModel.writeExcelReport(request, stream).getOrThrow()
                    } ?: throw IOException("Tidak bisa membuka berkas tujuan")
                }
                snackbarHostState.showSnackbar(
                    if (result.isSuccess) "Ekspor Excel selesai" else "Gagal ekspor Excel"
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
            icon = Icons.Default.Storefront,
            title = "Profil Toko",
            description = "Nama, alamat, telepon, istilah barang, dan catatan struk",
            onClick = onStoreClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingOptionCard(
            icon = Icons.Default.Category,
            title = "Kelola Kategori",
            description = "Tambah atau hapus kategori $termLower",
            onClick = onCategoriesClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingOptionCard(
            icon = Icons.Default.Tune,
            title = "Kelola Atribut",
            description = "Atribut khusus untuk $termLower (ukuran, warna, dll.)",
            onClick = onAttributesClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingOptionCard(
            icon = Icons.Default.LocalOffer,
            title = "Kelola Diskon",
            description = "Diskon persen berjangka waktu untuk $termLower tertentu",
            onClick = onDiscountsClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingOptionCard(
            icon = Icons.Default.Backup,
            title = "Cadangkan Data",
            description = "Simpan salinan database ke penyimpanan aplikasi",
            onClick = {
                scope.launch {
                    val file = runCatching { container.exportDatabase() }.getOrNull()
                    snackbarHostState.showSnackbar(
                        if (file == null) {
                            "Gagal mencadangkan data."
                        } else {
                            "Cadangan tersimpan: ${file.absolutePath}"
                        }
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingOptionCard(
            icon = Icons.Default.GridOn,
            title = "Ekspor ke Excel",
            description = "Unduh inventaris & laporan penjualan sebagai .xlsx",
            onClick = { showExportDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingOptionCard(
            icon = Icons.Default.RestartAlt,
            title = "Reset Toko",
            description = "Hapus semua data & mulai dari awal",
            onClick = { showResetDialog = true },
            danger = true
        )
    }

    if (showExportDialog) {
        ExportDialog(
            viewModel = viewModel,
            onDismiss = { showExportDialog = false },
            onExport = { request ->
                pendingRequest = request
                showExportDialog = false
                val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
                createDoc.launch("cobain-ekspor-$stamp.xlsx")
            }
        )
    }

    if (showResetDialog) {
        ResetStoreDialog(
            onDismiss = { showResetDialog = false },
            onConfirm = {
                showResetDialog = false
                scope.launch {
                    runCatching { container.resetStore() }
                        .onSuccess {
                            viewModel.notifyStoreReset()
                            onResetDone()
                        }
                        .onFailure { snackbarHostState.showSnackbar("Gagal mereset toko.") }
                }
            }
        )
    }
}

@Composable
private fun ResetStoreDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    val canReset = input.trim() == "RESET"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Toko") },
        text = {
            Column {
                Text(
                    "Semua barang, transaksi, kategori, atribut, diskon, dan profil toko " +
                        "akan dihapus permanen. Tindakan ini tidak bisa dibatalkan."
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Ketik RESET untuk lanjut") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = canReset,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Reset Toko")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun SettingOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    danger: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (danger) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                tint = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (danger) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (danger) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
