package com.dinyairsadot.taxtracker.feature.invoice

import com.dinyairsadot.taxtracker.core.domain.ServicePeriodMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Formats the service period for display based on [ServicePeriodMode].
 *
 * - MONTH mode: "January 2026" (single month) or "January 2026 – February 2026" (range)
 * - DATE mode: Locale-aware exact dates (e.g. "1 Jan 2026 – 15 Feb 2026")
 *
 * Uses [invoice.servicePeriodMode] as source of truth; never infers mode from dates.
 *
 * @param startText Start date in dd/MM/yyyy format (or first day of month for MONTH mode)
 * @param endText End date in dd/MM/yyyy format (or last day of month for MONTH mode)
 * @param mode Stored invoice.servicePeriodMode
 * @param locale Locale for month/date formatting (defaults to system locale)
 * @return Formatted string, or null if dates cannot be parsed
 */
fun formatServicePeriodForDisplay(
    startText: String?,
    endText: String?,
    mode: ServicePeriodMode,
    locale: Locale = Locale.getDefault()
): String? {
    if (startText.isNullOrBlank() || endText.isNullOrBlank()) return null

    val parseFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val startDate = runCatching { LocalDate.parse(startText.trim(), parseFmt) }.getOrNull() ?: return null
    val endDate = runCatching { LocalDate.parse(endText.trim(), parseFmt) }.getOrNull() ?: return null

    return when (mode) {
        ServicePeriodMode.MONTH -> {
            val startYm = YearMonth.from(startDate)
            val endYm = YearMonth.from(endDate)
            val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
            if (startYm == endYm) {
                startYm.format(monthFmt)
            } else {
                "${startYm.format(monthFmt)} – ${endYm.format(monthFmt)}"
            }
        }
        ServicePeriodMode.DATE -> {
            val dateFmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
            val startStr = startDate.format(dateFmt)
            val endStr = endDate.format(dateFmt)
            if (startStr == endStr) startStr else "$startStr – $endStr"
        }
    }
}
