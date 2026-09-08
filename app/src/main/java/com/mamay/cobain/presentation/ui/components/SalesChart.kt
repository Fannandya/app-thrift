package com.mamay.cobain.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mamay.cobain.domain.DailySales
import com.mamay.cobain.util.formatRupiah
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries

/**
 * Daily revenue as columns. Falls back to a sentence when there is nothing to plot:
 * an axis-only empty chart reads like a rendering bug to a shop owner.
 */
@Composable
fun SalesChart(data: List<DailySales>, modifier: Modifier = Modifier) {
    if (data.isEmpty() || data.all { it.total == 0L }) {
        Text(
            text = "Belum ada penjualan pada rentang ini.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
        return
    }

    // key/remember on data.size: switching the Dashboard range shrinks the model
    // (30 -> 7 buckets). Without rebuilding the producer and scroll state, Vico
    // 2.1.0 re-applies the retained end-scroll/zoom (sized for 30 wide columns) to
    // a 7-bucket model that fits on screen, and the layout math divides by a
    // non-positive max-scroll distance -> crash. A fresh producer + scroll state per
    // bucket count sidesteps it; a 7-bucket chart also starts at the left since it
    // already fits.
    key(data.size) {
        val modelProducer = remember(data.size) { CartesianChartModelProducer() }
        LaunchedEffect(data) {
            modelProducer.runTransaction {
                columnSeries { series(data.map { it.total }) }
            }
        }

        ProvideVicoTheme(rememberM3VicoTheme()) {
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(
                        valueFormatter = CartesianValueFormatter { _, value, _ ->
                            formatRupiah(value.toLong())
                        }
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = CartesianValueFormatter { _, value, _ ->
                            data.getOrNull(value.toInt())?.label.orEmpty()
                        }
                    )
                ),
                modelProducer = modelProducer,
                scrollState = rememberVicoScrollState(
                    initialScroll = if (data.size <= 7) Scroll.Absolute.Start else Scroll.Absolute.End
                ),
                modifier = modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}
