package com.mamay.cobain.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mamay.cobain.data.entity.AttributeMode
import com.mamay.cobain.data.entity.ItemAttribute
import com.mamay.cobain.data.entity.ItemAttributeOption

/**
 * Renders one input per attribute definition, writing into [values] (attributeId ->
 * raw value). LIST attributes get a [StringDropdown] over their options; FREE_TEXT
 * attributes get a plain field. A required attribute's label is suffixed with " *".
 */
@Composable
fun AttributeInputs(
    attributes: List<ItemAttribute>,
    options: List<ItemAttributeOption>,
    values: SnapshotStateMap<Int, String>,
    modifier: Modifier = Modifier
) {
    attributes.sortedBy { it.displayOrder }.forEach { attr ->
        val label = attr.name + if (attr.required) " *" else ""
        when (runCatching { AttributeMode.valueOf(attr.mode) }.getOrDefault(AttributeMode.FREE_TEXT)) {
            AttributeMode.LIST -> StringDropdown(
                label = label,
                options = options.filter { it.attributeId == attr.id }.map { it.value },
                selected = values[attr.id],
                emptyOptionsLabel = "Belum ada pilihan",
                onSelected = { values[attr.id] = it },
                allowClear = !attr.required,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            AttributeMode.FREE_TEXT -> TextField(
                value = values[attr.id].orEmpty(),
                onValueChange = { values[attr.id] = it },
                label = { androidx.compose.material3.Text(label) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }
    }
}

/** True when every required attribute has a non-blank value in [values]. */
fun requiredAttributesFilled(attributes: List<ItemAttribute>, values: Map<Int, String>): Boolean =
    attributes.filter { it.required }.all { values[it.id].orEmpty().isNotBlank() }
