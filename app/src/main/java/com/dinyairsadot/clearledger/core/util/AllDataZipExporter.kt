package com.dinyairsadot.clearledger.core.util

import com.dinyairsadot.clearledger.core.domain.Category
import java.io.BufferedOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Pure Kotlin ZIP writer for all-data export. No Android dependencies.
 */
object AllDataZipExporter {

    fun writeZip(
        outputStream: OutputStream,
        data: AllExportData,
        invoiceLabels: InvoiceCsvExportLabels,
        categoryLabels: CategoriesCsvLabels
    ) {
        ZipOutputStream(BufferedOutputStream(outputStream)).use { zip ->
            zip.putNextEntry(ZipEntry("categories.csv"))
            val categoriesCsv = buildCategoriesCsv(data.categories, categoryLabels)
            Utf8CsvWriter.writeUtf8CsvWithBom(zip, categoriesCsv)
            zip.closeEntry()

            for (category in data.categories) {
                val invoices = data.invoicesByCategory[category.id].orEmpty()
                if (invoices.isEmpty()) continue
                val safeName = sanitizeCategoryName(category.name)
                zip.putNextEntry(ZipEntry("invoices/${safeName}_${category.id}.csv"))
                val invoiceCsv = InvoiceCsvExporter.generate(
                    invoices = invoices,
                    categoryName = category.name,
                    customFieldTitles = category.customFieldTitles,
                    labels = invoiceLabels
                )
                Utf8CsvWriter.writeUtf8CsvWithBom(zip, invoiceCsv)
                zip.closeEntry()
            }
        }
    }

    internal fun buildCategoriesCsv(
        categories: List<Category>,
        labels: CategoriesCsvLabels
    ): String {
        val maxCustomFields = categories.maxOfOrNull { it.customFieldTitles.size } ?: 0
        val headers = buildCategoriesHeaders(labels, maxCustomFields)
        val rows = categories.map { category ->
            buildCategoryRow(category, maxCustomFields)
        }
        return (listOf(headers) + rows).joinToString("\n") { row ->
            row.joinToString(",") { InvoiceCsvExporter.escapeCsvField(it) }
        }
    }

    private fun buildCategoriesHeaders(
        labels: CategoriesCsvLabels,
        maxCustomFields: Int
    ): List<String> {
        val base = listOf(
            labels.categoryNameHeader,
            labels.descriptionHeader,
            labels.orderHeader
        )
        val customHeaders = (1..maxCustomFields).map { index ->
            labels.customFieldTitleHeader(index)
        }
        return base + customHeaders
    }

    private fun buildCategoryRow(
        category: Category,
        maxCustomFields: Int
    ): List<String> {
        val base = listOf(
            category.name,
            category.description.orEmpty(),
            category.orderIndex.toString()
        )
        val customCells = (0 until maxCustomFields).map { index ->
            category.customFieldTitles.getOrElse(index) { "" }
        }
        return base + customCells
    }

    private val unsafeFilenameChars = setOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')

    internal fun sanitizeCategoryName(name: String): String {
        val sanitized = name
            .trim()
            .map { char ->
                when {
                    char.code < 32 -> '_'
                    char in unsafeFilenameChars -> '_'
                    else -> char
                }
            }
            .joinToString("")
            .trim()
            .take(60)
            .trim()
        return sanitized.ifEmpty { "category" }
    }
}
