package com.dinyairsadot.taxtracker.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dinyairsadot.taxtracker.core.data.converters.DocumentTypeConverter
import com.dinyairsadot.taxtracker.core.data.converters.LocalDateConverter
import com.dinyairsadot.taxtracker.core.data.converters.PaymentStatusConverter
import com.dinyairsadot.taxtracker.core.data.converters.ServicePeriodModeConverter
import com.dinyairsadot.taxtracker.core.data.converters.StringListConverter
import com.dinyairsadot.taxtracker.core.data.converters.StringMapConverter
import com.dinyairsadot.taxtracker.core.data.dao.CategoryDao
import com.dinyairsadot.taxtracker.core.data.dao.InvoiceDao
import com.dinyairsadot.taxtracker.core.data.entities.CategoryEntity
import com.dinyairsadot.taxtracker.core.data.entities.InvoiceEntity

@Database(
    entities = [CategoryEntity::class, InvoiceEntity::class],
    version = 9,
    exportSchema = false
)
@TypeConverters(
    LocalDateConverter::class,
    PaymentStatusConverter::class,
    ServicePeriodModeConverter::class,
    StringListConverter::class,
    StringMapConverter::class,
    DocumentTypeConverter::class
)
abstract class TaxTrackerDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun invoiceDao(): InvoiceDao
    
    companion object {
        @Volatile
        private var INSTANCE: TaxTrackerDatabase? = null
        
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE invoices ADD COLUMN documentTypeString TEXT")
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE invoices ADD COLUMN vendorName TEXT")
                database.execSQL("ALTER TABLE invoices ADD COLUMN issueDateEpochDay INTEGER")
                database.execSQL("ALTER TABLE invoices ADD COLUMN servicePeriodStartEpochDay INTEGER")
                database.execSQL("ALTER TABLE invoices ADD COLUMN servicePeriodEndEpochDay INTEGER")
            }
        }
        
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new minimal core fields to invoices
                // amountDue defaults to existing amount, documentNumber defaults to invoiceNumber
                database.execSQL("ALTER TABLE invoices ADD COLUMN amountDue REAL")
                database.execSQL("ALTER TABLE invoices ADD COLUMN documentNumber TEXT")
                database.execSQL("ALTER TABLE invoices ADD COLUMN paymentMethodString TEXT")
                database.execSQL("ALTER TABLE invoices ADD COLUMN confirmationNumber TEXT")
                
                // Populate new fields from old ones for existing rows
                database.execSQL("UPDATE invoices SET amountDue = amount WHERE amountDue IS NULL")
                database.execSQL("UPDATE invoices SET documentNumber = invoiceNumber WHERE documentNumber IS NULL")
                
                // Add supplierName to categories
                database.execSQL("ALTER TABLE categories ADD COLUMN supplierName TEXT")
            }
        }
        
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add pinnedDefaultsJson to categories for flexible pinned defaults (e.g., supplierName)
                database.execSQL("ALTER TABLE categories ADD COLUMN pinnedDefaultsJson TEXT")
            }
        }
        
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add pinnedSnapshotJson to invoices to capture category pinned defaults at creation time
                database.execSQL("ALTER TABLE invoices ADD COLUMN pinnedSnapshotJson TEXT")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Locale-aware seeded categories: only new seeds get seedKey; no backfill
                database.execSQL("ALTER TABLE categories ADD COLUMN seedKey TEXT")
                database.execSQL("ALTER TABLE categories ADD COLUMN userEdited INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Step 1: give every category a default service-period mode of MONTH.
                // New categories also default to MONTH via the Kotlin field default.
                database.execSQL(
                    "ALTER TABLE categories ADD COLUMN defaultServicePeriodMode TEXT NOT NULL DEFAULT 'MONTH'"
                )

                // Step 2: add the mode column to invoices.
                // Temporary default 'MONTH' is overwritten in step 3 where the parent category exists.
                // Any orphaned invoice (no matching category) keeps 'MONTH' as a safe fallback.
                database.execSQL(
                    "ALTER TABLE invoices ADD COLUMN servicePeriodMode TEXT NOT NULL DEFAULT 'MONTH'"
                )

                // Step 3: propagate each category's defaultServicePeriodMode to its invoices.
                // After step 1 every category has 'MONTH', so existing invoices inherit MONTH.
                // This UPDATE is safe on SQLite because it is a correlated sub-select, not a JOIN.
                database.execSQL(
                    """
                    UPDATE invoices
                    SET servicePeriodMode = (
                        SELECT defaultServicePeriodMode
                        FROM categories
                        WHERE categories.id = invoices.categoryId
                    )
                    WHERE EXISTS (
                        SELECT 1 FROM categories WHERE categories.id = invoices.categoryId
                    )
                    """.trimIndent()
                )
                // Net result for existing users:
                // - All categories → defaultServicePeriodMode = MONTH
                // - All invoices that have a valid category → servicePeriodMode = MONTH
                // - Orphaned invoices (category deleted) → servicePeriodMode = MONTH (column default)
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Remove defaultServicePeriodMode from categories (mode is now per-invoice).
                // SQLite < 3.35 doesn't support DROP COLUMN; use table recreate.
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS categories_new (
                        id INTEGER PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        colorHex TEXT NOT NULL,
                        description TEXT,
                        customFieldTitlesJson TEXT,
                        supplierName TEXT,
                        pinnedDefaultsJson TEXT,
                        seedKey TEXT,
                        userEdited INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT INTO categories_new (id, name, colorHex, description, customFieldTitlesJson, supplierName, pinnedDefaultsJson, seedKey, userEdited)
                    SELECT id, name, colorHex, description, customFieldTitlesJson, supplierName, pinnedDefaultsJson, seedKey, userEdited
                    FROM categories
                """.trimIndent())
                database.execSQL("DROP TABLE categories")
                database.execSQL("ALTER TABLE categories_new RENAME TO categories")
            }
        }

        fun getDatabase(context: Context): TaxTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaxTrackerDatabase::class.java,
                    "tax_tracker_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
