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
import com.dinyairsadot.taxtracker.core.data.converters.StringListConverter
import com.dinyairsadot.taxtracker.core.data.converters.StringMapConverter
import com.dinyairsadot.taxtracker.core.data.dao.CategoryDao
import com.dinyairsadot.taxtracker.core.data.dao.InvoiceDao
import com.dinyairsadot.taxtracker.core.data.entities.CategoryEntity
import com.dinyairsadot.taxtracker.core.data.entities.InvoiceEntity

@Database(
    entities = [CategoryEntity::class, InvoiceEntity::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(
    LocalDateConverter::class,
    PaymentStatusConverter::class,
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
        
        fun getDatabase(context: Context): TaxTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaxTrackerDatabase::class.java,
                    "tax_tracker_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
