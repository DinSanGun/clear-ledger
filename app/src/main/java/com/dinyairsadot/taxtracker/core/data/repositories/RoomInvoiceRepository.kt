package com.dinyairsadot.taxtracker.core.data.repositories

import com.dinyairsadot.taxtracker.core.data.dao.InvoiceDao
import com.dinyairsadot.taxtracker.core.data.entities.InvoiceEntity
import com.dinyairsadot.taxtracker.core.domain.Invoice
import com.dinyairsadot.taxtracker.core.domain.InvoiceRepository

class RoomInvoiceRepository(
    private val invoiceDao: InvoiceDao
) : InvoiceRepository {
    
    override suspend fun getAllInvoices(): List<Invoice> {
        return invoiceDao.getAll().map { it.toDomain() }
    }

    override suspend fun getInvoicesForCategory(categoryId: Long): List<Invoice> {
        return invoiceDao.getByCategoryId(categoryId).map { it.toDomain() }
    }
    
    override suspend fun getInvoiceById(id: Long): Invoice? {
        return invoiceDao.getById(id)?.toDomain()
    }
    
    override suspend fun addInvoice(invoice: Invoice) {
        // Room will auto-generate ID if id=0 (due to @PrimaryKey(autoGenerate = true))
        val entity = InvoiceEntity.fromDomain(invoice)
        invoiceDao.insert(entity)
    }
    
    override suspend fun updateInvoice(invoice: Invoice) {
        val entity = InvoiceEntity.fromDomain(invoice)
        invoiceDao.update(entity)
    }
    
    override suspend fun deleteInvoice(id: Long) {
        invoiceDao.deleteById(id)
    }
}
