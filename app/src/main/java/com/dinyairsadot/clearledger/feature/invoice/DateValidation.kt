package com.dinyairsadot.clearledger.feature.invoice

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

private val strictDateFormatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("dd/MM/uuuu")
    .withResolverStyle(ResolverStyle.STRICT)

private val dateFormatRegex = Regex("^\\d{2}/\\d{2}/\\d{4}$")

sealed interface DateValidationResult {
    data object Blank : DateValidationResult
    data object FormatError : DateValidationResult
    data object InvalidDate : DateValidationResult
    data class Valid(val value: LocalDate) : DateValidationResult
}

fun validateDateInput(raw: String): DateValidationResult {
    val text = raw.trim()
    if (text.isBlank()) return DateValidationResult.Blank
    if (!dateFormatRegex.matches(text)) return DateValidationResult.FormatError

    val parsed = runCatching {
        LocalDate.parse(text, strictDateFormatter)
    }.getOrNull() ?: return DateValidationResult.InvalidDate

    return DateValidationResult.Valid(parsed)
}

fun parseStrictDateOrNull(raw: String): LocalDate? {
    return when (val result = validateDateInput(raw)) {
        DateValidationResult.Blank -> null
        DateValidationResult.FormatError -> null
        DateValidationResult.InvalidDate -> null
        is DateValidationResult.Valid -> result.value
    }
}
