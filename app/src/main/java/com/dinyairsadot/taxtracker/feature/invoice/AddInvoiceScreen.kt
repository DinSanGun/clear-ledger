package com.dinyairsadot.taxtracker.feature.invoice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.dinyairsadot.taxtracker.R
import com.dinyairsadot.taxtracker.core.domain.PaymentMethodOption
import com.dinyairsadot.taxtracker.core.domain.PaymentStatus
import com.dinyairsadot.taxtracker.core.domain.ServicePeriodMode
import com.dinyairsadot.taxtracker.core.ui.categoryTopAppBarColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

enum class Currency {
    USD,
    ILS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvoiceScreen(
    categoryId: Long,
    categoryName: String?,
    categoryColorHex: String?,
    categoryCustomFieldTitles: List<String>,
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
        numberOfPayments: String?,
        confirmationNumber: String?,
        vendorName: String?,
        notes: String,
        customFieldValues: List<String>
    ) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Main required fields
    var documentNumberText by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var paymentStatus by rememberSaveable { mutableStateOf(PaymentStatus.NOT_PAID) }
    var servicePeriodStartText by rememberSaveable { mutableStateOf("") }
    var servicePeriodEndText by rememberSaveable { mutableStateOf("") }
    var servicePeriodMode by rememberSaveable { mutableStateOf(ServicePeriodMode.MONTH) }

    val today = LocalDate.now()
    var startYear by rememberSaveable { mutableStateOf(today.year) }
    var startMonth by rememberSaveable { mutableStateOf(today.monthValue) }
    var endYear by rememberSaveable { mutableStateOf(today.year) }
    var endMonth by rememberSaveable { mutableStateOf(today.monthValue) }
    var showEndMonth by rememberSaveable { mutableStateOf(false) }

    // Conditional fields: Paid/Credit group
    var paymentDateText by rememberSaveable { mutableStateOf("") }
    var paymentMethod by rememberSaveable { mutableStateOf("") }
    var numberOfPayments by rememberSaveable { mutableStateOf("") }
    var confirmationNumber by rememberSaveable { mutableStateOf("") }

    // Conditional fields: Not paid group
    var dueDateText by rememberSaveable { mutableStateOf("") }

    var currency by rememberSaveable { mutableStateOf(Currency.ILS) }
    var vendorName by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var customFieldValues by rememberSaveable {
        mutableStateOf(List(categoryCustomFieldTitles.size) { "" })
    }

