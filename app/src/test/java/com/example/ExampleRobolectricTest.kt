package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.Client
import com.example.data.model.LineItem
import com.example.data.model.LineItemConverter
import com.example.data.model.Quote
import com.example.data.repository.FreelanceRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: FreelanceRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FreelanceRepository(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun readStringResourceAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Freelance Books", appName)
    }

    @Test
    fun lineItemCalculationAndSerializationTest() {
        val item1 = LineItem(description = "Brand Identity Design", quantity = 2.0, unitPrice = 750.0)
        assertEquals(1500.0, item1.subtotal, 0.001)
        assertEquals(1500.0, item1.total, 0.001)

        val json = LineItemConverter.toJson(listOf(item1))
        val deserialized = LineItemConverter.fromJson(json)
        assertEquals(1, deserialized.size)
        assertEquals("Brand Identity Design", deserialized[0].description)
        assertEquals(2.0, deserialized[0].quantity, 0.001)
        assertEquals(750.0, deserialized[0].unitPrice, 0.001)
    }

    @Test
    fun quoteConversionToInvoiceWithPoNumberTest() = runBlocking {
        val clientId = repository.insertClient(
            Client(
                name = "BrightWave Media SA",
                email = "billing@brightwave.co.za",
                company = "BrightWave Media",
                phone = "+27 82 555 1234"
            )
        )

        val items = listOf(
            LineItem(description = "Full Stack Mobile Development", quantity = 20.0, unitPrice = 650.0)
        )
        val quoteId = repository.insertQuote(
            Quote(
                quoteNumber = "EST-501",
                inviteNumber = "INVITE-9021",
                clientId = clientId,
                clientName = "BrightWave Media SA",
                clientEmail = "billing@brightwave.co.za",
                itemsJson = LineItemConverter.toJson(items),
                subtotal = 13000.0,
                total = 13000.0
            )
        )

        // Convert quote to invoice with PO Number
        val invoice = repository.convertQuoteToInvoice(quoteId, "PO-2026-X99")
        assertNotNull(invoice)
        assertEquals("PO-2026-X99", invoice!!.poNumber)
        assertEquals("INVITE-9021", invoice.inviteNumber)
        assertEquals("BrightWave Media SA", invoice.clientName)
        assertEquals(13000.0, invoice.total, 0.001)
        assertTrue(invoice.invoiceNumber.startsWith("INV-"))

        // Verify quote status changed to Converted
        val updatedQuote = repository.getQuoteById(quoteId)
        assertNotNull(updatedQuote)
        assertEquals("Converted", updatedQuote!!.status)
        assertEquals(invoice.id, updatedQuote.convertedInvoiceId)
        assertEquals(invoice.invoiceNumber, updatedQuote.convertedInvoiceNumber)
    }
}
