package com.dinyairsadot.clearledger.feature.invoice

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun InvoiceFormSectionHeader(
    title: String,
    isFirstSection: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(
            top = if (isFirstSection) 0.dp else 16.dp,
            bottom = 4.dp
        )
    )
}
