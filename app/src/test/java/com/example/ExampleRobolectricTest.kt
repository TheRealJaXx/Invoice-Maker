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

    @Test
    fun separateDaysWithDifferentRatesTest() {
        val day1 = com.example.data.model.DayRate("14 Oct 2026", 3500.0, "Rehearsal")
        val day2 = com.example.data.model.DayRate("15 Oct 2026", 4500.0, "Live Match")
        val item = LineItem(
            project = "Springbok Tour",
            service = "TD/Vision Mixer",
            dateMode = "SEPARATE",
            separateDays = listOf(day1, day2)
        )

        assertEquals(8000.0, item.total, 0.001)
        assertEquals(2.0, item.quantity, 0.001)

        val json = LineItemConverter.toJson(listOf(item))
        val parsed = LineItemConverter.fromJson(json)
        assertEquals(1, parsed.size)
        assertEquals("Springbok Tour", parsed[0].project)
        assertEquals("TD/Vision Mixer", parsed[0].service)
        assertEquals("SEPARATE", parsed[0].dateMode)
        assertEquals(2, parsed[0].separateDays.size)
        assertEquals(3500.0, parsed[0].separateDays[0].rate, 0.001)
        assertEquals(4500.0, parsed[0].separateDays[1].rate, 0.001)
        assertEquals(8000.0, parsed[0].total, 0.001)
    }

    @Test
    fun clientExportAndImportTest() {
        val client = Client(
            id = 1L,
            name = "SuperSport Media",
            company = "MultiChoice Africa",
            email = "broadcast@multichoice.co.za",
            phone = "+27 11 289 3000",
            address = "Randburg Broadcasting Hub",
            cityStateZip = "Johannesburg, 2194",
            notes = "Net 30 terms"
        )

        // JSON Export & Import
        val json = com.example.util.DataBackupHelper.exportClientsToJson(listOf(client))
        val importedFromJson = com.example.util.DataBackupHelper.importClientsFromJson(json)
        assertEquals(1, importedFromJson.size)
        assertEquals("SuperSport Media", importedFromJson[0].name)
        assertEquals("MultiChoice Africa", importedFromJson[0].company)
        assertEquals("broadcast@multichoice.co.za", importedFromJson[0].email)

        // CSV Export & Import
        val csv = com.example.util.DataBackupHelper.exportClientsToCsv(listOf(client))
        val importedFromCsv = com.example.util.DataBackupHelper.importClientsFromCsv(csv)
        assertEquals(1, importedFromCsv.size)
        assertEquals("SuperSport Media", importedFromCsv[0].name)
        assertEquals("MultiChoice Africa", importedFromCsv[0].company)
        assertEquals("broadcast@multichoice.co.za", importedFromCsv[0].email)
    }
}
