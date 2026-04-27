package com.dinyairsadot.taxtracker.feature.invoice

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

private val strictDateFormatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("dd/MM/uuuu")
    .withResolverStyle(ResolverStyle.STRICT)

fun parseStrictDateOrNull(raw: String): LocalDate? {
    val text = raw.trim()
    if (text.isBlank()) return null
    return runCatching {
        LocalDate.parse(text, strictDateFormatter)
    }.getOrNull()
}
