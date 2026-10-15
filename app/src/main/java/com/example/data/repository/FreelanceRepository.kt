package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.Client
import com.example.data.model.FreelancerProfile
import com.example.data.model.Invoice
import com.example.data.model.Quote
import kotlinx.coroutines.flow.Flow

class FreelanceRepository(private val db: AppDatabase) {

    // --- Clients ---
    val allClients: Flow<List<Client>> = db.clientDao().getAllClients()

    suspend fun getClientById(id: Long): Client? = db.clientDao().getClientById(id)

    suspend fun insertClient(client: Client): Long = db.clientDao().insertClient(client)

    suspend fun insertClients(clients: List<Client>): List<Long> = db.clientDao().insertClients(clients)

    suspend fun getAllClientsList(): List<Client> = db.clientDao().getAllClientsList()

    suspend fun updateClient(client: Client) = db.clientDao().updateClient(client)

    suspend fun deleteClient(client: Client) = db.clientDao().deleteClient(client)

    // --- Invoices ---
    val allInvoices: Flow<List<Invoice>> = db.invoiceDao().getAllInvoices()

    suspend fun getAllInvoicesList(): List<Invoice> = db.invoiceDao().getAllInvoicesList()

    suspend fun insertInvoices(invoices: List<Invoice>): List<Long> = db.invoiceDao().insertInvoices(invoices)

    suspend fun getInvoiceById(id: Long): Invoice? = db.invoiceDao().getInvoiceById(id)

    fun getInvoicesForClient(clientId: Long): Flow<List<Invoice>> =
        db.invoiceDao().getInvoicesForClient(clientId)

    suspend fun getNextInvoiceSequence(): Int {
        val maxSeq = db.invoiceDao().getMaxInvoiceSequence() ?: 1000
        val profile = db.profileDao().getProfileSync() ?: FreelancerProfile()
        val nextSeq = maxOf(profile.nextInvoiceNumber, maxSeq + 1)
        // Advance nextInvoiceNumber in profile
        db.profileDao().insertOrUpdate(profile.copy(nextInvoiceNumber = nextSeq + 1))
        return nextSeq
    }

    suspend fun peekNextInvoiceNumber(): String {
        val maxSeq = db.invoiceDao().getMaxInvoiceSequence() ?: 1000
        val profile = db.profileDao().getProfileSync() ?: FreelancerProfile()
        val nextSeq = maxOf(profile.nextInvoiceNumber, maxSeq + 1)
        val prefix = profile.invoicePrefix.ifBlank { "INV-" }
        return String.format("%s%04d", prefix, nextSeq)
    }

    suspend fun insertInvoice(invoice: Invoice): Long {
        val id = db.invoiceDao().insertInvoice(invoice)
        return id
    }

    suspend fun updateInvoice(invoice: Invoice) = db.invoiceDao().updateInvoice(invoice)

    suspend fun deleteInvoice(invoice: Invoice) = db.invoiceDao().deleteInvoice(invoice)

    suspend fun updateInvoiceStatus(id: Long, status: String, paidDate: Long? = null) {
        val timestamp = System.currentTimeMillis()
        val finalPaidDate = if (status == "Paid") (paidDate ?: timestamp) else null
        db.invoiceDao().updateStatus(id, status, finalPaidDate, timestamp)
    }

    // --- Quotes ---
    val allQuotes: Flow<List<Quote>> = db.quoteDao().getAllQuotes()

    suspend fun getAllQuotesList(): List<Quote> = db.quoteDao().getAllQuotesList()

    suspend fun insertQuotes(quotes: List<Quote>): List<Long> = db.quoteDao().insertQuotes(quotes)

    suspend fun getQuoteById(id: Long): Quote? = db.quoteDao().getQuoteById(id)

    fun getQuotesForClient(clientId: Long): Flow<List<Quote>> =
        db.quoteDao().getQuotesForClient(clientId)

    suspend fun peekNextQuoteNumber(): String {
        val count = db.quoteDao().getQuoteCount()
        val profile = db.profileDao().getProfileSync() ?: FreelancerProfile()
        val nextNum = maxOf(profile.nextQuoteNumber, 500 + count + 1)
        val prefix = profile.quotePrefix.ifBlank { "EST-" }
        return String.format("%s%03d", prefix, nextNum)
    }

    suspend fun insertQuote(quote: Quote): Long {
        val id = db.quoteDao().insertQuote(quote)
        val profile = db.profileDao().getProfileSync() ?: FreelancerProfile()
        db.profileDao().insertOrUpdate(profile.copy(nextQuoteNumber = profile.nextQuoteNumber + 1))
        return id
    }

    suspend fun updateQuote(quote: Quote) = db.quoteDao().updateQuote(quote)

    suspend fun deleteQuote(quote: Quote) = db.quoteDao().deleteQuote(quote)

    /**
     * Converts a Quote into an Invoice:
     * - Asks for PO Number (provided in argument)
     * - Auto-increments invoice number
     * - Carries over client info and line items
     * - Updates quote status to "Converted"
     * - Returns the newly inserted Invoice
     */
    suspend fun convertQuoteToInvoice(quoteId: Long, poNumber: String): Invoice? {
        val quote = db.quoteDao().getQuoteById(quoteId) ?: return null
        val nextSeq = getNextInvoiceSequence()
        val profile = db.profileDao().getProfileSync() ?: FreelancerProfile()
        val prefix = profile.invoicePrefix.ifBlank { "INV-" }
        val invNumber = String.format("%s%04d", prefix, nextSeq)

        val newInvoice = Invoice(
            invoiceNumber = invNumber,
            invoiceSequence = nextSeq,
            poNumber = poNumber.trim(),
            inviteNumber = quote.inviteNumber,
            quoteId = quote.id,
            clientId = quote.clientId,
            clientName = quote.clientName,
            clientCompany = quote.clientCompany,
            clientEmail = quote.clientEmail,
            clientPhone = quote.clientPhone,
            clientAddress = quote.clientAddress,
            clientCityStateZip = quote.clientCityStateZip,
            issueDate = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + (profile.defaultPaymentTermsDays.toLong() * 24 * 60 * 60 * 1000),
            itemsJson = quote.itemsJson,
            subtotal = quote.subtotal,
            total = quote.total,
            amountPaid = 0.0,
            notes = quote.notes.ifBlank { "Converted from Quote #${quote.quoteNumber}" },
            paymentInstructions = profile.paymentInstructions,
            status = "Draft"
        )

        val createdInvoiceId = db.invoiceDao().insertInvoice(newInvoice)

        // Update quote status
        db.quoteDao().markAsConverted(
            id = quote.id,
            status = "Converted",
            invoiceId = createdInvoiceId,
            invoiceNumber = invNumber,
            poNumber = poNumber.trim()
        )

        return db.invoiceDao().getInvoiceById(createdInvoiceId)
    }

    // --- Profile ---
    val profile: Flow<FreelancerProfile?> = db.profileDao().getProfile()

    suspend fun getProfileSync(): FreelancerProfile {
        return db.profileDao().getProfileSync() ?: FreelancerProfile().also {
            db.profileDao().insertOrUpdate(it)
        }
    }

    suspend fun updateProfile(profile: FreelancerProfile) =
        db.profileDao().insertOrUpdate(profile)
}
