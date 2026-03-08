package com.dinyairsadot.taxtracker.feature.invoice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dinyairsadot.taxtracker.R
import com.dinyairsadot.taxtracker.core.domain.PaymentStatus
import com.dinyairsadot.taxtracker.core.domain.ServicePeriodMode
import com.dinyairsadot.taxtracker.core.ui.categoryTopAppBarColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInvoiceScreen(
    invoiceId: Long,
    categoryColorHex: String?,
    categoryCustomFieldTitles: List<String>,
    initialServicePeriodMode: ServicePeriodMode,
    initialDocumentNumber: String,
    initialAmount: String,
    initialPaymentStatus: PaymentStatus,
    initialServicePeriodStartText: String,
    initialServicePeriodEndText: String,
    initialPaymentDateText: String?,
    initialDueDateText: String?,
    initialPaymentMethod: String?,
    initialConfirmationNumber: String?,
    initialNotes: String,
    initialCustomFieldValues: List<String>,
    onNavigateBack: () -> Unit,
    onSaveInvoice: (
        documentNumber: String,
        amountDue: Double,
        paymentStatus: PaymentStatus,
        servicePeriodStartText: String,
        servicePeriodEndText: String,
        servicePeriodMode: ServicePeriodMode,
        paymentDate: LocalDate?,
        dueDate: LocalDate?,
        paymentMethod: String?,
        confirmationNumber: String?,
        notes: String,
        customFieldValues: List<String>
    ) -> Unit
) {
    val context = LocalContext.current
    
    // Core required fields
    var documentNumberText by rememberSaveable { mutableStateOf(initialDocumentNumber) }
    var amountText by rememberSaveable { mutableStateOf(initialAmount) }
    var paymentStatus by rememberSaveable { mutableStateOf(initialPaymentStatus) }
    var servicePeriodStartText by rememberSaveable { mutableStateOf(initialServicePeriodStartText) }
    var servicePeriodEndText by rememberSaveable { mutableStateOf(initialServicePeriodEndText) }
    var paymentDateText by rememberSaveable { mutableStateOf(initialPaymentDateText ?: "") }
    var dueDateText by rememberSaveable { mutableStateOf(initialDueDateText ?: "") }
    var showDueDate by rememberSaveable { mutableStateOf(initialDueDateText != null) }
    var servicePeriodMode by rememberSaveable { mutableStateOf(initialServicePeriodMode) }

    // MONTH mode state – parse initial dates so the pickers pre-populate correctly
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val today = LocalDate.now()
    val parsedStart = remember {
        initialServicePeriodStartText.trim().let {
            runCatching { LocalDate.parse(it, dateFormatter) }.getOrNull()
        } ?: today
    }
    val parsedEnd = remember {
        initialServicePeriodEndText.trim().let {
            runCatching { LocalDate.parse(it, dateFormatter) }.getOrNull()
        } ?: parsedStart
    }
    var startYear by rememberSaveable { mutableStateOf(parsedStart.year) }
    var startMonth by rememberSaveable { mutableStateOf(parsedStart.monthValue) }
    var endYear by rememberSaveable { mutableStateOf(parsedEnd.year) }
    var endMonth by rememberSaveable { mutableStateOf(parsedEnd.monthValue) }
    var showEndMonth by rememberSaveable {
        mutableStateOf(parsedEnd.year != parsedStart.year || parsedEnd.monthValue != parsedStart.monthValue)
    }

    // Optional core fields
    var paymentMethod by rememberSaveable { mutableStateOf(initialPaymentMethod ?: "") }
    var confirmationNumber by rememberSaveable { mutableStateOf(initialConfirmationNumber ?: "") }
    var notes by rememberSaveable { mutableStateOf(initialNotes) }
    
    var currency by rememberSaveable { mutableStateOf(Currency.ILS) }
    var customFieldValues by rememberSaveable {
        mutableStateOf(
            categoryCustomFieldTitles.mapIndexed { index, _ ->
                initialCustomFieldValues.getOrNull(index) ?: ""
            }
        )
    }

    // Validation state
    var documentNumberError by rememberSaveable { mutableStateOf<String?>(null) }
    var amountError by rememberSaveable { mutableStateOf<String?>(null) }
    var servicePeriodStartError by rememberSaveable { mutableStateOf<String?>(null) }
    var servicePeriodEndError by rememberSaveable { mutableStateOf<String?>(null) }
    var startMonthError by rememberSaveable { mutableStateOf<String?>(null) }
    var endMonthError by rememberSaveable { mutableStateOf<String?>(null) }

    var documentNumberTouched by rememberSaveable { mutableStateOf(false) }
    var amountTouched by rememberSaveable { mutableStateOf(false) }
    var servicePeriodStartTouched by rememberSaveable { mutableStateOf(false) }
    var servicePeriodEndTouched by rememberSaveable { mutableStateOf(false) }
    
    fun handleSave() {
        var hasError = false
        val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        if (documentNumberText.trim().isBlank()) {
            documentNumberError = context.getString(R.string.document_number_required)
            hasError = true
        } else {
            documentNumberError = null
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            amountError = context.getString(R.string.enter_valid_amount)
            hasError = true
        } else {
            amountError = null
        }

        var finalStartText = ""
        var finalEndText = ""

        if (servicePeriodMode == ServicePeriodMode.MONTH) {
            val actualEndYear = if (showEndMonth) endYear else startYear
            val actualEndMonth = if (showEndMonth) endMonth else startMonth
            if (showEndMonth) {
                val startYM = YearMonth.of(startYear, startMonth)
                val endYM = YearMonth.of(actualEndYear, actualEndMonth)
                if (endYM.isBefore(startYM)) {
                    endMonthError = context.getString(R.string.service_period_end_before_start)
                    hasError = true
                } else {
                    endMonthError = null
                }
            } else {
                endMonthError = null
            }
            startMonthError = null
            if (!hasError) {
                finalStartText = LocalDate.of(startYear, startMonth, 1).format(fmt)
                finalEndText = YearMonth.of(actualEndYear, actualEndMonth).atEndOfMonth().format(fmt)
            }
        } else {
            val trimmedStart = servicePeriodStartText.trim()
            val startDate = if (trimmedStart.isBlank()) {
                servicePeriodStartError = context.getString(R.string.date_required)
                hasError = true
                null
            } else {
                val d = runCatching { LocalDate.parse(trimmedStart, fmt) }.getOrNull()
                if (d == null) {
                    servicePeriodStartError = context.getString(R.string.use_format_dd_mm_yyyy)
                    hasError = true
                } else {
                    servicePeriodStartError = null
                }
                d
            }

            val trimmedEnd = servicePeriodEndText.trim()
            val endDate = if (trimmedEnd.isBlank()) {
                servicePeriodEndError = context.getString(R.string.date_required)
                hasError = true
                null
            } else {
                val d = runCatching { LocalDate.parse(trimmedEnd, fmt) }.getOrNull()
                if (d == null) {
                    servicePeriodEndError = context.getString(R.string.use_format_dd_mm_yyyy)
                    hasError = true
                } else {
                    servicePeriodEndError = null
                }
                d
            }

            if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
                servicePeriodEndError = context.getString(R.string.service_period_end_before_start)
                hasError = true
            }

            finalStartText = trimmedStart
            finalEndText = trimmedEnd
        }

        if (hasError) return

        val paymentDate = paymentDateText.trim().takeIf { it.isNotBlank() }
            ?.let { runCatching { LocalDate.parse(it, fmt) }.getOrNull() }
        val dueDate = dueDateText.trim().takeIf { it.isNotBlank() }
            ?.let { runCatching { LocalDate.parse(it, fmt) }.getOrNull() }

        onSaveInvoice(
            documentNumberText.trim(),
            amount!!,
            paymentStatus,
            finalStartText,
            finalEndText,
            servicePeriodMode,
            paymentDate,
            dueDate,
            paymentMethod.takeIf { it.isNotBlank() },
            confirmationNumber.takeIf { it.isNotBlank() },
            notes.trim(),
            customFieldValues
        )
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_invoice_title)) },
                colors = categoryTopAppBarColors(categoryColorHex),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Document Number (REQUIRED)
            OutlinedTextField(
                value = documentNumberText,
                onValueChange = { 
                    documentNumberText = it
                    documentNumberTouched = true
                    if (documentNumberError != null) {
                        documentNumberError = null
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused && documentNumberTouched) {
                            if (documentNumberText.trim().isBlank()) {
                                documentNumberError = context.getString(R.string.document_number_required)
                            } else {
                                documentNumberError = null
                            }
                        }
                    },
                label = { Text("${stringResource(R.string.document_number)} *") },
                isError = documentNumberError != null,
                supportingText = { documentNumberError?.let { Text(it) } }
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Amount Due (REQUIRED) with currency selector
            OutlinedTextField(
                value = amountText,
                onValueChange = { 
                    amountText = it
                    amountTouched = true
                    if (amountError != null) {
                        amountError = null
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused && amountTouched) {
                            val amount = amountText.toDoubleOrNull()
                            if (amount == null || amount <= 0.0) {
                                amountError = context.getString(R.string.enter_valid_amount)
                            } else {
                                amountError = null
                            }
                        }
                    },
                label = { Text("${stringResource(R.string.amount_due)} *") },
                isError = amountError != null,
                supportingText = { amountError?.let { Text(it) } },
                trailingIcon = {
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .width(56.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outline)
                                .align(Alignment.CenterStart)
                        )
                        TextButton(
                            onClick = {
                                currency = if (currency == Currency.USD) Currency.ILS else Currency.USD
                            },
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 4.dp, bottomEnd = 4.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (currency == Currency.USD) stringResource(R.string.currency_usd) else stringResource(R.string.currency_ils))
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Service period mode selector (per-invoice)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.service_period_mode_label) + ":",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
                TextButton(onClick = { servicePeriodMode = ServicePeriodMode.MONTH }) {
                    Text(
                        stringResource(R.string.service_period_mode_month),
                        fontWeight = if (servicePeriodMode == ServicePeriodMode.MONTH) FontWeight.Bold else FontWeight.Normal
                    )
                }
                TextButton(onClick = { servicePeriodMode = ServicePeriodMode.DATE }) {
                    Text(
                        stringResource(R.string.service_period_mode_dates),
                        fontWeight = if (servicePeriodMode == ServicePeriodMode.DATE) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            ServicePeriodInput(
                mode = servicePeriodMode,
                // DATE mode
                startDateText = servicePeriodStartText,
                onStartDateTextChange = {
                    servicePeriodStartText = it
                    servicePeriodStartError = null
                },
                startDateError = servicePeriodStartError,
                onStartDateTouched = { servicePeriodStartTouched = true },
                endDateText = servicePeriodEndText,
                onEndDateTextChange = {
                    servicePeriodEndText = it
                    servicePeriodEndError = null
                },
                endDateError = servicePeriodEndError,
                onEndDateTouched = { servicePeriodEndTouched = true },
                // MONTH mode
                startYear = startYear,
                startMonth = startMonth,
                onStartMonthSelected = { y, m -> startYear = y; startMonth = m; startMonthError = null },
                startMonthError = startMonthError,
                showEndMonth = showEndMonth,
                onToggleEndMonth = {
                    showEndMonth = !showEndMonth
                    if (!showEndMonth) { endYear = startYear; endMonth = startMonth; endMonthError = null }
                },
                endYear = endYear,
                endMonth = endMonth,
                onEndMonthSelected = { y, m -> endYear = y; endMonth = m; endMonthError = null },
                endMonthError = endMonthError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Payment Status (REQUIRED)
            PaymentStatusSelector(
                selected = paymentStatus,
                onSelectedChange = { paymentStatus = it }
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Due date (optional, exact-date only, hidden by default)
            if (showDueDate) {
                ExactDateField(
                    value = dueDateText,
                    onValueChange = { dueDateText = it },
                    label = stringResource(R.string.due_date_label_hint),
                    error = null,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(top = 8.dp))
            } else {
                TextButton(onClick = { showDueDate = true }) {
                    Text(stringResource(R.string.add_due_date))
                }
                Spacer(modifier = Modifier.padding(top = 8.dp))
            }

            // Payment Method (OPTIONAL)
            OutlinedTextField(
                value = paymentMethod,
                onValueChange = { paymentMethod = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.payment_method)) }
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Confirmation Number (OPTIONAL)
            OutlinedTextField(
                value = confirmationNumber,
                onValueChange = { confirmationNumber = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.confirmation_number)) }
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Payment date (optional, exact-date only, shown by default)
            ExactDateField(
                value = paymentDateText,
                onValueChange = { paymentDateText = it },
                label = stringResource(R.string.payment_date_label_hint),
                error = null,
                modifier = Modifier.fillMaxWidth()
            )

            // Custom fields
            if (categoryCustomFieldTitles.isNotEmpty()) {
                Spacer(modifier = Modifier.padding(top = 8.dp))
                
                categoryCustomFieldTitles.forEachIndexed { index, fieldTitle ->
                    Spacer(modifier = Modifier.padding(top = 8.dp))
                    OutlinedTextField(
                        value = customFieldValues.getOrNull(index) ?: "",
                        onValueChange = { newValue ->
                            val newList = customFieldValues.toMutableList()
                            while (newList.size <= index) {
                                newList.add("")
                            }
                            newList[index] = newValue
                            customFieldValues = newList
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(fieldTitle) }
                    )
                }
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.notes)) },
                minLines = 3
            )

            Spacer(modifier = Modifier.padding(top = 16.dp))

            Button(
                onClick = { handleSave() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(stringResource(R.string.save_changes))
            }
        }
    }
}
