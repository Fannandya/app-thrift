package com.mamay.cobain.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Shared read-only selection dropdown for id/name option lists (ItemCategory,
 * ItemSize). AddItemDialog and EditItemDialog used to each define their own copy of
 * this for size and for category (four near-identical composables) - one generic
 * version keeps them from drifting out of sync.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> IdNameDropdown(
    label: String,
    options: List<T>,
    selectedId: Int?,
    idOf: (T) -> Int,
    nameOf: (T) -> String,
    emptyOptionsLabel: String,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.find { idOf(it) == selectedId }?.let(nameOf) ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        TextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(emptyOptionsLabel) },
                    onClick = { expanded = false }
                )
            } else {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(nameOf(option)) },
                        onClick = {
                            onSelected(idOf(option))
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
