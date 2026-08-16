package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, size: String, buyPrice: Int, sellPrice: Int) -> Unit
) {
    val name = remember { mutableStateOf("") }
    val size = remember { mutableStateOf("") }
    val buyPrice = remember { mutableStateOf("") }
    val sellPrice = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Barang Baru") },
        text = {
            Column(modifier = Modifier.padding(8.dp)) {
                TextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Nama Pakaian") },
                    modifier = Modifier.padding(bottom = 8.dp),
                    singleLine = true
                )
                TextField(
                    value = size.value,
                    onValueChange = { size.value = it },
                    label = { Text("Ukuran (M, L, XL, dll)") },
                    modifier = Modifier.padding(bottom = 8.dp),
                    singleLine = true
                )
                TextField(
                    value = buyPrice.value,
                    onValueChange = { buyPrice.value = it },
                    label = { Text("Harga Beli") },
                    modifier = Modifier.padding(bottom = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                TextField(
                    value = sellPrice.value,
                    onValueChange = { sellPrice.value = it },
                    label = { Text("Harga Jual") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val buyPriceInt = buyPrice.value.toIntOrNull() ?: 0
                    val sellPriceInt = sellPrice.value.toIntOrNull() ?: 0
                    if (name.value.isNotEmpty() && size.value.isNotEmpty()) {
                        onSave(name.value, size.value, buyPriceInt, sellPriceInt)
                        onDismiss()
                    }
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
