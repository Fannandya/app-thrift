package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ItemSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    categories: List<ItemCategory>,
    sizes: List<ItemSize>,
    onDismiss: () -> Unit,
    onSave: (name: String, size: String, category: String, quantity: Int, buyPrice: Int, sellPrice: Int) -> Unit
) {
    val name = remember { mutableStateOf("") }
    val size = remember { mutableStateOf("") }
    val selectedCategory = remember { mutableStateOf("") }
    val quantity = remember { mutableStateOf("1") }
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
                SizeDropdown(
                    sizes = sizes,
                    selectedSize = size.value,
                    onSizeSelected = { size.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
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
                    val quantityInt = quantity.value.toIntOrNull() ?: 1
                    if (name.value.isNotEmpty() && size.value.isNotEmpty()) {
                        onSave(name.value, size.value, selectedCategory.value, quantityInt, buyPriceInt, sellPriceInt)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SizeDropdown(
    sizes: List<ItemSize>,
    selectedSize: String,
    onSizeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        TextField(
            value = selectedSize,
            onValueChange = {},
            readOnly = true,
            label = { Text("Ukuran") },
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
            if (sizes.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Belum ada ukuran") },
                    onClick = { expanded = false }
                )
            } else {
                sizes.forEach { size ->
                    DropdownMenuItem(
                        text = { Text(size.name) },
                        onClick = {
                            onSizeSelected(size.name)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
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