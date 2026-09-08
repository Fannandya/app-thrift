package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.SaleTransaction
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale
import com.mamay.cobain.domain.DailySales
import com.mamay.cobain.domain.LowStockReport
import com.mamay.cobain.domain.TopSellingItem
import com.mamay.cobain.domain.dailySalesSeries
import com.mamay.cobain.domain.grossProfit
import com.mamay.cobain.domain.itemDiscountGiven
import com.mamay.cobain.domain.lowStock
import com.mamay.cobain.domain.revenueDelta
import com.mamay.cobain.domain.topSellingItems
import com.mamay.cobain.domain.transactionStats
import com.mamay.cobain.presentation.ui.components.AppCard
import com.mamay.cobain.presentation.ui.components.SalesChart
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel
import com.mamay.cobain.util.formatRupiah
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.days

enum class SalesRange(val label: String, val days: Long?, val chartDays: Int) {
    ALL("Semua", null, 30),
    WEEK("7 Hari", 7, 7),
    MONTH("30 Hari", 30, 30)
}

private val idLocale: Locale = Locale.forLanguageTag("id")

@Composable
fun DashboardScreen(
    viewModel: ThriftViewModel,
    onOpenCashier: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val profile by viewModel.storeProfile.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    var selectedRange by remember { mutableStateOf(SalesRange.ALL) }

    // Everything the dashboard shows is derived here, ONCE per real input change -
    // not on every recomposition / Vico animation frame / tab return.
    val data = remember(items, sales, transactions, selectedRange, profile.lowStockThreshold) {
        computeDashboardData(items, sales, transactions, selectedRange, profile.lowStockThreshold)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { DashboardHeaderCard(storeName = profile.storeName) }

        item { RangeChips(selected = selectedRange, onSelect = { selectedRange = it }) }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader("Status Inventaris", trailing = "Live SKU")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStatCard(
                        icon = Icons.Default.Inventory2,
                        badgeContainer = MaterialTheme.colorScheme.surfaceContainerHigh,
                        badgeContent = MaterialTheme.colorScheme.onSurfaceVariant,
                        value = data.totalItems.toString(),
                        label = "Total Item",
                        caption = "Gudang & Toko",
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        icon = Icons.Default.CheckCircle,
                        badgeContainer = MaterialTheme.colorScheme.secondaryContainer,
                        badgeContent = MaterialTheme.colorScheme.onSecondaryContainer,
                        value = data.availableItems.toString(),
                        label = "Tersedia",
                        caption = "Siap dipajang",
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        icon = Icons.Default.Sell,
                        badgeContainer = MaterialTheme.colorScheme.tertiaryContainer,
                        badgeContent = MaterialTheme.colorScheme.onTertiaryContainer,
                        value = data.soldItems.toString(),
                        label = "Terjual",
                        caption = "Items laku",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader("Kinerja Keuangan", trailing = "Mata Uang (IDR)")
                RevenueHeroCard(
                    total = data.totalRevenue,
                    delta = data.delta,
                    prevRevenue = data.prevRevenue,
                    rangeDays = selectedRange.days
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ValueCard(
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "Total Aset Masuk",
                        value = formatRupiah(data.totalInvestment),
                        caption = "Nilai beli stok aktif",
                        modifier = Modifier.weight(1f)
                    )
                    ValueCard(
                        icon = Icons.Default.MonetizationOn,
                        label = "Potensi Omzet",
                        value = formatRupiah(data.potentialRevenue),
                        caption = "Estimasi jual sisa stok",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            MetricGridCard(
                gross = data.gross,
                marginPct = data.marginPct,
                transactionCount = data.transactionCount,
                averageBasket = data.averageBasket,
                discountGiven = data.discountGiven
            )
        }

        item { SalesChartCard(series = data.series, peak = data.peak, rangeLabel = selectedRange.label) }

        item { TopSellingCard(top = data.top) }

        item { LowStockCard(report = data.low, categories = categories) }

        item { RecentTxCard(transactions = data.recent, onOpenHistory = onOpenHistory) }

        item { OpenPosBanner(onOpenCashier = onOpenCashier) }

        if (profile.storeName.isBlank()) {
            item {
                Text(
                    text = "Nama toko belum diisi. Buka Pengaturan → Profil Toko supaya nama toko " +
                        "muncul di sini dan di struk.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// --- Derivation ---------------------------------------------------------------

private data class RecentTransaction(
    val transactionId: String,
    val timestamp: Long,
    val itemCount: Int,
    val totalPrice: Long
)

private data class DashboardData(
    val totalItems: Int,
    val availableItems: Int,
    val soldItems: Int,
    val totalRevenue: Long,
    val totalInvestment: Long,
    val potentialRevenue: Long,
    val prevRevenue: Long?,
    val delta: Double?,
    val gross: Long,
    val marginPct: Int?,
    val transactionCount: Int,
    val averageBasket: Long,
    val discountGiven: Long,
    val top: List<TopSellingItem>,
    val low: LowStockReport,
    val series: List<DailySales>,
    val peak: DailySales?,
    val recent: List<RecentTransaction>
)

private fun computeDashboardData(
    items: List<ThriftItem>,
    sales: List<ThriftSale>,
    transactions: List<SaleTransaction>,
    range: SalesRange,
    lowStockThreshold: Int
): DashboardData {
    val windowMs = range.days?.let { it.days.inWholeMilliseconds }
    val now = System.currentTimeMillis()
    val cutoff = windowMs?.let { now - it }

    val filteredSales = if (cutoff == null) sales else sales.filter { it.timestamp >= cutoff }
    val filteredTransactions =
        if (cutoff == null) transactions else transactions.filter { it.timestamp >= cutoff }

    val unsoldItems = items.filter { !it.isSold }
    val availableItems = unsoldItems.sumOf { it.quantity }
    val soldItems = filteredSales.sumOf { it.quantity }

    val totalRevenue = filteredTransactions.sumOf { it.total.toLong() }
    val prevRevenue = windowMs?.let { w ->
        transactions.filter { it.timestamp in (now - 2 * w) until (now - w) }.sumOf { it.total.toLong() }
    }
    val delta = prevRevenue?.let { revenueDelta(totalRevenue, it) }

    val gross = grossProfit(filteredTransactions, filteredSales)
    val stats = transactionStats(filteredTransactions)
    val series = dailySalesSeries(transactions = transactions, days = range.chartDays)

    val recent = filteredSales
        .groupBy { it.transactionId }
        .map { (id, lines) ->
            RecentTransaction(
                transactionId = id,
                timestamp = lines.maxOf { it.timestamp },
                itemCount = lines.sumOf { it.quantity },
                totalPrice = lines.sumOf { it.totalPrice.toLong() }
            )
        }
        .sortedByDescending { it.timestamp }
        .take(3)

    return DashboardData(
        totalItems = availableItems + soldItems,
        availableItems = availableItems,
        soldItems = soldItems,
        totalRevenue = totalRevenue,
        totalInvestment = unsoldItems.sumOf { it.quantity.toLong() * it.buyPrice },
        potentialRevenue = unsoldItems.sumOf { it.quantity.toLong() * it.sellPrice },
        prevRevenue = prevRevenue,
        delta = delta,
        gross = gross,
        marginPct = if (totalRevenue > 0) (gross * 100 / totalRevenue).toInt() else null,
        transactionCount = stats.count,
        averageBasket = stats.average,
        discountGiven = itemDiscountGiven(filteredSales),
        top = topSellingItems(filteredSales),
        low = lowStock(items, lowStockThreshold),
        series = series,
        peak = series.filter { it.total > 0 }.maxByOrNull { it.total },
        recent = recent
    )
}

// --- Building blocks --------------------------------------------------------

@Composable
private fun DashboardCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) = AppCard(
    modifier = modifier,
    containerColor = containerColor,
    contentPadding = contentPadding,
    content = content
)

@Composable
private fun SectionHeader(title: String, trailing: String? = null) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Pill(
    text: String,
    container: Color,
    content: Color,
    leadingIcon: ImageVector? = null
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(container)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = content, modifier = Modifier.size(13.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = content)
    }
}

@Composable
private fun IconBadge(
    icon: ImageVector,
    container: Color,
    content: Color,
    boxSize: Dp = 32.dp
) {
    Box(
        modifier = Modifier
            .size(boxSize)
            .clip(RoundedCornerShape(10.dp))
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(boxSize * 0.55f))
    }
}

// --- Sections -------------------------------------------------------------

@Composable
private fun DashboardHeaderCard(storeName: String) {
    val now = remember { SimpleDateFormat("HH:mm", idLocale).format(Date()) }
    DashboardCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = storeName.ifBlank { "Toko Belum Diberi Nama" },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Icon(
                Icons.Default.Verified,
                contentDescription = "Toko terverifikasi",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Pill(
                text = "Live",
                container = MaterialTheme.colorScheme.surfaceContainerHigh,
                content = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Ringkasan performa penjualan",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Diperbarui otomatis",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Hari ini, $now WIB",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RangeChips(selected: SalesRange, onSelect: (SalesRange) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(SalesRange.entries) { range ->
            val isSelected = selected == range
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(range) },
                label = { Text(range.label) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else {
                    null
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun MiniStatCard(
    icon: ImageVector,
    badgeContainer: Color,
    badgeContent: Color,
    value: String,
    label: String,
    caption: String,
    modifier: Modifier = Modifier
) {
    DashboardCard(modifier = modifier, contentPadding = PaddingValues(12.dp)) {
        IconBadge(icon = icon, container = badgeContainer, content = badgeContent, boxSize = 28.dp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = caption,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RevenueHeroCard(
    total: Long,
    delta: Double?,
    prevRevenue: Long?,
    rangeDays: Long?
) {
    val scheme = MaterialTheme.colorScheme
    AppCard(
        containerColor = scheme.primary,
        borderColor = scheme.primary
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "TOTAL PENDAPATAN BERSIH",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onPrimary,
                modifier = Modifier.weight(1f)
            )
            if (delta != null) {
                Pill(
                    text = String.format(Locale.US, "%+.1f%%", delta),
                    container = scheme.onPrimary.copy(alpha = 0.16f),
                    content = scheme.onPrimary,
                    leadingIcon = if (delta >= 0) {
                        Icons.AutoMirrored.Filled.TrendingUp
                    } else {
                        Icons.AutoMirrored.Filled.TrendingDown
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = formatRupiah(total),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onPrimary
        )
        if (prevRevenue != null && rangeDays != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "vs $rangeDays hari sebelumnya (${formatRupiah(prevRevenue)})",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onPrimary.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun ValueCard(
    icon: ImageVector,
    label: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier
) {
    DashboardCard(modifier = modifier, contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = caption,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MetricGridCard(
    gross: Long,
    marginPct: Int?,
    transactionCount: Int,
    averageBasket: Long,
    discountGiven: Long
) {
    DashboardCard(contentPadding = PaddingValues(0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Analytics,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Metrik Operasional Kasir",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Pill(
                text = if (gross >= 0) "Sehat" else "Perlu perhatian",
                container = if (gross >= 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
                content = if (gross >= 0) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer
            )
        }
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCell(
                    label = "Laba Kotor",
                    value = formatRupiah(gross),
                    sub = marginPct?.let { "Margin $it%" } ?: "Margin —",
                    subIcon = Icons.Default.PieChart,
                    modifier = Modifier.weight(1f)
                )
                MetricCell(
                    label = "Jumlah Transaksi",
                    value = "$transactionCount Trx",
                    sub = "Nota selesai",
                    subIcon = Icons.AutoMirrored.Filled.ReceiptLong,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCell(
                    label = "Rata-rata Keranjang",
                    value = formatRupiah(averageBasket),
                    sub = "Avg Basket",
                    subIcon = Icons.Default.MonetizationOn,
                    modifier = Modifier.weight(1f)
                )
                MetricCell(
                    label = "Diskon Diberikan",
                    value = formatRupiah(discountGiven),
                    sub = "Voucher & promo",
                    subIcon = Icons.Default.Sell,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricCell(
    label: String,
    value: String,
    sub: String,
    subIcon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Icon(
                subIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SalesChartCard(
    series: List<DailySales>,
    peak: DailySales?,
    rangeLabel: String
) {
    DashboardCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Grafik Penjualan Harian",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Volume omzet ($rangeLabel)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Pill(
                text = "Chart",
                container = MaterialTheme.colorScheme.surfaceContainerHigh,
                content = MaterialTheme.colorScheme.onSurfaceVariant,
                leadingIcon = Icons.Default.BarChart
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        SalesChart(data = series)
        if (peak != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Puncak: ${peak.label}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatRupiah(peak.total),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun TopSellingCard(top: List<TopSellingItem>) {
    DashboardCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(
                icon = Icons.Default.LocalFireDepartment,
                container = MaterialTheme.colorScheme.surfaceContainerHigh,
                content = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.size(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Barang Terlaris",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Paling diminati pelanggan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (top.isNotEmpty()) {
                Pill(
                    text = "${top.size} Produk",
                    container = MaterialTheme.colorScheme.surfaceContainerHigh,
                    content = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        if (top.isEmpty()) {
            Text(
                text = "Belum ada penjualan pada rentang ini.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                top.forEachIndexed { index, entry ->
                    val unitPrice = if (entry.quantity > 0) entry.revenue / entry.quantity else 0L
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconBadge(
                            icon = Icons.Default.Inventory2,
                            container = MaterialTheme.colorScheme.surfaceContainerHigh,
                            content = MaterialTheme.colorScheme.onSurfaceVariant,
                            boxSize = 44.dp
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.name,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "${entry.quantity} pcs terjual · ${formatRupiah(unitPrice)}/pcs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatRupiah(entry.revenue),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "#${index + 1} Terlaris",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LowStockCard(
    report: LowStockReport,
    categories: List<ItemCategory>
) {
    val categoryName = remember(categories) { categories.associate { it.id to it.name } }
    DashboardCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(
                icon = Icons.Default.Warning,
                container = MaterialTheme.colorScheme.errorContainer,
                content = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.size(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Peringatan Stok Menipis",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Item perlu restock atau kurasi ulang",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (report.items.isNotEmpty()) {
                Pill(
                    text = "Restock",
                    container = MaterialTheme.colorScheme.errorContainer,
                    content = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        if (report.items.isEmpty()) {
            Text(
                text = "Semua stok aman.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                report.items.forEach { item ->
                    val dotColor = when {
                        item.quantity <= 0 -> MaterialTheme.colorScheme.error
                        item.quantity <= 1 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.outline
                    }
                    val pillText = when {
                        item.quantity <= 0 -> "Stok Habis"
                        item.quantity == 1 -> "Sisa 1 pcs (Kritis)"
                        else -> "Sisa ${item.quantity} pcs"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(dotColor)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                textDecoration = if (item.quantity <= 0) TextDecoration.LineThrough else null
                            )
                            Text(
                                text = "Kategori: ${categoryName[item.categoryId] ?: "-"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Pill(
                            text = pillText,
                            container = if (item.quantity <= 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer,
                            content = if (item.quantity <= 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentTxCard(
    transactions: List<RecentTransaction>,
    onOpenHistory: () -> Unit
) {
    DashboardCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Riwayat Transaksi Kasir",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onOpenHistory, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text("Lihat Semua", style = MaterialTheme.typography.labelSmall)
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (transactions.isEmpty()) {
            Text(
                text = "Belum ada transaksi. Layani penjualan dari menu Kasir.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                transactions.forEach { tx ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${tx.transactionId.take(8)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "  •  ${formatTxTime(tx.timestamp)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Pill(
                                text = "Selesai",
                                container = MaterialTheme.colorScheme.secondaryContainer,
                                content = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${tx.itemCount} item",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatRupiah(tx.totalPrice),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OpenPosBanner(onOpenCashier: () -> Unit) {
    DashboardCard(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PointOfSale,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Kasir Siap Transaksi",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Buka keranjang baru sekarang",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Button(onClick = onOpenCashier) {
                Text("Buka POS")
            }
        }
    }
}

private fun formatTxTime(ts: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = ts }
    val now = Calendar.getInstance()
    val sameDay = cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
        cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
    return if (sameDay) {
        "Hari ini, ${SimpleDateFormat("HH:mm", idLocale).format(Date(ts))}"
    } else {
        SimpleDateFormat("dd/MM HH:mm", idLocale).format(Date(ts))
    }
}
