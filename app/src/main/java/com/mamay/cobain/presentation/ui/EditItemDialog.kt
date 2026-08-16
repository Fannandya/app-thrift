package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mamay.cobain.data.entity.ThriftItem

@Composable
fun EditItemDialog(
    item: ThriftItem,
    onDismiss: () -> Unit,
    onSave: (ThriftItem) -> Unit
) {
    val name = remember { mutableStateOf(item.name) }
    val size = remember { mutableStateOf(item.size) }
    val buyPrice = remember { mutableStateOf(item.buyPrice.toString()) }
    val sellPrice = remember { mutableStateOf(item.sellPrice.toString()) }
    val isSold = remember { mutableStateOf(item.isSold) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Barang") },
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
                    label = { Text("Ukuran") },
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
                    modifier = Modifier.padding(bottom = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSold.value,
                        onCheckedChange = { isSold.value = it }
                    )
                    Text(
                        text = "Tandai sebagai Terjual",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val buyPriceInt = buyPrice.value.toIntOrNull() ?: item.buyPrice
                    val sellPriceInt = sellPrice.value.toIntOrNull() ?: item.sellPrice
                    val updatedItem = item.copy(
                        name = name.value,
                        size = size.value,
                        buyPrice = buyPriceInt,
                        sellPrice = sellPriceInt,
                        isSold = isSold.value
                    )
                    onSave(updatedItem)
                    onDismiss()
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
