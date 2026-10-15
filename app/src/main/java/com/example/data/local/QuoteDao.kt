package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Quote
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes ORDER BY issueDate DESC, id DESC")
    fun getAllQuotes(): Flow<List<Quote>>

    @Query("SELECT * FROM quotes WHERE id = :id LIMIT 1")
    suspend fun getQuoteById(id: Long): Quote?

    @Query("SELECT * FROM quotes WHERE clientId = :clientId ORDER BY issueDate DESC")
    fun getQuotesForClient(clientId: Long): Flow<List<Quote>>

    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun getQuoteCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: Quote): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<Quote>): List<Long>

    @Query("SELECT * FROM quotes")
    suspend fun getAllQuotesList(): List<Quote>

    @Update
    suspend fun updateQuote(quote: Quote)

    @Delete
    suspend fun deleteQuote(quote: Quote)

    @Query("UPDATE quotes SET status = :status, convertedInvoiceId = :invoiceId, convertedInvoiceNumber = :invoiceNumber, poNumber = :poNumber, updatedAt = :timestamp WHERE id = :id")
    suspend fun markAsConverted(id: Long, status: String, invoiceId: Long, invoiceNumber: String, poNumber: String, timestamp: Long = System.currentTimeMillis())
}
