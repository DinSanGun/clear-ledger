package com.dinyairsadot.taxtracker.feature.invoice

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
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
import com.dinyairsadot.taxtracker.core.domain.DocumentType
import com.dinyairsadot.taxtracker.core.domain.PaymentStatus
import com.dinyairsadot.taxtracker.core.ui.categoryTopAppBarColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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
    categoryPinnedSupplierName: String?,
    getDefaultDocumentType: (String?) -> DocumentType?,
    onNavigateBack: () -> Unit,
    onSaveInvoice: (
        documentNumber: String,
        amountDue: Double,
        paymentStatus: PaymentStatus,
        servicePeriodStartText: String,
        servicePeriodEndText: String,
        paymentMethod: String?,
        confirmationNumber: String?,
        notes: String,
        customFieldValues: List<String>
    ) -> Unit
) {
    val context = LocalContext.current
    
    // Display-only field (prefilled from category, not saved to invoice)
    var supplierName by rememberSaveable { mutableStateOf(categoryPinnedSupplierName ?: "") }
    
    // New core required fields
    var documentNumberText by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var paymentStatus by rememberSaveable { mutableStateOf(PaymentStatus.NOT_PAID) }
    var servicePeriodStartText by rememberSaveable { mutableStateOf("") }
    var servicePeriodEndText by rememberSaveable { mutableStateOf("") }
    
    // Optional core fields
    var paymentMethod by rememberSaveable { mutableStateOf("") }
    var confirmationNumber by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    
    var currency by rememberSaveable { mutableStateOf(Currency.ILS) }
    var customFieldValues by rememberSaveable {
        mutableStateOf(List(categoryCustomFieldTitles.size) { "" })
    }

    // Validation state
    var documentNumberError by rememberSaveable { mutableStateOf<String?>(null) }
    var amountError by rememberSaveable { mutableStateOf<String?>(null) }
    var servicePeriodStartError by rememberSaveable { mutableStateOf<String?>(null) }
    var servicePeriodEndError by rememberSaveable { mutableStateOf<String?>(null) }
    
    var documentNumberTouched by rememberSaveable { mutableStateOf(false) }
    var amountTouched by rememberSaveable { mutableStateOf(false) }
    var servicePeriodStartTouched by rememberSaveable { mutableStateOf(false) }
    var servicePeriodEndTouched by rememberSaveable { mutableStateOf(false) }
    
    // Date picker logic
    fun showDatePicker(
        currentDateText: String,
        onDateSelected: (String) -> Unit,
        onTouched: () -> Unit,
        onErrorCleared: () -> Unit
    ) {
        val cal = java.util.Calendar.getInstance()
        if (currentDateText.isNotBlank()) {
            val trimmed = currentDateText.trim()
            try {
                val date = LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                cal.set(date.year, date.monthValue - 1, date.dayOfMonth)
            } catch (e: Exception) {
                // Use current date if parsing fails
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
        var hasError = false
        
        // Validate document number
        if (documentNumberText.trim().isBlank()) {
            documentNumberError = context.getString(R.string.document_number_required)
            hasError = true
        } else {
            documentNumberError = null
        }
        
        // Validate amount
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            amountError = context.getString(R.string.enter_valid_amount)
            hasError = true
        } else {
            amountError = null
        }
        
        // Validate service period start (required)
        val trimmedStart = servicePeriodStartText.trim()
        if (trimmedStart.isBlank()) {
            servicePeriodStartError = context.getString(R.string.date_required)
            hasError = true
        } else {
            val isValid = try {
                LocalDate.parse(trimmedStart, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                true
            } catch (e: DateTimeParseException) {
                false
            }
            if (!isValid) {
                servicePeriodStartError = context.getString(R.string.use_format_dd_mm_yyyy)
                hasError = true
            } else {
                servicePeriodStartError = null
            }
        }
        
        // Validate service period end (required)
        val trimmedEnd = servicePeriodEndText.trim()
        if (trimmedEnd.isBlank()) {
            servicePeriodEndError = context.getString(R.string.date_required)
            hasError = true
        } else {
            val isValid = try {
                LocalDate.parse(trimmedEnd, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                true
            } catch (e: DateTimeParseException) {
                false
            }
            if (!isValid) {
                servicePeriodEndError = context.getString(R.string.use_format_dd_mm_yyyy)
                hasError = true
            } else {
                servicePeriodEndError = null
            }
        }
        
        if (hasError) return
        
        onSaveInvoice(
            documentNumberText.trim(),
            amount!!,
            paymentStatus,
            trimmedStart,
            trimmedEnd,
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

            // Supplier Name (display only, prefilled from category, not saved to invoice)
            OutlinedTextField(
                value = supplierName,
                onValueChange = { supplierName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.vendor_name)) }
            )

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

            // Payment Status (REQUIRED)
            PaymentStatusSelector(
                selected = paymentStatus,
                onSelectedChange = { paymentStatus = it }
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Service Period Start (REQUIRED) with calendar picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = servicePeriodStartText,
                    onValueChange = { 
                        servicePeriodStartText = it
                        servicePeriodStartTouched = true
                        if (servicePeriodStartError != null) {
                            servicePeriodStartError = null
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused && servicePeriodStartTouched) {
                                val trimmed = servicePeriodStartText.trim()
                                if (trimmed.isBlank()) {
                                    servicePeriodStartError = context.getString(R.string.date_required)
                                } else {
                                    val isValid = try {
                                        LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                        true
                                    } catch (e: DateTimeParseException) {
                                        false
                                    }
                                    if (!isValid) {
                                        servicePeriodStartError = context.getString(R.string.use_format_dd_mm_yyyy)
                                    } else {
                                        servicePeriodStartError = null
                                    }
                                }
                            }
                        },
                    label = { Text("${stringResource(R.string.service_period_start)} *") },
                    isError = servicePeriodStartError != null,
                    supportingText = { servicePeriodStartError?.let { Text(it) } }
                )
                IconButton(onClick = {
                    showDatePicker(
                        servicePeriodStartText,
                        { servicePeriodStartText = it },
                        { servicePeriodStartTouched = true },
                        { servicePeriodStartError = null }
                    )
                }) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = stringResource(R.string.pick_date)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Service Period End (REQUIRED) with calendar picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = servicePeriodEndText,
                    onValueChange = { 
                        servicePeriodEndText = it
                        servicePeriodEndTouched = true
                        if (servicePeriodEndError != null) {
                            servicePeriodEndError = null
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused && servicePeriodEndTouched) {
                                val trimmed = servicePeriodEndText.trim()
                                if (trimmed.isBlank()) {
                                    servicePeriodEndError = context.getString(R.string.date_required)
                                } else {
                                    val isValid = try {
                                        LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                        true
                                    } catch (e: DateTimeParseException) {
                                        false
                                    }
                                    if (!isValid) {
                                        servicePeriodEndError = context.getString(R.string.use_format_dd_mm_yyyy)
                                    } else {
                                        servicePeriodEndError = null
                                    }
                                }
                            }
                        },
                    label = { Text("${stringResource(R.string.service_period_end)} *") },
                    isError = servicePeriodEndError != null,
                    supportingText = { servicePeriodEndError?.let { Text(it) } }
                )
                IconButton(onClick = {
                    showDatePicker(
                        servicePeriodEndText,
                        { servicePeriodEndText = it },
                        { servicePeriodEndTouched = true },
                        { servicePeriodEndError = null }
                    )
                }) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = stringResource(R.string.pick_date)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

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
                Text(stringResource(R.string.save_invoice))
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
            text = stringResource(R.string.status),
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(end = 8.dp)
        )
        TextButton(
            onClick = { onSelectedChange(PaymentStatus.NOT_PAID) }
        ) {
            Text(
                text = stringResource(R.string.not_paid),
                fontWeight = if (selected == PaymentStatus.NOT_PAID) FontWeight.Bold else FontWeight.Normal
            )
        }
        TextButton(
            onClick = { onSelectedChange(PaymentStatus.PAID_FULL) }
        ) {
            Text(
                text = stringResource(R.string.paid_full),
                fontWeight = if (selected == PaymentStatus.PAID_FULL) FontWeight.Bold else FontWeight.Normal
            )
        }
        TextButton(
            onClick = { onSelectedChange(PaymentStatus.PAID_CREDIT) }
        ) {
            Text(
                text = stringResource(R.string.paid_credit),
                fontWeight = if (selected == PaymentStatus.PAID_CREDIT) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
