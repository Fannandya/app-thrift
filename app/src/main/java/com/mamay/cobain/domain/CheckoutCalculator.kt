package com.mamay.cobain.domain

/** How a cashier expressed the discount: a flat Rupiah amount or a percentage. */
enum class DiscountType { NONE, AMOUNT, PERCENT }

data class CheckoutTotals(
    val subtotal: Int,
    val discountAmount: Int,
    val total: Int,
    val changeAmount: Int,
    val isPaidEnough: Boolean
)

/**
 * Pure money math for one checkout, deliberately free of Android and Room so the
 * edge cases (over-100% discounts, discounts larger than the bill, underpayment,
 * junk input) are covered by fast JVM tests instead of being discovered at the till.
 *
 * Everything is clamped rather than rejected: a cashier mistyping a discount should
 * see a sane total, not a negative one.
 */
fun calculateCheckoutTotals(
    subtotal: Int,
    discountType: DiscountType,
    discountValue: Int,
    paidAmount: Int
): CheckoutTotals {
    val safeSubtotal = subtotal.coerceAtLeast(0)
    val safePaid = paidAmount.coerceAtLeast(0)
    val rawDiscount = when (discountType) {
        DiscountType.NONE -> 0
        DiscountType.AMOUNT -> discountValue
        // Split as (a/100)*p + (a%100)*p/100 rather than a*p/100: a 25-million-Rupiah
        // bill times 100 already overflows Int, and the naive form would silently
        // produce a negative discount. Integer division truncates, so a fractional
        // Rupiah always favours the shop.
        DiscountType.PERCENT -> {
            val percent = discountValue.coerceIn(0, 100)
            safeSubtotal / 100 * percent + safeSubtotal % 100 * percent / 100
        }
    }
    val discountAmount = rawDiscount.coerceIn(0, safeSubtotal)
    val total = safeSubtotal - discountAmount
    return CheckoutTotals(
        subtotal = safeSubtotal,
        discountAmount = discountAmount,
        total = total,
        changeAmount = (safePaid - total).coerceAtLeast(0),
        isPaidEnough = safePaid >= total
    )
}
