package com.dinyairsadot.taxtracker.feature.invoice

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dinyairsadot.taxtracker.R
import com.dinyairsadot.taxtracker.core.domain.DocumentType
import com.dinyairsadot.taxtracker.core.domain.PaymentStatus
import com.dinyairsadot.taxtracker.core.ui.categoryTopAppBarColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInvoiceScreen(
    invoiceId: Long,
    categoryColorHex: String?,
    categoryCustomFieldTitles: List<String>,
    initialVendorName: String?,
    initialIssueDateText: String?,
    initialAmount: String,
    initialDueDateText: String,
    initialPaymentStatus: PaymentStatus,
    initialPaymentDateText: String?,
    initialServicePeriodStartText: String?,
    initialServicePeriodEndText: String?,
    initialNotes: String,
    initialCustomFieldValues: List<String>,
    initialDocumentType: DocumentType?,
    onNavigateBack: () -> Unit,
    onSaveInvoice: (
        vendorName: String?,
        issueDateText: String?,
        dueDateText: String,
        amount: Double,
        paymentStatus: PaymentStatus,
        paymentDateText: String?,
        servicePeriodStartText: String?,
        servicePeriodEndText: String?,
        notes: String,
        customFieldValues: List<String>,
        documentType: DocumentType?
    ) -> Unit
) {
    val context = LocalContext.current
    var vendorNameText by rememberSaveable { mutableStateOf(initialVendorName ?: "") }
    var issueDateText by rememberSaveable { mutableStateOf(initialIssueDateText ?: "") }
    var amountText by rememberSaveable { mutableStateOf(initialAmount) }
    // Convert date from YYYY-MM-DD to DD/MM/YYYY if needed
    var dueDateText by rememberSaveable {
        mutableStateOf(
            if (initialDueDateText.isNotBlank()) {
                val trimmed = initialDueDateText.trim()
                try {
                    // Try to parse as YYYY-MM-DD (old format) and convert
                    val date = LocalDate.parse(trimmed)
                    date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                } catch (e: Exception) {
                    // If parsing fails, check if it's already in DD/MM/YYYY format
                    try {
                        LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        trimmed // Already in correct format
                    } catch (e2: Exception) {
                        trimmed // Keep as-is if can't parse
                    }
                }
            } else {
                initialDueDateText
            }
        )
    }
    var paymentDateText by rememberSaveable { mutableStateOf(initialPaymentDateText ?: "") }
    var servicePeriodStartText by rememberSaveable { mutableStateOf(initialServicePeriodStartText ?: "") }
    var servicePeriodEndText by rememberSaveable { mutableStateOf(initialServicePeriodEndText ?: "") }
    var notes by rememberSaveable { mutableStateOf(initialNotes) }
    var paymentStatus by rememberSaveable { mutableStateOf(initialPaymentStatus) }
    var currency by rememberSaveable { mutableStateOf(Currency.ILS) }
    var customFieldValues by rememberSaveable {
        mutableStateOf(
            // Ensure we have values for all custom fields, padding with empty strings if needed
            categoryCustomFieldTitles.mapIndexed { index, _ ->
                initialCustomFieldValues.getOrNull(index) ?: ""
            }
        )
    }

    var amountError by rememberSaveable { mutableStateOf<String?>(null) }
    var dueDateError by rememberSaveable { mutableStateOf<String?>(null) }
    var amountTouched by rememberSaveable { mutableStateOf(false) }
    var dueDateTouched by rememberSaveable { mutableStateOf(false) }
    
    // Document type state
    var documentType by rememberSaveable { mutableStateOf<DocumentType?>(initialDocumentType) }
    var documentTypeExpanded by rememberSaveable { mutableStateOf(false) }
    
    // Date picker logic - reusable function
    fun showDatePicker(
        currentDateText: String,
        onDateSelected: (String) -> Unit,
        onTouched: () -> Unit,
        onErrorCleared: () -> Unit
    ) {
        val cal = java.util.Calendar.getInstance()
        // Try to parse existing date to set initial calendar date
        if (currentDateText.isNotBlank()) {
            val trimmed = currentDateText.trim()
            try {
                // Try DD/MM/YYYY format
                val date = LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                cal.set(date.year, date.monthValue - 1, date.dayOfMonth)
            } catch (e: Exception) {
                try {
                    // Try YYYY-MM-DD format (old format for backward compatibility)
                    val date = LocalDate.parse(trimmed)
                    cal.set(date.year, date.monthValue - 1, date.dayOfMonth)
                } catch (e2: Exception) {
                    // Use current date if parsing fails
                }
            }
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                onDateSelected(selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                onTouched()
                onErrorCleared()
            },
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH),
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun handleSave() {
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            amountError = context.getString(R.string.enter_valid_amount)
            return
        } else {
            amountError = null
        }

        // Validate DD/MM/YYYY format - due date is mandatory
        val trimmedDueDate = dueDateText.trim()
        if (trimmedDueDate.isBlank()) {
            dueDateError = context.getString(R.string.date_required)
            return
        }
        val isValid = try {
            LocalDate.parse(trimmedDueDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            true
        } catch (e: DateTimeParseException) {
            false
        }
        if (!isValid) {
            dueDateError = context.getString(R.string.use_format_dd_mm_yyyy)
            return
        } else {
            dueDateError = null
        }

        onSaveInvoice(
            vendorNameText.takeIf { it.isNotBlank() },
            issueDateText.takeIf { it.isNotBlank() },
            trimmedDueDate,
            amount,
            paymentStatus,
            paymentDateText.takeIf { it.isNotBlank() },
            servicePeriodStartText.takeIf { it.isNotBlank() },
            servicePeriodEndText.takeIf { it.isNotBlank() },
            notes.trim(),
            customFieldValues,
            documentType
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

            // Vendor/Body Name field
            OutlinedTextField(
                value = vendorNameText,
                onValueChange = { vendorNameText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.vendor_name)) }
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Issue Date field with calendar picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = issueDateText,
                    onValueChange = { issueDateText = it },
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.issue_date_dd_mm_yyyy)) }
                )
                IconButton(onClick = {
                    showDatePicker(
                        issueDateText,
                        { issueDateText = it },
                        { },
                        { }
                    )
                }) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = stringResource(R.string.pick_date)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Due Date field with calendar picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = dueDateText,
                    onValueChange = { 
                        dueDateText = it
                        dueDateTouched = true
                        // Clear error when user starts typing
                        if (dueDateError != null) {
                            dueDateError = null
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused && dueDateTouched) {
                                val trimmed = dueDateText.trim()
                                if (trimmed.isBlank()) {
                                    dueDateError = context.getString(R.string.date_required)
                                } else {
                                    val isValid = try {
                                        LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                        true
                                    } catch (e: DateTimeParseException) {
                                        false
                                    }
                                    if (!isValid) {
                                        dueDateError = context.getString(R.string.use_format_dd_mm_yyyy)
                                    } else {
                                        dueDateError = null
                                    }
                                }
                            }
                        },
                    label = { Text(stringResource(R.string.due_date_dd_mm_yyyy)) },
                    isError = dueDateError != null,
                    supportingText = dueDateError?.let { msg -> { Text(msg) } }
                )
                IconButton(onClick = {
                    showDatePicker(
                        dueDateText,
                        { dueDateText = it },
                        { dueDateTouched = true },
                        { dueDateError = null }
                    )
                }) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = stringResource(R.string.pick_date)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Amount field with currency selector
            OutlinedTextField(
                value = amountText,
                onValueChange = { 
                    amountText = it
                    amountTouched = true
                    // Clear error when user starts typing
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
                label = { Text(stringResource(R.string.amount)) },
                isError = amountError != null,
                supportingText = amountError?.let { msg -> { Text(msg) } },
                trailingIcon = {
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .width(56.dp)
                    ) {
                        // Draw left border only
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outline)
                                .align(Alignment.CenterStart)
                        )
                        // Button content
                        TextButton(
                            onClick = {
                                currency = if (currency == Currency.USD) Currency.ILS else Currency.USD
                            },
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 4.dp, bottomEnd = 4.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
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

            PaymentStatusSelector(
                selected = paymentStatus,
                onSelectedChange = { paymentStatus = it }
            )

            // Paid Date field (shown only if paid)
            if (paymentStatus == PaymentStatus.PAID_FULL || paymentStatus == PaymentStatus.PAID_CREDIT) {
                Spacer(modifier = Modifier.padding(top = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = paymentDateText,
                        onValueChange = { paymentDateText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.paid_date_dd_mm_yyyy)) }
                    )
                    IconButton(onClick = {
                        showDatePicker(
                            paymentDateText,
                            { paymentDateText = it },
                            { },
                            { }
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = stringResource(R.string.pick_date)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Document Type dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = documentType?.let { dt ->
                        when (dt) {
                            DocumentType.BILL_DEMAND -> stringResource(R.string.document_type_bill_demand)
                            DocumentType.TAX_INVOICE -> stringResource(R.string.document_type_tax_invoice)
                            DocumentType.INVOICE_RECEIPT -> stringResource(R.string.document_type_invoice_receipt)
                        }
                    } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { documentTypeExpanded = true },
                    label = { Text(stringResource(R.string.document_type)) },
                    trailingIcon = {
                        IconButton(onClick = { documentTypeExpanded = !documentTypeExpanded }) {
                            Icon(
                                imageVector = if (documentTypeExpanded) {
                                    Icons.Filled.KeyboardArrowUp
                                } else {
                                    Icons.Filled.KeyboardArrowDown
                                },
                                contentDescription = null
                            )
                        }
                    }
                )
                DropdownMenu(
                    expanded = documentTypeExpanded,
                    onDismissRequest = { documentTypeExpanded = false }
                ) {
                    // Option for "None" / null
                    DropdownMenuItem(
                        text = { Text("-") },
                        onClick = {
                            documentType = null
                            documentTypeExpanded = false
                        }
                    )
                    DocumentType.values().forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (type) {
                                        DocumentType.BILL_DEMAND -> stringResource(R.string.document_type_bill_demand)
                                        DocumentType.TAX_INVOICE -> stringResource(R.string.document_type_tax_invoice)
                                        DocumentType.INVOICE_RECEIPT -> stringResource(R.string.document_type_invoice_receipt)
                                    }
                                )
                            },
                            onClick = {
                                documentType = type
                                documentTypeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Service Period (optional)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = servicePeriodStartText,
                    onValueChange = { servicePeriodStartText = it },
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.service_period_start)) }
                )
                IconButton(onClick = {
                    showDatePicker(
                        servicePeriodStartText,
                        { servicePeriodStartText = it },
                        { },
                        { }
                    )
                }) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = stringResource(R.string.pick_date)
                    )
                }
            }
            Spacer(modifier = Modifier.padding(top = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = servicePeriodEndText,
                    onValueChange = { servicePeriodEndText = it },
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.service_period_end)) }
                )
                IconButton(onClick = {
                    showDatePicker(
                        servicePeriodEndText,
                        { servicePeriodEndText = it },
                        { },
                        { }
                    )
                }) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = stringResource(R.string.pick_date)
                    )
                }
            }

            // Custom fields
            if (categoryCustomFieldTitles.isNotEmpty()) {
                Spacer(modifier = Modifier.padding(top = 8.dp))
                
                categoryCustomFieldTitles.forEachIndexed { index, fieldTitle ->
                    Spacer(modifier = Modifier.padding(top = 8.dp))
                    OutlinedTextField(
                        value = customFieldValues.getOrNull(index) ?: "",
                        onValueChange = { newValue ->
                            val newList = customFieldValues.toMutableList()
                            // Ensure list is large enough
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
                    containerColor = Color(0xFF4CAF50) // Green color
                )
            ) {
                Text(stringResource(R.string.save_changes))
            }
        }
    }
}
