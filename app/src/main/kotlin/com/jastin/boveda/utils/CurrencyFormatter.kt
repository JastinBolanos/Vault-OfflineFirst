package com.jastin.boveda.utils

import kotlin.math.abs
import kotlin.math.round

/* =========================================================================
 * FINANCIAL FORMATTING UTILITY (PURE FUNCTION)
 * Centralizes currency presentation logic. Being a pure function,
 * it ensures that views (UI) remain agnostic to string logic.
 * ========================================================================= */
fun formatMoney(amount: Double): String {
    val isNegative = amount < 0
    val absoluteAmount = abs(amount)
    val rounded = round(absoluteAmount * 100) / 100.0
    val parts = rounded.toString().split(".")
    val integerPart = parts[0]
    val decimalPart = if (parts.size > 1) parts[1].padEnd(2, '0') else "00"
    val formattedInteger = integerPart.reversed().chunked(3).joinToString(",").reversed()
    val sign = if (isNegative) "-" else ""
    return ("$sign $ $formattedInteger.$decimalPart").trim()
}