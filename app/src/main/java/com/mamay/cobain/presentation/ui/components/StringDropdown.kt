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
 * Read-only selection dropdown over a plain list of strings - the sibling of
 * [IdNameDropdown] for [com.mamay.cobain.data.entity.AttributeMode.LIST] attribute
 * options, whose values are stored as text rather than id/name pairs. A blank
 * "(kosong)" entry lets the user clear an optional attribute.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StringDropdown(
    label: String,
    options: List<String>,
    selected: String?,
    emptyOptionsLabel: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    allowClear: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        TextField(
            value = selected.orEmpty(),
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
                if (allowClear) {
                    DropdownMenuItem(
                        text = { Text("(kosong)") },
                        onClick = {
                            onSelected("")
                            expanded = false
                        }
                    )
                }
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
