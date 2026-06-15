package com.dinyairsadot.clearledger.core.util

import com.dinyairsadot.clearledger.core.domain.Category
import com.dinyairsadot.clearledger.core.domain.Invoice

data class AllExportData(
    val categories: List<Category>,
    val invoicesByCategory: Map<Long, List<Invoice>>
)