    // Validation state
    var documentNumberError by rememberSaveable { mutableStateOf<String?>(null) }
    var amountError by rememberSaveable { mutableStateOf<String?>(null) }
    var paymentStatusError by rememberSaveable { mutableStateOf<String?>(null) }
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
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        if (documentNumberText.trim().isBlank()) {
            documentNumberError = context.getString(R.string.invoice_number_required)
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

        paymentStatusError = null

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
                finalStartText = LocalDate.of(startYear, startMonth, 1).format(dateFormatter)
                finalEndText = YearMonth.of(actualEndYear, actualEndMonth).atEndOfMonth().format(dateFormatter)
            }
        } else {
            val trimmedStart = servicePeriodStartText.trim()
            val trimmedEnd = servicePeriodEndText.trim()
            if (trimmedStart.isBlank() && trimmedEnd.isBlank()) {
                servicePeriodStartError = null
                servicePeriodEndError = null
                finalStartText = ""
                finalEndText = ""
            } else {
                val startDate = if (trimmedStart.isBlank()) {
                    servicePeriodStartError = context.getString(R.string.date_required)
                    hasError = true
                    null
                } else {
                    val d = runCatching { LocalDate.parse(trimmedStart, dateFormatter) }.getOrNull()
                    if (d == null) {
                        servicePeriodStartError = context.getString(R.string.use_format_dd_mm_yyyy)
                        hasError = true
                    } else {
                        servicePeriodStartError = null
                    }
                    d
                }

                val endDate = if (trimmedEnd.isBlank()) {
                    servicePeriodEndError = context.getString(R.string.date_required)
                    hasError = true
                    null
                } else {
                    val d = runCatching { LocalDate.parse(trimmedEnd, dateFormatter) }.getOrNull()
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
        }

        if (hasError) return

        val paymentDate = paymentDateText.trim().takeIf { it.isNotBlank() }
            ?.let { runCatching { LocalDate.parse(it, dateFormatter) }.getOrNull() }
        val dueDate = dueDateText.trim().takeIf { it.isNotBlank() }
            ?.let { runCatching { LocalDate.parse(it, dateFormatter) }.getOrNull() }

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
            numberOfPayments.takeIf { it.isNotBlank() },
            confirmationNumber.takeIf { it.isNotBlank() },
            vendorName.trim().takeIf { it.isNotBlank() },
            notes.trim(),
            customFieldValues
        )
        onNavigateBack()
    }

    val showPaidGroup = paymentStatus == PaymentStatus.PAID
    val showDueDateGroup = paymentStatus == PaymentStatus.NOT_PAID

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_invoice_title)) },
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

            // ── Main section ──
            // Invoice number *
            val documentNumberBringIntoViewRequester = remember { BringIntoViewRequester() }
            OutlinedTextField(
                value = documentNumberText,
                onValueChange = {
                    documentNumberText = it
                    documentNumberTouched = true
                    documentNumberError = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(documentNumberBringIntoViewRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                // Wait briefly for the IME to appear so we scroll to the right position.
                                delay(250)
                                documentNumberBringIntoViewRequester.bringIntoView()
                            }
                        } else if (documentNumberTouched) {
                            documentNumberError = if (documentNumberText.trim().isBlank()) {
                                context.getString(R.string.invoice_number_required)
                            } else null
                        }
                    },
                label = { Text("${stringResource(R.string.invoice_number)} *") },
                isError = documentNumberError != null,
                supportingText = documentNumberError?.let { err -> { Text(err) } }
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Amount due *
            val amountBringIntoViewRequester = remember { BringIntoViewRequester() }
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    amountTouched = true
                    amountError = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(amountBringIntoViewRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                delay(250)
                                amountBringIntoViewRequester.bringIntoView()
                            }
                        } else if (amountTouched) {
                            val amt = amountText.toDoubleOrNull()
                            amountError = if (amt == null || amt <= 0.0) {
                                context.getString(R.string.enter_valid_amount)
                            } else null
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
                                Text(
                                    if (currency == Currency.USD) stringResource(R.string.currency_usd)
                                    else stringResource(R.string.currency_ils)
                                )
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Service period (optional)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.service_period_mode_label) + ":",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
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
                startYear = startYear,
                startMonth = startMonth,
                onStartMonthSelected = { y, m -> startYear = y; startMonth = m; startMonthError = null },
                startMonthError = startMonthError,
                showEndMonth = showEndMonth,
                onToggleEndMonth = {
                    showEndMonth = !showEndMonth
                    if (!showEndMonth) {
                        endYear = startYear
                        endMonth = startMonth
                        endMonthError = null
                    }
                },
                endYear = endYear,
                endMonth = endMonth,
                onEndMonthSelected = { y, m -> endYear = y; endMonth = m; endMonthError = null },
                endMonthError = endMonthError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Payment status *
            PaymentStatusSelector(
                selected = paymentStatus,
                onSelectedChange = { paymentStatus = it }
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // ── Conditional: Paid ──
            if (showPaidGroup) {
                PaymentMethodSelector(
                    selected = paymentMethod,
                    onSelected = { paymentMethod = it },
                    modifier = Modifier.fillMaxWidth()
                )
                if (paymentMethod == PaymentMethodOption.CREDIT.value) {
                    Spacer(modifier = Modifier.padding(top = 8.dp))
                    val numberOfPaymentsBringIntoViewRequester = remember { BringIntoViewRequester() }
                    OutlinedTextField(
                        value = numberOfPayments,
                        onValueChange = { numberOfPayments = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(numberOfPaymentsBringIntoViewRequester)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        delay(250)
                                        numberOfPaymentsBringIntoViewRequester.bringIntoView()
                                    }
                                }
                            },
                        placeholder = { Text(stringResource(R.string.number_of_payments_hint)) }
                    )
                }
                Spacer(modifier = Modifier.padding(top = 8.dp))

                val confirmationNumberBringIntoViewRequester = remember { BringIntoViewRequester() }
                OutlinedTextField(
                    value = confirmationNumber,
                    onValueChange = { confirmationNumber = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(confirmationNumberBringIntoViewRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                coroutineScope.launch {
                                    delay(250)
                                    confirmationNumberBringIntoViewRequester.bringIntoView()
                                }
                            }
                        },
                    label = { Text(stringResource(R.string.confirmation_number)) }
                )
                Spacer(modifier = Modifier.padding(top = 8.dp))

                ExactDateField(
                    value = paymentDateText,
                    onValueChange = { paymentDateText = it },
                    label = stringResource(R.string.payment_date_label_hint),
                    error = null,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(top = 8.dp))
            }

            // ── Conditional: Not paid ──
            if (showDueDateGroup) {
                ExactDateField(
                    value = dueDateText,
                    onValueChange = { dueDateText = it },
                    label = stringResource(R.string.due_date_label_hint),
                    error = null,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(top = 8.dp))
            }

            // Custom fields
            if (categoryCustomFieldTitles.isNotEmpty()) {
                categoryCustomFieldTitles.forEachIndexed { index, fieldTitle ->
                    Spacer(modifier = Modifier.padding(top = 8.dp))
                    val customFieldBringIntoViewRequester = remember { BringIntoViewRequester() }
                    OutlinedTextField(
                        value = customFieldValues.getOrNull(index) ?: "",
                        onValueChange = { newValue ->
                            val newList = customFieldValues.toMutableList()
                            while (newList.size <= index) newList.add("")
                            newList[index] = newValue
                            customFieldValues = newList
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(customFieldBringIntoViewRequester)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        delay(250)
                                        customFieldBringIntoViewRequester.bringIntoView()
                                    }
                                }
                            },
                        label = { Text(fieldTitle) }
                    )
                }
                Spacer(modifier = Modifier.padding(top = 8.dp))
            }

            // ── Bottom optional fields ──
            val vendorBringIntoViewRequester = remember { BringIntoViewRequester() }
            OutlinedTextField(
                value = vendorName,
                onValueChange = { vendorName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(vendorBringIntoViewRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                delay(250)
                                vendorBringIntoViewRequester.bringIntoView()
                            }
                        }
                    },
                label = { Text(stringResource(R.string.vendor_name)) }
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            val notesBringIntoViewRequester = remember { BringIntoViewRequester() }
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(notesBringIntoViewRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                delay(250)
                                notesBringIntoViewRequester.bringIntoView()
                            }
                        }
                    },
                label = { Text(stringResource(R.string.notes_additional_info)) },
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
                Text(stringResource(R.string.save_invoice))
            }

            // Small bottom buffer so user can scroll Save button above keyboard
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodSelector(
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val density = LocalDensity.current
    var textFieldWidth by remember { mutableStateOf(0.dp) }
    val displayText = when (selected) {
        PaymentMethodOption.CREDIT.value -> stringResource(R.string.payment_method_credit)
        PaymentMethodOption.BANK_TRANSFER.value -> stringResource(R.string.payment_method_bank_transfer)
        PaymentMethodOption.OTHER.value -> stringResource(R.string.payment_method_other)
        else -> ""
    }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldWidth = with(density) { coordinates.size.width.toDp() }
                },
            label = { Text(stringResource(R.string.payment_method)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(textFieldWidth)
        ) {
            DropdownMenuItem(
                text = { Text("—") },
                onClick = {
                    onSelected("")
                    expanded = false
                }
            )
            PaymentMethodOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            when (option) {
                                PaymentMethodOption.CREDIT -> stringResource(R.string.payment_method_credit)
                                PaymentMethodOption.BANK_TRANSFER -> stringResource(R.string.payment_method_bank_transfer)
                                PaymentMethodOption.OTHER -> stringResource(R.string.payment_method_other)
                            }
                        )
                    },
                    onClick = {
                        onSelected(option.value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PaymentStatusSelector(
    selected: PaymentStatus,
    onSelectedChange: (PaymentStatus) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${stringResource(R.string.status)} *",
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(end = 8.dp)
        )
        TextButton(onClick = { onSelectedChange(PaymentStatus.NOT_PAID) }) {
            Text(
                text = stringResource(R.string.not_paid),
                fontWeight = if (selected == PaymentStatus.NOT_PAID) FontWeight.Bold else FontWeight.Normal
            )
        }
        TextButton(onClick = { onSelectedChange(PaymentStatus.PAID) }) {
            Text(
                text = stringResource(R.string.paid),
                fontWeight = if (selected == PaymentStatus.PAID) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
