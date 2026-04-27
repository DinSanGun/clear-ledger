package com.dinyairsadot.taxtracker.feature.invoice

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

private const val MAX_DATE_DIGITS = 8 // DDMMYYYY
private const val DATE_TEMPLATE = "DD/MM/YYYY"

fun formatDateInput(previous: TextFieldValue, incoming: TextFieldValue): TextFieldValue {
    val digitsOnly = incoming.text.filter(Char::isDigit).take(MAX_DATE_DIGITS)
    val formatted = buildDateTextFromDigits(digitsOnly)

    val digitsBeforeCursor = incoming.text
        .take(incoming.selection.end)
        .count(Char::isDigit)
        .coerceAtMost(digitsOnly.length)

    val mappedCursor = mapDigitIndexToFormattedCursor(digitsBeforeCursor, formatted.length)

    if (formatted == previous.text && mappedCursor == previous.selection.end) {
        return previous
    }

    return TextFieldValue(
        text = formatted,
        selection = TextRange(mappedCursor)
    )
}

private fun buildDateTextFromDigits(digits: String): String {
    if (digits.isEmpty()) return ""

    val day = digits.take(2)
    val month = digits.drop(2).take(2)
    val year = digits.drop(4).take(4)

    return buildString {
        append(day)
        if (month.isNotEmpty()) {
            append("/")
            append(month)
        }
        if (year.isNotEmpty()) {
            append("/")
            append(year)
        }
    }
}

private fun mapDigitIndexToFormattedCursor(digitIndex: Int, formattedLength: Int): Int {
    val cursor = when {
        digitIndex <= 2 -> digitIndex
        digitIndex <= 4 -> digitIndex + 1 // one slash after DD
        else -> digitIndex + 2 // two slashes after DD/MM
    }
    return cursor.coerceAtMost(formattedLength)
}

class DateTemplateVisualTransformation(
    private val templateColor: Color
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val suffix = DATE_TEMPLATE.drop(raw.length.coerceAtMost(DATE_TEMPLATE.length))

        val transformed = AnnotatedString.Builder().apply {
            append(raw)
            if (suffix.isNotEmpty()) {
                pushStyle(SpanStyle(color = templateColor))
                append(suffix)
                pop()
            }
        }.toAnnotatedString()

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = offset

            override fun transformedToOriginal(offset: Int): Int {
                return offset.coerceAtMost(raw.length)
            }
        }

        return TransformedText(transformed, mapping)
    }
}
