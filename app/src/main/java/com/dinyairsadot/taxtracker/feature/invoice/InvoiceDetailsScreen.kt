package com.dinyairsadot.taxtracker.feature.invoice

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.dinyairsadot.taxtracker.core.domain.DocumentType
import com.dinyairsadot.taxtracker.feature.invoice.formatServicePeriodForDisplay
import com.dinyairsadot.taxtracker.core.domain.PaymentStatus
import com.dinyairsadot.taxtracker.core.domain.PaymentMethodOption
import com.dinyairsadot.taxtracker.core.ui.categoryTopAppBarColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import com.dinyairsadot.taxtracker.R



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailsScreen(
    invoice: InvoiceUi,
    categoryCustomFieldTitles: List<String>,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    categoryColorHex: String?
) {
    val currentLocale = LocalConfiguration.current.locales[0]
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.invoice_details)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    // Ensures the action uses the same contrast-aware color as the title/icons
                    TextButton(
                        onClick = onEditClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = LocalContentColor.current
                        )
                    ) {
                        Text(stringResource(R.string.edit_invoice))
                    }
                },
                colors = categoryTopAppBarColors(categoryColorHex)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        val invoiceNumberText = invoice.invoiceNumber.ifBlank {
                            stringResource(R.string.invoice_number_fallback, invoice.id)
                        }
                        Text(
                            text = stringResource(R.string.invoice_number_label, invoiceNumberText),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        invoice.documentType?.let { docType ->
                            Spacer(modifier = Modifier.padding(top = 4.dp))
                            val docTypeText = when (docType) {
                                DocumentType.BILL_DEMAND -> stringResource(R.string.document_type_bill_demand)
                                DocumentType.TAX_INVOICE -> stringResource(R.string.document_type_tax_invoice)
                                DocumentType.INVOICE_RECEIPT -> stringResource(R.string.document_type_invoice_receipt)
                            }
                            Text(
                                text = stringResource(R.string.document_type_label, docTypeText),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        invoice.vendorName?.takeIf { it.isNotBlank() }?.let { vendor ->
                            Spacer(modifier = Modifier.padding(top = 4.dp))
                            Text(
                                text = stringResource(R.string.vendor_name_label, vendor),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.padding(top = 8.dp))

                        Text(
                            text = stringResource(
                                R.string.amount_label,
                                formatAmountWithCurrency(
                                    LocalContext.current,
                                    invoice.amount,
                                    invoice.amountCurrency
                                )
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.padding(top = 4.dp))

                        val statusText = when (invoice.paymentStatus) {
                            PaymentStatus.PAID -> stringResource(R.string.paid)
                            PaymentStatus.NOT_PAID -> stringResource(R.string.not_paid)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.status),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = invoice.paymentStatus.toDisplayColor()
                            )
                        }

                        // Payment details (shown only when present)
                        invoice.paymentMethod?.takeIf { it.isNotBlank() }?.let { methodValue ->
                            val methodLabel = when (methodValue) {
                                PaymentMethodOption.CREDIT.value -> stringResource(R.string.payment_method_credit)
                                PaymentMethodOption.BANK_TRANSFER.value -> stringResource(R.string.payment_method_bank_transfer)
                                PaymentMethodOption.CASH.value -> stringResource(R.string.payment_method_cash)
                                PaymentMethodOption.CHECK.value -> stringResource(R.string.payment_method_check)
                                PaymentMethodOption.DIGITAL_WALLET.value -> stringResource(R.string.payment_method_digital_wallet)
                                PaymentMethodOption.OTHER.value -> stringResource(R.string.payment_method_other)
                                else -> methodValue
                            }
                            Spacer(modifier = Modifier.padding(top = 4.dp))
                            Text(
                                text = stringResource(R.string.payment_method_label, methodLabel),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        invoice.numberOfPayments?.takeIf { it.isNotBlank() }?.let { count ->
                            Spacer(modifier = Modifier.padding(top = 4.dp))
                            Text(
                                text = stringResource(R.string.number_of_payments_label, count),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        invoice.confirmationNumber?.takeIf { it.isNotBlank() }?.let { confirmation ->
                            Spacer(modifier = Modifier.padding(top = 4.dp))
                            Text(
                                text = stringResource(R.string.confirmation_number_label, confirmation),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        invoice.issueDateText?.let { issue ->
                            Spacer(modifier = Modifier.padding(top = 4.dp))
                            Text(
                                text = stringResource(R.string.issue_date_label, issue),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        invoice.dueDateText?.let { due ->
                            Spacer(modifier = Modifier.padding(top = 4.dp))
                            Text(
                                text = stringResource(R.string.due_date_label, due),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        invoice.paymentDateText?.let { paid ->
                            Spacer(modifier = Modifier.padding(top = 4.dp))
                            Text(
                                text = stringResource(R.string.paid_date_label, paid),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        formatServicePeriodForDisplay(
                            invoice.servicePeriodStartText,
                            invoice.servicePeriodEndText,
                            invoice.servicePeriodMode,
                            currentLocale
                        )?.let { formattedPeriod ->
                            Spacer(modifier = Modifier.padding(top = 4.dp))
                            Text(
                                text = stringResource(R.string.service_period_label, formattedPeriod),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        invoice.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                            Spacer(modifier = Modifier.padding(top = 8.dp))
                            Text(
                                text = stringResource(R.string.notes_label),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.padding(top = 2.dp))
                            Text(
                                text = notes,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // Custom fields
                        if (categoryCustomFieldTitles.isNotEmpty() && invoice.customFieldValues.isNotEmpty()) {
                            categoryCustomFieldTitles.forEachIndexed { index, fieldTitle ->
                                invoice.customFieldValues.getOrNull(index)?.takeIf { it.isNotBlank() }?.let { value ->
                                    Spacer(modifier = Modifier.padding(top = 8.dp))
                                    Text(
                                        text = "$fieldTitle:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.padding(top = 2.dp))
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentStatus.toDisplayColor(): Color {
    return when (this) {
        PaymentStatus.PAID -> Color(0xFF4CAF50)
        PaymentStatus.NOT_PAID -> MaterialTheme.colorScheme.error
    }
}
