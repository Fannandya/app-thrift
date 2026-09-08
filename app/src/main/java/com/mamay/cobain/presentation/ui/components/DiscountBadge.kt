package com.mamay.cobain.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mamay.cobain.data.entity.Discount
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val badgeDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id"))

/** "Diskon 20% s/d 30 Sep 2026" pill for an item that has a linked discount. */
@Composable
fun DiscountBadge(discount: Discount, modifier: Modifier = Modifier) {
    val endLabel = badgeDateFormat.format(Date(discount.endMillis - 1))
    Text(
        text = "Diskon ${discount.percent}% s/d $endLabel",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = modifier
            .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}
