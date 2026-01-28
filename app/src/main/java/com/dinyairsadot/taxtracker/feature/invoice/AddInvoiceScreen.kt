package com.dinyairsadot.taxtracker.feature.invoice
// Contains AddInvoiceScreen and EditInvoiceScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import com.dinyairsadot.taxtracker.core.domain.PaymentStatus
import com.dinyairsadot.taxtracker.core.ui.categoryTopAppBarColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import android.app.DatePickerDialog



enum class Currency {
    USD,
    ILS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvoiceScreen(
    categoryId: Long,
    categoryColorHex: String?,
    categoryCustomFieldTitles: List<String>,
    onNavigateBack: () -> Unit,
    onSaveInvoice: (
        amount: Double,
        dateText: String,
        paymentStatus: PaymentStatus,
        notes: String,
        customFieldValues: List<String>
    ) -> Unit
) {
    val context = LocalContext.current
    var amountText by rememberSaveable { mutableStateOf("") }
    var dateText by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var paymentStatus by rememberSaveable { mutableStateOf(PaymentStatus.NOT_PAID) }
    var currency by rememberSaveable { mutableStateOf(Currency.ILS) }
    var customFieldValues by rememberSaveable {
        mutableStateOf(List(categoryCustomFieldTitles.size) { "" })
    }

    var amountError by rememberSaveable { mutableStateOf<String?>(null) }
    var dateError by rememberSaveable { mutableStateOf<String?>(null) }
    var amountTouched by rememberSaveable { mutableStateOf(false) }
    var dateTouched by rememberSaveable { mutableStateOf(false) }
    
    // Date picker logic - create dialog that initializes with current dateText if available
    fun showDatePicker() {
        val cal = java.util.Calendar.getInstance()
        // Try to parse existing date to set initial calendar date
        if (dateText.isNotBlank()) {
            val trimmed = dateText.trim()
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
                dateText = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                dateTouched = true
                dateError = null
            },
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH),
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun handleSave() {
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            amountError = "Enter a valid amount"
            return
        } else {
            amountError = null
        }

        // Validate DD/MM/YYYY format - date is mandatory
        val trimmed = dateText.trim()
        if (trimmed.isBlank()) {
            dateError = "Date is required"
            return
        }
        val isValid = try {
            LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            true
        } catch (e: DateTimeParseException) {
            false
        }
        if (!isValid) {
            dateError = "Use format DD/MM/YYYY"
            return
        } else {
            dateError = null
        }

        onSaveInvoice(amount, trimmed, paymentStatus, notes.trim(), customFieldValues)
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add invoice") },
                colors = categoryTopAppBarColors(categoryColorHex),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                                amountError = "Enter a valid amount"
                            } else {
                                amountError = null
                            }
                        }
                    },
                label = { Text("Amount") },
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
                                Text(if (currency == Currency.USD) "$" else "₪")
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Date field with calendar picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { 
                        dateText = it
                        dateTouched = true
                        // Clear error when user starts typing
                        if (dateError != null) {
                            dateError = null
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused && dateTouched) {
                                val trimmed = dateText.trim()
                                if (trimmed.isBlank()) {
                                    dateError = "Date is required"
                                } else {
                                    val isValid = try {
                                        LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                        true
                                    } catch (e: DateTimeParseException) {
                                        false
                                    }
                                    if (!isValid) {
                                        dateError = "Use format DD/MM/YYYY"
                                    } else {
                                        dateError = null
                                    }
                                }
                            }
                        },
                    label = { Text("Due date (DD/MM/YYYY)") },
                    isError = dateError != null,
                    supportingText = dateError?.let { msg -> { Text(msg) } }
                )
                IconButton(onClick = { showDatePicker() }) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = "Pick date"
                    )
                }
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            PaymentStatusSelector(
                selected = paymentStatus,
                onSelectedChange = { paymentStatus = it }
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes") },
                minLines = 3
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

            Spacer(modifier = Modifier.padding(top = 16.dp))

            Button(
                onClick = { handleSave() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50) // Green color
                )
            ) {
                Text("Save invoice")
            }
        }
    }
}

