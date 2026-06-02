package com.dinyairsadot.taxtracker.feature.invoice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.dinyairsadot.taxtracker.R
import com.dinyairsadot.taxtracker.core.domain.PaymentMethodOption
import com.dinyairsadot.taxtracker.core.util.InvoiceCsvExportLabels
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val CsvDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun rememberInvoiceCsvExportLabels(): InvoiceCsvExportLabels {
    val categoryNameHeader = stringResource(R.string.csv_export_category_name)
    val invoiceNumberHeader = stringResource(R.string.csv_export_invoice_number)
    val amountHeader = stringResource(R.string.csv_export_amount)
    val currencyHeader = stringResource(R.string.csv_export_currency)
    val statusHeader = stringResource(R.string.csv_export_status)
    val servicePeriodModeHeader = stringResource(R.string.csv_export_service_period_mode)
    val servicePeriodStartHeader = stringResource(R.string.csv_export_service_period_start)
    val servicePeriodEndHeader = stringResource(R.string.csv_export_service_period_end)
    val dueDateHeader = stringResource(R.string.csv_export_due_date)
    val paymentDateHeader = stringResource(R.string.csv_export_payment_date)
    val paymentMethodHeader = stringResource(R.string.csv_export_payment_method)
    val confirmationNumberHeader = stringResource(R.string.csv_export_confirmation_number)
    val notesHeader = stringResource(R.string.csv_export_notes)
    val customFieldFallbackTemplate = stringResource(R.string.csv_export_custom_field_fallback)
    val paymentStatusPaid = stringResource(R.string.paid)
    val paymentStatusNotPaid = stringResource(R.string.not_paid)
    val servicePeriodModeMonth = stringResource(R.string.service_period_mode_month)
    val servicePeriodModeDate = stringResource(R.string.service_period_mode_dates)
    val currencyIls = stringResource(R.string.csv_export_currency_ils)
    val currencyUsd = stringResource(R.string.csv_export_currency_usd)
    val paymentMethodNotSpecified = stringResource(R.string.payment_method_not_specified)
    val paymentMethodCredit = stringResource(R.string.payment_method_credit)
    val paymentMethodBankTransfer = stringResource(R.string.payment_method_bank_transfer)
    val paymentMethodCash = stringResource(R.string.payment_method_cash)
    val paymentMethodCheck = stringResource(R.string.payment_method_check)
    val paymentMethodDigitalWallet = stringResource(R.string.payment_method_digital_wallet)
    val paymentMethodOther = stringResource(R.string.payment_method_other)

    return remember(
        categoryNameHeader,
        invoiceNumberHeader,
        amountHeader,
        currencyHeader,
        statusHeader,
        servicePeriodModeHeader,
        servicePeriodStartHeader,
        servicePeriodEndHeader,
        dueDateHeader,
        paymentDateHeader,
        paymentMethodHeader,
        confirmationNumberHeader,
        notesHeader,
        customFieldFallbackTemplate,
        paymentStatusPaid,
        paymentStatusNotPaid,
        servicePeriodModeMonth,
        servicePeriodModeDate,
        currencyIls,
        currencyUsd,
        paymentMethodNotSpecified,
        paymentMethodCredit,
        paymentMethodBankTransfer,
        paymentMethodCash,
        paymentMethodCheck,
        paymentMethodDigitalWallet,
        paymentMethodOther
    ) {
        InvoiceCsvExportLabels(
            categoryNameHeader = categoryNameHeader,
            invoiceNumberHeader = invoiceNumberHeader,
            amountHeader = amountHeader,
            currencyHeader = currencyHeader,
            statusHeader = statusHeader,
            servicePeriodModeHeader = servicePeriodModeHeader,
            servicePeriodStartHeader = servicePeriodStartHeader,
            servicePeriodEndHeader = servicePeriodEndHeader,
            dueDateHeader = dueDateHeader,
            paymentDateHeader = paymentDateHeader,
            paymentMethodHeader = paymentMethodHeader,
            confirmationNumberHeader = confirmationNumberHeader,
            notesHeader = notesHeader,
            customFieldFallbackHeader = { index ->
                String.format(customFieldFallbackTemplate, index)
            },
            paymentStatusPaid = paymentStatusPaid,
            paymentStatusNotPaid = paymentStatusNotPaid,
            servicePeriodModeMonth = servicePeriodModeMonth,
            servicePeriodModeDate = servicePeriodModeDate,
            currencyIls = currencyIls,
            currencyUsd = currencyUsd,
            paymentMethodByStoredValue = mapOf(
                PaymentMethodOption.NOT_SPECIFIED.value to paymentMethodNotSpecified,
                "" to paymentMethodNotSpecified,
                PaymentMethodOption.CREDIT.value to paymentMethodCredit,
                PaymentMethodOption.BANK_TRANSFER.value to paymentMethodBankTransfer,
                PaymentMethodOption.CASH.value to paymentMethodCash,
                PaymentMethodOption.CHECK.value to paymentMethodCheck,
                PaymentMethodOption.DIGITAL_WALLET.value to paymentMethodDigitalWallet,
                PaymentMethodOption.OTHER.value to paymentMethodOther
            ),
            formatDate = { date -> date?.format(CsvDateFormatter).orEmpty() }
        )
    }
}
