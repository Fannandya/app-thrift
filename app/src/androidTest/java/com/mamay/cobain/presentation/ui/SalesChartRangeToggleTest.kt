package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mamay.cobain.data.entity.SaleTransaction
import com.mamay.cobain.domain.dailySalesSeries
import com.mamay.cobain.presentation.ui.components.SalesChart
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Reproduces the "7 Hari" force close: opening on a 30-bucket chart then switching
 * to 7 buckets and back used to crash Vico's scroll/zoom layout. The chart must
 * survive every switch.
 *
 * The bucket count is driven directly from a MutableState (no tap synthesis), but
 * the Compose test rule still bridges `waitForIdle()` to Espresso. espresso-core
 * 3.5.1 cannot initialise its InputManager strategy on API 36 emulator images
 * (`NoSuchMethodException: InputManager.getInstance`), so run this on an API <= 35
 * device/emulator, or bump espresso-core once a fix ships.
 */
@RunWith(AndroidJUnit4::class)
class SalesChartRangeToggleTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun switchingBucketCountDoesNotCrash() {
        val now = 1_757_000_000_000L
        val transactions = List(20) { i ->
            SaleTransaction(
                id = "tx$i", timestamp = now - i.toLong() * 24 * 60 * 60 * 1000,
                subtotal = 10_000, discountType = "NONE", discountValue = 0, discountAmount = 0,
                total = 10_000, paidAmount = 10_000, changeAmount = 0
            )
        }
        lateinit var days: MutableState<Int>

        rule.setContent {
            days = remember { mutableStateOf(30) }
            val current by days
            Column(modifier = Modifier.testTag("chart")) {
                SalesChart(data = dailySalesSeries(transactions = transactions, days = current, now = now))
            }
        }

        rule.onNodeWithTag("chart").assertExists()
        listOf(7, 30, 7, 30).forEach { target ->
            rule.runOnUiThread { days.value = target }
            rule.waitForIdle()
            rule.onNodeWithTag("chart").assertExists()
        }
    }
}
