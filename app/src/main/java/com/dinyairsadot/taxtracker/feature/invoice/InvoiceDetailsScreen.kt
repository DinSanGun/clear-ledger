package com.dinyairsadot.taxtracker.feature.invoice

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dinyairsadot.taxtracker.core.domain.PaymentStatus
import com.dinyairsadot.taxtracker.core.ui.parseCategoryColorOrDefault
import androidx.compose.material3.TopAppBarDefaults
import com.dinyairsadot.taxtracker.core.ui.categoryTopAppBarColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.LocalContentColor



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailsScreen(
    invoice: InvoiceUi,
    categoryCustomFieldTitles: List<String>,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    categoryColorHex: String?
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                        Text("Edit invoice")
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = invoice.invoiceNumber.ifBlank { "Invoice #${invoice.id}" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.padding(top = 8.dp))

                    Text(
                        text = "Amount: ${invoice.amount}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.padding(top = 4.dp))

                    Text(
                        text = "Status: " + when (invoice.paymentStatus) {
                            PaymentStatus.PAID_FULL -> "Paid in full"
                            PaymentStatus.NOT_PAID -> "Not paid"
                            PaymentStatus.PAID_CREDIT -> "Paid with credit"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    invoice.dueDateText?.let { due ->
                        Spacer(modifier = Modifier.padding(top = 4.dp))
                        Text(
                            text = "Due date: $due",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    invoice.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                        Spacer(modifier = Modifier.padding(top = 8.dp))
                        Text(
                            text = "Notes:",
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
