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
import com.dinyairsadot.taxtracker.core.data.dao.CategoryDao
import com.dinyairsadot.taxtracker.core.data.dao.InvoiceDao
import com.dinyairsadot.taxtracker.core.data.entities.CategoryEntity
import com.dinyairsadot.taxtracker.core.data.entities.InvoiceEntity

@Database(
    entities = [CategoryEntity::class, InvoiceEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(
    LocalDateConverter::class,
    PaymentStatusConverter::class,
    StringListConverter::class,
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
        
        fun getDatabase(context: Context): TaxTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaxTrackerDatabase::class.java,
                    "tax_tracker_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