@Composable
private fun PaymentStatusSelector(
    selected: PaymentStatus,
    onSelectedChange: (PaymentStatus) -> Unit
) {
    // Row with label and buttons on the same line
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Status:",
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(end = 8.dp)
        )
        TextButton(
            onClick = { onSelectedChange(PaymentStatus.NOT_PAID) }
        ) {
            Text(
                text = "Not paid",
                fontWeight = if (selected == PaymentStatus.NOT_PAID) FontWeight.Bold else FontWeight.Normal
            )
        }
        TextButton(
            onClick = { onSelectedChange(PaymentStatus.PAID_FULL) }
        ) {
            Text(
                text = "Paid full",
                fontWeight = if (selected == PaymentStatus.PAID_FULL) FontWeight.Bold else FontWeight.Normal
            )
        }
        TextButton(
            onClick = { onSelectedChange(PaymentStatus.PAID_CREDIT) }
        ) {
            Text(
                text = "Paid credit",
                fontWeight = if (selected == PaymentStatus.PAID_CREDIT) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInvoiceScreen(
    invoiceId: Long,
    categoryColorHex: String?,
    categoryCustomFieldTitles: List<String>,
    initialAmount: String,
    initialDateText: String,
    initialPaymentStatus: PaymentStatus,
    initialNotes: String,
    initialCustomFieldValues: List<String>,
    onNavigateBack: () -> Unit,
    onSaveInvoice: (
        amount: Double,
        dateText: String,
        paymentStatus: PaymentStatus,
        notes: String,
        customFieldValues: List<String>
    ) -> Unit
) {
    val context = LocalContext.current
    var amountText by rememberSaveable { mutableStateOf(initialAmount) }
    // Convert date from YYYY-MM-DD to DD/MM/YYYY if needed
    var dateText by rememberSaveable {
        mutableStateOf(
            if (initialDateText.isNotBlank()) {
                val trimmed = initialDateText.trim()
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
                initialDateText
            }
        )
    }
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
    var dateError by rememberSaveable { mutableStateOf<String?>(null) }
    var amountTouched by rememberSaveable { mutableStateOf(false) }
    var dateTouched by rememberSaveable { mutableStateOf(false) }
    
    // Date picker logic - create dialog that initializes with current dateText if available
    fun showDatePicker() {
        val cal = java.util.Calendar.getInstance()
        // Try to parse existing date to set initial calendar date
        if (dateText.isNotBlank()) {
            val trimmed = dateText.trim()
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
                dateText = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                dateTouched = true
                dateError = null
            },
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH),
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun handleSave() {
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            amountError = "Enter a valid amount"
            return
        } else {
            amountError = null
        }

        // Validate DD/MM/YYYY format - date is mandatory
        val trimmed = dateText.trim()
        if (trimmed.isBlank()) {
            dateError = "Date is required"
            return
        }
        val isValid = try {
            LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            true
        } catch (e: DateTimeParseException) {
            false
        }
        if (!isValid) {
            dateError = "Use format DD/MM/YYYY"
            return
        } else {
            dateError = null
        }

        onSaveInvoice(amount, trimmed, paymentStatus, notes.trim(), customFieldValues)
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit invoice") },
                colors = categoryTopAppBarColors(categoryColorHex),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                                amountError = "Enter a valid amount"
                            } else {
                                amountError = null
                            }
                        }
                    },
                label = { Text("Amount") },
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
                                Text(if (currency == Currency.USD) "$" else "₪")
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Date field with calendar picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { 
                        dateText = it
                        dateTouched = true
                        // Clear error when user starts typing
                        if (dateError != null) {
                            dateError = null
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused && dateTouched) {
                                val trimmed = dateText.trim()
                                if (trimmed.isBlank()) {
                                    dateError = "Date is required"
                                } else {
                                    val isValid = try {
                                        LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                        true
                                    } catch (e: DateTimeParseException) {
                                        false
                                    }
                                    if (!isValid) {
                                        dateError = "Use format DD/MM/YYYY"
                                    } else {
                                        dateError = null
                                    }
                                }
                            }
                        },
                    label = { Text("Due date (DD/MM/YYYY)") },
                    isError = dateError != null,
                    supportingText = dateError?.let { msg -> { Text(msg) } }
                )
                IconButton(onClick = { showDatePicker() }) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = "Pick date"
                    )
                }
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            PaymentStatusSelector(
                selected = paymentStatus,
                onSelectedChange = { paymentStatus = it }
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes") },
                minLines = 3
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

            Spacer(modifier = Modifier.padding(top = 16.dp))

            Button(
                onClick = { handleSave() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50) // Green color
                )
            ) {
                Text("Save changes")
            }
        }
    }
}

