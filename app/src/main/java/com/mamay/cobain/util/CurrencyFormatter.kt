package com.mamay.cobain.util

/**
 * Formats Rupiah with the conventional Indonesian '.' thousands grouping (e.g.
 * "Rp15.000"). Deliberately not java.text.NumberFormat/Locale-based: that ties the
 * output to the device's system locale, so the same amount would render differently
 * (or with a decimal comma) depending on the user's phone settings.
 */
fun formatRupiah(amount: Int): String = formatRupiah(amount.toLong())

fun formatRupiah(amount: Long): String {
    val isNegative = amount < 0
    val digits = kotlin.math.abs(amount).toString()
    val grouped = digits.reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
    return (if (isNegative) "-Rp" else "Rp") + grouped
}
