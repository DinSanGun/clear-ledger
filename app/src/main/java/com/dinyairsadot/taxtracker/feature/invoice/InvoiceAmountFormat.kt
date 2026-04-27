package com.dinyairsadot.taxtracker.feature.invoice

import android.content.Context
import com.dinyairsadot.taxtracker.R
import com.dinyairsadot.taxtracker.core.domain.InvoiceCurrency

fun formatAmountWithCurrency(context: Context, amount: Double, currency: InvoiceCurrency): String {
    val formatRes = when (currency) {
        InvoiceCurrency.ILS -> R.string.amount_format_ils
        InvoiceCurrency.USD -> R.string.amount_format_usd
    }
    return context.getString(formatRes, amount)
}
