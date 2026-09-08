package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mamay.cobain.data.entity.Discount
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.domain.applyPercent
import com.mamay.cobain.presentation.ui.components.DiscountBadge
import com.mamay.cobain.util.formatRupiah

@Composable
fun ThriftItemCard(
    item: ThriftItem,
    categoryName: String,
    attributesText: String,
    activeDiscount: Discount?,
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
                text = "${attributesText.ifBlank { "-" }} · Kategori: ${categoryName.ifBlank { "-" }} · Jumlah: ${item.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (activeDiscount != null) {
                Text(
                    text = formatRupiah(item.sellPrice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.LineThrough
                )
                Text(
                    text = formatRupiah(applyPercent(item.sellPrice, activeDiscount.percent)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                DiscountBadge(activeDiscount)
            } else {
                Text(
                    text = formatRupiah(item.sellPrice),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
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
