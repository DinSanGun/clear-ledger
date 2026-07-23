package com.dinyairsadot.clearledger.feature.category

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class EditableCategorySnapshotTest {

    private fun emptyAddFormSnapshot() = editableCategorySnapshot(
        name = "",
        colorHex = "",
        description = "",
        customFieldTitles = emptyList()
    )

    @Test
    fun `untouched add category form is not dirty`() {
        val original = emptyAddFormSnapshot()
        val current = editableCategorySnapshot(
            name = "",
            colorHex = "",
            description = "",
            customFieldTitles = emptyList(),
            pendingNewFieldName = ""
        )
        assertEquals(original, current)
    }

    @Test
    fun `whitespace only category fields are not dirty`() {
        val original = emptyAddFormSnapshot()
        val current = editableCategorySnapshot(
            name = "   ",
            colorHex = "  ",
            description = "\t",
            customFieldTitles = listOf("  "),
            pendingNewFieldName = "   "
        )
        assertEquals(original, current)
    }

    @Test
    fun `meaningful category data makes form dirty`() {
        val original = emptyAddFormSnapshot()
        val current = editableCategorySnapshot(
            name = "Arnona",
            colorHex = "",
            description = "",
            customFieldTitles = emptyList()
        )
        assertNotEquals(original, current)
    }

    @Test
    fun `restoring category data removes dirty state`() {
        val original = editableCategorySnapshot(
            name = "Water",
            colorHex = "#42A5F5",
            description = "Monthly",
            customFieldTitles = listOf("Meter")
        )
        val edited = editableCategorySnapshot(
            name = "Water bill",
            colorHex = "#42A5F5",
            description = "Monthly",
            customFieldTitles = listOf("Meter")
        )
        assertNotEquals(original, edited)

        val restored = editableCategorySnapshot(
            name = "Water",
            colorHex = "#42A5F5",
            description = "Monthly",
            customFieldTitles = listOf("Meter")
        )
        assertEquals(original, restored)
    }

    @Test
    fun `untouched edit category form is not dirty`() {
        val original = editableCategorySnapshot(
            name = "Gas",
            colorHex = "#AB47BC",
            description = "Notes",
            customFieldTitles = listOf("Account")
        )
        val current = editableCategorySnapshot(
            name = "Gas",
            colorHex = "#AB47BC",
            description = "Notes",
            customFieldTitles = listOf("Account")
        )
        assertEquals(original, current)
    }

    @Test
    fun `pending nonblank custom field makes form dirty`() {
        val original = emptyAddFormSnapshot()
        val current = editableCategorySnapshot(
            name = "",
            colorHex = "",
            description = "",
            customFieldTitles = emptyList(),
            pendingNewFieldName = "Supplier"
        )
        assertNotEquals(original, current)
        assertEquals(listOf("Supplier"), current.customFieldTitles)
    }

    @Test
    fun `empty pending custom field input is not dirty`() {
        val original = emptyAddFormSnapshot()
        val current = editableCategorySnapshot(
            name = "",
            colorHex = "",
            description = "",
            customFieldTitles = emptyList(),
            pendingNewFieldName = ""
        )
        assertEquals(original, current)
    }
}
