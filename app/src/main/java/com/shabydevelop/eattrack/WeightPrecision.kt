package com.shabydevelop.eattrack

internal fun parseWeightGrams(input: String): Int? =
    input.replace(',','.').toBigDecimalOrNull()?.movePointRight(3)?.let { runCatching { it.intValueExact() }.getOrNull() }

internal fun editableWeightKg(grams: Int): String =
    java.math.BigDecimal.valueOf(grams.toLong(), 3).stripTrailingZeros().toPlainString()
