package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.mamay.cobain.data.entity.ThriftItem

@Composable
fun ThriftItemCard(
    item: ThriftItem,
    onItemClick: (ThriftItem) -> Unit,
    onDeleteClick: (ThriftItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .clickable { onItemClick(item) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Ukuran: ${item.size} · Kategori: ${item.category.ifBlank { "-" }} · Jumlah: ${item.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Rp${item.sellPrice}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (item.isSold) "Terjual" else "Tersedia",
                style = MaterialTheme.typography.labelSmall,
                color = if (item.isSold)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .background(
                        if (item.isSold)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.tertiaryContainer,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        IconButton(onClick = { onDeleteClick(item) }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Hapus item",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
