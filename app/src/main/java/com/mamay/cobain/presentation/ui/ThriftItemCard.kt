package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.util.formatRupiah

@Composable
fun ThriftItemCard(
    item: ThriftItem,
    categoryName: String,
    sizeName: String,
    onItemClick: (ThriftItem) -> Unit
) {
    Card(
        onClick = { onItemClick(item) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Ukuran: ${sizeName.ifBlank { "-" }} · Kategori: ${categoryName.ifBlank { "-" }} · Jumlah: ${item.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatRupiah(item.sellPrice),
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
    }
}
