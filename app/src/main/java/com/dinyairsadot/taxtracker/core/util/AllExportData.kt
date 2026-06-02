package com.dinyairsadot.taxtracker.core.util

import com.dinyairsadot.taxtracker.core.domain.Category
import com.dinyairsadot.taxtracker.core.domain.Invoice

data class AllExportData(
    val categories: List<Category>,
    val invoicesByCategory: Map<Long, List<Invoice>>
)
