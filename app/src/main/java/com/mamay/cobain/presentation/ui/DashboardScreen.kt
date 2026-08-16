package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel
import com.mamay.cobain.util.formatRupiah
import kotlin.time.Duration.Companion.days

enum class SalesRange(val label: String, val days: Long?) {
    ALL("Semua", null),
    WEEK("7 Hari", 7),
    MONTH("30 Hari", 30)
}

@Composable
fun DashboardScreen(
    viewModel: ThriftViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.items.collectAsState()
    val sales by viewModel.sales.collectAsState()

    var selectedRange by remember { mutableStateOf(SalesRange.ALL) }

    val cutoff = selectedRange.days?.let { System.currentTimeMillis() - it.days.inWholeMilliseconds }
    val filteredSales = if (cutoff == null) {
        sales
    } else {
        sales.filter { it.timestamp >= cutoff }
    }

    val availableItems = items.sumOf { it.quantity }
    val soldItems = filteredSales.sumOf { it.quantity }
    val totalItems = availableItems + soldItems
    val totalRevenue = filteredSales.sumOf { it.totalPrice.toLong() }
    val totalInvestment = items.sumOf { it.quantity.toLong() * it.buyPrice }
    val potentialRevenue = items.filter { !it.isSold }.sumOf { it.quantity.toLong() * it.sellPrice }
    val recentSales = filteredSales.sortedByDescending { it.timestamp }.take(5)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Ringkasan Inventaris",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Pantau performa penjualan pakaian thrift-mu",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SalesRange.entries) { range ->
                FilterChip(
                    selected = selectedRange == range,
                    onClick = { selectedRange = range },
                    label = { Text(range.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            StatCard(
                icon = Icons.Default.Checklist,
                label = "Total Item",
                value = totalItems.toString(),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatCard(
                icon = Icons.Default.Inventory2,
                label = "Tersedia",
                value = availableItems.toString(),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatCard(
                icon = Icons.Default.Sell,
                label = "Terjual",
                value = soldItems.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Keuangan",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row {
            StatCard(
                icon = Icons.Default.AttachMoney,
                label = "Total Pendapatan",
                value = formatRupiah(totalRevenue),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatCard(
                icon = Icons.Default.ShoppingCart,
                label = "Total Asset",
                value = formatRupiah(totalInvestment),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        StatCard(
            icon = Icons.Default.Storefront,
            label = "Potensi Pendapatan (belum terjual)",
            value = formatRupiah(potentialRevenue),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Transaksi Terbaru",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (recentSales.isEmpty()) {
            Text(
                text = "Belum ada transaksi. Layani penjualan dari menu Kasir.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(
                modifier = Modifier
                    .shadow(2.dp, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp)
            ) {
                recentSales.forEachIndexed { index, sale ->
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sale.itemName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ukuran ${sale.size} · Jumlah ${sale.quantity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "+${formatRupiah(sale.totalPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}