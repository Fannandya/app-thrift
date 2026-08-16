package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ThriftItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemDialog(
    item: ThriftItem,
    categories: List<ItemCategory>,
    onDismiss: () -> Unit,
    onSave: (ThriftItem) -> Unit
) {
    val name = remember { mutableStateOf(item.name) }
    val size = remember { mutableStateOf(item.size) }
    val selectedCategory = remember { mutableStateOf(item.category) }
    val quantity = remember { mutableStateOf(item.quantity.toString()) }
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
                CategoryDropdown(
                    categories = categories,
                    selectedCategory = selectedCategory.value,
                    onCategorySelected = { selectedCategory.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                TextField(
                    value = quantity.value,
                    onValueChange = { quantity.value = it.filter { char -> char.isDigit() } },
                    label = { Text("Jumlah") },
                    modifier = Modifier.padding(bottom = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    val quantityInt = quantity.value.toIntOrNull() ?: item.quantity
                    val updatedItem = item.copy(
                        name = name.value,
                        size = size.value,
                        category = selectedCategory.value,
                        quantity = quantityInt,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<ItemCategory>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        TextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            label = { Text("Kategori") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (categories.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Belum ada kategori") },
                    onClick = { expanded = false }
                )
            } else {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            onCategorySelected(category.name)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}