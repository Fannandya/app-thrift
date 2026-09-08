package com.mamay.cobain.presentation.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * Single-button modal dialog for one-shot notices ("operation succeeded"). The
 * sibling of [ConfirmDialog] without the cancel path: it only acknowledges.
 */
@Composable
fun MessageDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    confirmLabel: String = "OK"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(confirmLabel)
            }
        }
    )
}
