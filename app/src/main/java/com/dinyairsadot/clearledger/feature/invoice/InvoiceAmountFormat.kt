package com.dinyairsadot.clearledger.feature.invoice

import android.content.Context
import com.dinyairsadot.clearledger.R
import com.dinyairsadot.clearledger.core.domain.InvoiceCurrency

fun formatAmountWithCurrency(context: Context, amount: Double, currency: InvoiceCurrency): String {
    val formatRes = when (currency) {
        InvoiceCurrency.ILS -> R.string.amount_format_ils
        InvoiceCurrency.USD -> R.string.amount_format_usd
    }
    return context.getString(formatRes, amount)
}
