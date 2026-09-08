package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mamay.cobain.data.entity.ItemAttribute
import com.mamay.cobain.data.entity.ItemAttributeOption
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.presentation.ui.components.AttributeInputs
import com.mamay.cobain.presentation.ui.components.IdNameDropdown
import com.mamay.cobain.presentation.ui.components.requiredAttributesFilled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemDialog(
    item: ThriftItem,
    categories: List<ItemCategory>,
    attributes: List<ItemAttribute>,
    attributeOptions: List<ItemAttributeOption>,
    initialAttributeValues: Map<Int, String>,
    itemTerm: String,
    onDismiss: () -> Unit,
    onSave: (ThriftItem, attributeValues: Map<Int, String>) -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var categoryId by remember { mutableStateOf(item.categoryId) }
    var quantity by remember { mutableStateOf(item.quantity.toString()) }
    var buyPrice by remember { mutableStateOf(item.buyPrice.toString()) }
    var sellPrice by remember { mutableStateOf(item.sellPrice.toString()) }
    var isSold by remember { mutableStateOf(item.isSold) }
    val attributeValues = remember { mutableStateMapOf<Int, String>().apply { putAll(initialAttributeValues) } }

    val quantityInt = quantity.toIntOrNull() ?: 0
    val buyPriceInt = buyPrice.toIntOrNull() ?: 0
    val sellPriceInt = sellPrice.toIntOrNull() ?: 0
    val isValid = name.isNotBlank() && quantityInt > 0 && requiredAttributesFilled(attributes, attributeValues)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit $itemTerm") },
        text = {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama $itemTerm") },
                    modifier = Modifier.padding(bottom = 8.dp),
                    singleLine = true
                )
                IdNameDropdown(
                    label = "Kategori",
                    options = categories,
                    selectedId = categoryId,
                    idOf = { it.id },
                    nameOf = { it.name },
                    emptyOptionsLabel = "Belum ada kategori",
                    onSelected = { categoryId = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                AttributeInputs(
                    attributes = attributes,
                    options = attributeOptions,
                    values = attributeValues
                )
                TextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { char -> char.isDigit() } },
                    label = { Text("Jumlah") },
                    modifier = Modifier.padding(bottom = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                TextField(
                    value = buyPrice,
                    onValueChange = { buyPrice = it.filter { char -> char.isDigit() } },
                    label = { Text("Harga Beli") },
                    modifier = Modifier.padding(bottom = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                TextField(
                    value = sellPrice,
                    onValueChange = { sellPrice = it.filter { char -> char.isDigit() } },
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
                        checked = isSold,
                        onCheckedChange = { isSold = it }
                    )
                    Text(
                        text = "Tandai sebagai Terjual",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                if (!isValid) {
                    Text(
                        text = "Nama $itemTerm dan jumlah (lebih dari 0) wajib diisi. " +
                            "Atribut bertanda * juga wajib.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    onSave(
                        item.copy(
                            name = name,
                            categoryId = categoryId,
                            quantity = quantityInt,
                            buyPrice = buyPriceInt,
                            sellPrice = sellPriceInt,
                            isSold = isSold
                        ),
                        attributeValues.toMap()
                    )
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
