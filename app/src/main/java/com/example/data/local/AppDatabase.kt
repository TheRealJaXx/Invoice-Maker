package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Client
import com.example.data.model.FreelancerProfile
import com.example.data.model.Invoice
import com.example.data.model.LineItem
import com.example.data.model.LineItemConverter
import com.example.data.model.Quote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Client::class,
        Invoice::class,
        Quote::class,
        FreelancerProfile::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun quoteDao(): QuoteDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "freelance_books.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // Seed South African freelancer profile
            db.profileDao().insertOrUpdate(
                FreelancerProfile(
                    id = 1,
                    businessName = "Apex Creative Studio",
                    ownerName = "Jacques Du Plessis",
                    email = "billing@apexcreative.co.za",
                    phone = "+27 82 450 1928",
                    address = "42 Bree Street, City Centre",
                    cityStateZip = "Cape Town, 8001",
                    currencySymbol = "R",
                    invoicePrefix = "INV-",
                    nextInvoiceNumber = 1004,
                    quotePrefix = "EST-",
                    nextQuoteNumber = 502,
                    paymentInstructions = "EFT Payment Details:\nBank: FNB (First National Bank)\nAccount Number: 62890049281\nBranch Code: 250655\nAccount Type: Cheque / Current\nRef: [Invoice Number]"
                )
            )

            // Seed clients
            val client1Id = db.clientDao().insertClient(
                Client(
                    name = "Sarah Jenkins",
                    company = "BrightWave Media SA",
                    email = "sarah@brightwave.co.za",
                    phone = "+27 83 291 8203",
                    address = "120 Main Road, Green Point",
                    cityStateZip = "Cape Town, 8005",
                    notes = "Preferred Net 14 terms. Fast payer."
                )
            )

            val client2Id = db.clientDao().insertClient(
                Client(
                    name = "Marcus Vance",
                    company = "Horizon Cloud Technologies",
                    email = "mvance@horizoncloud.co.za",
                    phone = "+27 11 555 4321",
                    address = "15 Alice Lane, Sandton",
                    cityStateZip = "Johannesburg, 2196",
                    notes = "Requires PO number on all invoices."
                )
            )

            val client3Id = db.clientDao().insertClient(
                Client(
                    name = "Elena Rostova",
                    company = "Nova Creatives Studio",
                    email = "elena@novacreatives.co.za",
                    phone = "+27 21 422 8765",
                    address = "85 Kloof Street, Gardens",
                    cityStateZip = "Cape Town, 8001",
                    notes = "Agency creative partner."
                )
            )

            // Seed invoices in Rand (R) with daily rates
            val itemsInv1 = listOf(
                LineItem(description = "Mobile App Architecture & Core Engineering", quantity = 8.0, unitPrice = 4500.0),
                LineItem(description = "API Endpoint Integration & QA Testing", quantity = 4.0, unitPrice = 4500.0)
            )
            val subtotalInv1 = itemsInv1.sumOf { it.subtotal }
            val totalInv1 = itemsInv1.sumOf { it.total }

            db.invoiceDao().insertInvoice(
                Invoice(
                    invoiceNumber = "INV-1001",
                    invoiceSequence = 1001,
                    poNumber = "PO-2026-9041",
                    inviteNumber = "INVITE-8491",
                    clientId = client1Id,
                    clientName = "Sarah Jenkins",
                    clientCompany = "BrightWave Media SA",
                    clientEmail = "sarah@brightwave.co.za",
                    clientPhone = "+27 83 291 8203",
                    clientAddress = "120 Main Road, Green Point",
                    clientCityStateZip = "Cape Town, 8005",
                    issueDate = System.currentTimeMillis() - (12L * 24 * 60 * 60 * 1000),
                    dueDate = System.currentTimeMillis() + (2L * 24 * 60 * 60 * 1000),
                    itemsJson = LineItemConverter.toJson(itemsInv1),
                    subtotal = subtotalInv1,
                    total = totalInv1,
                    amountPaid = totalInv1,
                    status = "Paid",
                    paidDate = System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000),
                    notes = "Thank you for your business!"
                )
            )

            val itemsInv2 = listOf(
                LineItem(description = "UI Design System & Component Library", quantity = 6.0, unitPrice = 4000.0),
                LineItem(description = "Performance Optimization Audit", quantity = 2.0, unitPrice = 4800.0)
            )
            val subtotalInv2 = itemsInv2.sumOf { it.subtotal }
            val totalInv2 = itemsInv2.sumOf { it.total }

            db.invoiceDao().insertInvoice(
                Invoice(
                    invoiceNumber = "INV-1002",
                    invoiceSequence = 1002,
                    poNumber = "PO-HC-5501",
                    inviteNumber = "INVITE-8820",
                    clientId = client2Id,
                    clientName = "Marcus Vance",
                    clientCompany = "Horizon Cloud Technologies",
                    clientEmail = "mvance@horizoncloud.co.za",
                    clientPhone = "+27 11 555 4321",
                    clientAddress = "15 Alice Lane, Sandton",
                    clientCityStateZip = "Johannesburg, 2196",
                    issueDate = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000),
                    dueDate = System.currentTimeMillis() + (9L * 24 * 60 * 60 * 1000),
                    itemsJson = LineItemConverter.toJson(itemsInv2),
                    subtotal = subtotalInv2,
                    total = totalInv2,
                    amountPaid = 0.0,
                    status = "Sent",
                    notes = "Net 14 payment terms."
                )
            )

            // Seed quote in Rand (R) with daily rates
            val itemsQte1 = listOf(
                LineItem(description = "Creative Direction & Brand Strategy", quantity = 4.0, unitPrice = 5000.0),
                LineItem(description = "Custom Interface Iconography Pack", quantity = 2.0, unitPrice = 4500.0)
            )
            val subtotalQ1 = itemsQte1.sumOf { it.subtotal }
            val totalQ1 = itemsQte1.sumOf { it.total }

            db.quoteDao().insertQuote(
                Quote(
                    quoteNumber = "EST-501",
                    inviteNumber = "JOB-INVITE-7731",
                    clientId = client3Id,
                    clientName = "Elena Rostova",
                    clientCompany = "Nova Creatives Studio",
                    clientEmail = "elena@novacreatives.co.za",
                    clientPhone = "+27 21 422 8765",
                    clientAddress = "85 Kloof Street, Gardens",
                    clientCityStateZip = "Cape Town, 8001",
                    issueDate = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000),
                    expiryDate = System.currentTimeMillis() + (28L * 24 * 60 * 60 * 1000),
                    itemsJson = LineItemConverter.toJson(itemsQte1),
                    subtotal = subtotalQ1,
                    total = totalQ1,
                    status = "Pending",
                    notes = "Includes 2 rounds of creative revisions.",
                    terms = "50% deposit upon kickoff, balance net 14 upon delivery."
                )
            )
        }
    }
}
