package com.dinyairsadot.taxtracker.feature.invoice

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dinyairsadot.taxtracker.R
import com.dinyairsadot.taxtracker.core.domain.ServicePeriodMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.Locale

/**
 * Renders the "Service Period" section for invoice add/edit screens.
 *
 * The [mode] is fixed per category (Policy A) and is never changeable per invoice.
 *
 * DATE mode  – two free-text + calendar-picker fields (Start / End).
 * MONTH mode – a single month picker for the start month; an optional end-month
 *              picker revealed by "Add end month" button.
 *
 * All mutable state is hoisted to the caller so that it survives ViewModel-driven
 * recompositions and orientation changes via rememberSaveable.
 */
@Composable
fun ServicePeriodInput(
    mode: ServicePeriodMode,

    // ── DATE mode ────────────────────────────────────────────────────────────
    startDateText: String,
    onStartDateTextChange: (String) -> Unit,
    startDateError: String?,
    onStartDateTouched: () -> Unit,
    endDateText: String,
    onEndDateTextChange: (String) -> Unit,
    endDateError: String?,
    onEndDateTouched: () -> Unit,

    // ── MONTH mode ───────────────────────────────────────────────────────────
    startYear: Int,
    startMonth: Int,          // 1-12
    onStartMonthSelected: (year: Int, month: Int) -> Unit,
    startMonthError: String?,
    showEndMonth: Boolean,
    onToggleEndMonth: () -> Unit,
    endYear: Int,
    endMonth: Int,            // 1-12
    onEndMonthSelected: (year: Int, month: Int) -> Unit,
    endMonthError: String?,

    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Column(modifier = modifier) {
        when (mode) {
            ServicePeriodMode.DATE -> DateModeContent(
                startDateText = startDateText,
                onStartDateTextChange = onStartDateTextChange,
                startDateError = startDateError,
                onStartDateTouched = onStartDateTouched,
                endDateText = endDateText,
                onEndDateTextChange = onEndDateTextChange,
                endDateError = endDateError,
                onEndDateTouched = onEndDateTouched,
                dateFormatter = dateFormatter,
                context = context
            )

            ServicePeriodMode.MONTH -> MonthModeContent(
                startYear = startYear,
                startMonth = startMonth,
                onStartMonthSelected = onStartMonthSelected,
                startMonthError = startMonthError,
                showEndMonth = showEndMonth,
                onToggleEndMonth = onToggleEndMonth,
                endYear = endYear,
                endMonth = endMonth,
                onEndMonthSelected = onEndMonthSelected,
                endMonthError = endMonthError
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DATE mode
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DateModeContent(
    startDateText: String,
    onStartDateTextChange: (String) -> Unit,
    startDateError: String?,
    onStartDateTouched: () -> Unit,
    endDateText: String,
    onEndDateTextChange: (String) -> Unit,
    endDateError: String?,
    onEndDateTouched: () -> Unit,
    dateFormatter: DateTimeFormatter,
    context: android.content.Context
) {
    fun showPicker(currentText: String, onPicked: (String) -> Unit) {
        val cal = java.util.Calendar.getInstance()
        currentText.trim().takeIf { it.isNotBlank() }?.let {
            runCatching {
                val d = LocalDate.parse(it, dateFormatter)
                cal.set(d.year, d.monthValue - 1, d.dayOfMonth)
            }
        }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                onPicked(LocalDate.of(year, month + 1, day).format(dateFormatter))
            },
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH),
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Start
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = startDateText,
            onValueChange = {
                onStartDateTextChange(it)
                onStartDateTouched()
            },
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { fs ->
                    if (!fs.isFocused) onStartDateTouched()
                },
            label = { Text(stringResource(R.string.service_period_start_short)) },
            supportingText = {
                if (startDateError != null) Text(startDateError)
                else Text(stringResource(R.string.format_hint_dd_mm_yyyy))
            },
            isError = startDateError != null
        )
        IconButton(onClick = { showPicker(startDateText, onStartDateTextChange) }) {
            Icon(Icons.Filled.CalendarToday, contentDescription = stringResource(R.string.pick_date))
        }
    }

    // End
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = endDateText,
            onValueChange = {
                onEndDateTextChange(it)
                onEndDateTouched()
            },
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { fs ->
                    if (!fs.isFocused) onEndDateTouched()
                },
            label = { Text(stringResource(R.string.service_period_end_short)) },
            supportingText = {
                if (endDateError != null) Text(endDateError)
                else Text(stringResource(R.string.format_hint_dd_mm_yyyy))
            },
            isError = endDateError != null
        )
        IconButton(onClick = { showPicker(endDateText, onEndDateTextChange) }) {
            Icon(Icons.Filled.CalendarToday, contentDescription = stringResource(R.string.pick_date))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MONTH mode
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MonthModeContent(
    startYear: Int,
    startMonth: Int,
    onStartMonthSelected: (year: Int, month: Int) -> Unit,
    startMonthError: String?,
    showEndMonth: Boolean,
    onToggleEndMonth: () -> Unit,
    endYear: Int,
    endMonth: Int,
    onEndMonthSelected: (year: Int, month: Int) -> Unit,
    endMonthError: String?
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    if (showStartPicker) {
        MonthPickerDialog(
            initialYear = startYear,
            initialMonth = startMonth,
            onDismiss = { showStartPicker = false },
            onConfirm = { y, m ->
                onStartMonthSelected(y, m)
                showStartPicker = false
            }
        )
    }
    if (showEndPicker) {
        MonthPickerDialog(
            initialYear = endYear,
            initialMonth = endMonth,
            onDismiss = { showEndPicker = false },
            onConfirm = { y, m ->
                onEndMonthSelected(y, m)
                showEndPicker = false
            }
        )
    }

    val locale = Locale.getDefault()
    fun monthLabel(year: Int, month: Int): String =
        YearMonth.of(year, month).let {
            "${it.month.getDisplayName(TextStyle.SHORT, locale)} $year"
        }

    // Start month field
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = monthLabel(startYear, startMonth),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.weight(1f),
            label = null,
            isError = startMonthError != null,
            supportingText = { startMonthError?.let { Text(it) } }
        )
        IconButton(onClick = { showStartPicker = true }) {
            Icon(Icons.Filled.CalendarToday, contentDescription = stringResource(R.string.pick_month))
        }
    }

    // End month field (only visible when range is active)
    if (showEndMonth) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = monthLabel(endYear, endMonth),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.weight(1f),
                label = null,
                isError = endMonthError != null,
                supportingText = { endMonthError?.let { Text(it) } }
            )
            IconButton(onClick = { showEndPicker = true }) {
                Icon(Icons.Filled.CalendarToday, contentDescription = stringResource(R.string.pick_month))
            }
        }
    }

    // Toggle button
    TextButton(
        onClick = onToggleEndMonth,
        modifier = Modifier.padding(start = 0.dp)
    ) {
        Text(
            if (showEndMonth) stringResource(R.string.remove_end_month)
            else stringResource(R.string.add_end_month)
        )
    }
}

/**
 * Optional exact-date field for payment date / due date.
 * Uses OutlinedTextField + DatePickerDialog. No monthly mode toggle.
 */
@Composable
fun ExactDateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun showPicker(currentText: String, onPicked: (String) -> Unit) {
        val cal = java.util.Calendar.getInstance()
        currentText.trim().takeIf { it.isNotBlank() }?.let {
            runCatching {
                val d = LocalDate.parse(it, dateFormatter)
                cal.set(d.year, d.monthValue - 1, d.dayOfMonth)
            }
        }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                onPicked(LocalDate.of(year, month + 1, day).format(dateFormatter))
            },
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH),
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            label = { Text(label) },
            supportingText = {
                if (error != null) Text(error)
                else Text(stringResource(R.string.format_hint_dd_mm_yyyy))
            },
            isError = error != null
        )
        IconButton(onClick = { showPicker(value, onValueChange) }) {
            Icon(Icons.Filled.CalendarToday, contentDescription = stringResource(R.string.pick_date))
        }
    }
}
