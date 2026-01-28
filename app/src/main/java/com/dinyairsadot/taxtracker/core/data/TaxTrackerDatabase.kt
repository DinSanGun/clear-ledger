package com.dinyairsadot.taxtracker.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dinyairsadot.taxtracker.core.data.converters.LocalDateConverter
import com.dinyairsadot.taxtracker.core.data.converters.PaymentStatusConverter
import com.dinyairsadot.taxtracker.core.data.converters.StringListConverter
import com.dinyairsadot.taxtracker.core.data.dao.CategoryDao
import com.dinyairsadot.taxtracker.core.data.dao.InvoiceDao
import com.dinyairsadot.taxtracker.core.data.entities.CategoryEntity
import com.dinyairsadot.taxtracker.core.data.entities.InvoiceEntity

@Database(
    entities = [CategoryEntity::class, InvoiceEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    LocalDateConverter::class,
    PaymentStatusConverter::class,
    StringListConverter::class
)
abstract class TaxTrackerDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun invoiceDao(): InvoiceDao
    
    companion object {
        @Volatile
        private var INSTANCE: TaxTrackerDatabase? = null
        
        fun getDatabase(context: Context): TaxTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaxTrackerDatabase::class.java,
                    "tax_tracker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
