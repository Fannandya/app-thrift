package com.mamay.cobain.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel

/**
 * The shop's own identity. Everything here is what the app prints on receipts and
 * shows in screen headers, so none of it is hardcoded anywhere else.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreProfileScreen(
    viewModel: ThriftViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val profile by viewModel.storeProfile.collectAsStateWithLifecycle()

    var storeName by remember { mutableStateOf(profile.storeName) }
    var address by remember { mutableStateOf(profile.address) }
    var phone by remember { mutableStateOf(profile.phone) }
    var receiptFooter by remember { mutableStateOf(profile.receiptFooter) }
    var itemTerm by remember { mutableStateOf(profile.itemTerm) }
    var lowStockThreshold by remember { mutableStateOf(profile.lowStockThreshold.toString()) }

    // The profile Flow emits after the first composition, so the fields start empty
    // and have to be refilled once the stored values actually arrive.
    LaunchedEffect(profile) {
        storeName = profile.storeName
        address = profile.address
        phone = profile.phone
        receiptFooter = profile.receiptFooter
        itemTerm = profile.itemTerm
        lowStockThreshold = profile.lowStockThreshold.toString()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Profil Toko") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Data ini dipakai sebagai judul di seluruh layar dan sebagai kepala struk.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text("Nama Toko") },
                singleLine = true,
                isError = storeName.isBlank(),
                supportingText = if (storeName.isBlank()) {
                    { Text("Nama toko wajib diisi") }
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Alamat") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Nomor Telepon / WA") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = receiptFooter,
                onValueChange = { receiptFooter = it },
                label = { Text("Catatan Bawah Struk") },
                singleLine = true,
                placeholder = { Text("Terima kasih sudah belanja!") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = itemTerm,
                onValueChange = { itemTerm = it },
                label = { Text("Istilah Barang") },
                singleLine = true,
                placeholder = { Text("Barang / Produk / Sepatu") },
                supportingText = { Text("Dipakai di label & judul, mis. \"Nama ${itemTerm.ifBlank { "Barang" }}\".") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = lowStockThreshold,
                onValueChange = { lowStockThreshold = it.filter { c -> c.isDigit() } },
                label = { Text("Ambang Stok Menipis") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text("Barang dengan sisa jumlah di bawah/sama dengan angka ini muncul di Dashboard.") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.saveStoreProfile(
                        storeName,
                        address,
                        phone,
                        receiptFooter,
                        itemTerm,
                        lowStockThreshold.toIntOrNull() ?: 0
                    )
                    onBack()
                },
                enabled = storeName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan")
            }
        }
    }
}
