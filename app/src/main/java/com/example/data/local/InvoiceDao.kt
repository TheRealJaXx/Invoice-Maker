package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Invoice
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY issueDate DESC, id DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: Long): Invoice?

    @Query("SELECT * FROM invoices WHERE clientId = :clientId ORDER BY issueDate DESC")
    fun getInvoicesForClient(clientId: Long): Flow<List<Invoice>>

    @Query("SELECT MAX(invoiceSequence) FROM invoices")
    suspend fun getMaxInvoiceSequence(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(invoices: List<Invoice>): List<Long>

    @Query("SELECT * FROM invoices")
    suspend fun getAllInvoicesList(): List<Invoice>

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    @Delete
    suspend fun deleteInvoice(invoice: Invoice)

    @Query("UPDATE invoices SET status = :status, paidDate = :paidDate, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, paidDate: Long?, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM invoices")
    fun getInvoiceCount(): Flow<Int>
}
