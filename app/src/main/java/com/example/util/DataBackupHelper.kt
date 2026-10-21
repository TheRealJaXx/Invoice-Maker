package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.Client
import com.example.data.model.FreelancerProfile
import com.example.data.model.Invoice
import com.example.data.model.Quote
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FullBackupData(
    val clients: List<Client>,
    val invoices: List<Invoice>,
    val quotes: List<Quote>,
    val profile: FreelancerProfile?
)

object DataBackupHelper {

    // --- CLIENTS EXPORT & IMPORT ---

    fun exportClientsToJson(clients: List<Client>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("type", "freelance_books_clients")
        root.put("exportedAt", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))

        val clientsArray = JSONArray()
        for (c in clients) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("company", c.company)
                put("address", c.address)
                put("suburb", c.suburb)
                put("city", c.city)
                put("postalCode", c.postalCode)
                put("cityStateZip", c.cityStateZip)
                put("vatNo", c.vatNo)
                put("regNo", c.regNo)
                put("email", c.email)
                put("phone", c.phone)
                put("notes", c.notes)
            }
            clientsArray.put(obj)
        }
        root.put("clients", clientsArray)
        return root.toString(2)
    }

    fun exportClientsToCsv(clients: List<Client>): String {
        val sb = StringBuilder()
        sb.append("Name,Company,Email,Phone,Address,CityStateZip,Notes\n")
        for (c in clients) {
            sb.append("\"${escapeCsv(c.name)}\",")
            sb.append("\"${escapeCsv(c.company)}\",")
            sb.append("\"${escapeCsv(c.email)}\",")
            sb.append("\"${escapeCsv(c.phone)}\",")
            sb.append("\"${escapeCsv(c.address)}\",")
            sb.append("\"${escapeCsv(c.cityStateZip)}\",")
            sb.append("\"${escapeCsv(c.notes)}\"\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }

    fun importClientsFromJson(jsonStr: String): List<Client> {
        val list = mutableListOf<Client>()
        try {
            val trimmed = jsonStr.trim()
            val clientsArray = if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                if (obj.has("clients")) obj.getJSONArray("clients") else JSONArray()
            } else if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                JSONArray()
            }

            for (i in 0 until clientsArray.length()) {
                val item = clientsArray.getJSONObject(i)
                val name = item.optString("name", "").trim()
                val email = item.optString("email", "").trim()
                if (name.isNotBlank() || email.isNotBlank()) {
                    list.add(
                        Client(
                            id = 0L, // New auto-generated id on import
                            name = if (name.isNotBlank()) name else "Unnamed Client",
                            company = item.optString("company", ""),
                            address = item.optString("address", ""),
                            suburb = item.optString("suburb", ""),
                            city = item.optString("city", ""),
                            postalCode = item.optString("postalCode", ""),
                            cityStateZip = item.optString("cityStateZip", ""),
                            vatNo = item.optString("vatNo", ""),
                            regNo = item.optString("regNo", ""),
                            email = email,
                            quoteEmail = item.optString("quoteEmail", ""),
                            invoiceEmail = item.optString("invoiceEmail", ""),
                            invoiceEmail2 = item.optString("invoiceEmail2", ""),
                            phone = item.optString("phone", ""),
                            notes = item.optString("notes", "")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun importClientsFromCsv(csvStr: String): List<Client> {
        val list = mutableListOf<Client>()
        try {
            val lines = csvStr.lines().filter { it.isNotBlank() }
            if (lines.size <= 1) return list

            // Skip header row
            for (lineIdx in 1 until lines.size) {
                val tokens = parseCsvLine(lines[lineIdx])
                if (tokens.isNotEmpty()) {
                    val name = tokens.getOrNull(0)?.trim() ?: ""
                    val company = tokens.getOrNull(1)?.trim() ?: ""
                    val email = tokens.getOrNull(2)?.trim() ?: ""
                    val phone = tokens.getOrNull(3)?.trim() ?: ""
                    val address = tokens.getOrNull(4)?.trim() ?: ""
                    val cityStateZip = tokens.getOrNull(5)?.trim() ?: ""
                    val notes = tokens.getOrNull(6)?.trim() ?: ""

                    if (name.isNotBlank() || email.isNotBlank()) {
                        list.add(
                            Client(
                                id = 0L,
                                name = if (name.isNotBlank()) name else "Unnamed Client",
                                company = company,
                                email = email,
                                phone = phone,
                                address = address,
                                cityStateZip = cityStateZip,
                                notes = notes
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val cur = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    cur.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(cur.toString())
                cur.clear()
            } else {
                cur.append(c)
            }
            i++
        }
        result.add(cur.toString())
        return result
    }

    // --- FULL DATA BACKUP & RESTORE ---

    fun exportFullBackupToJson(
        clients: List<Client>,
        invoices: List<Invoice>,
        quotes: List<Quote>,
        profile: FreelancerProfile
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("type", "freelance_books_full_backup")
        root.put("exportedAt", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))

        // Profile
        val profObj = JSONObject().apply {
            put("entityType", profile.entityType)
            put("businessName", profile.businessName)
            put("ownerName", profile.ownerName)
            put("email", profile.email)
            put("phone", profile.phone)
            put("address", profile.address)
            put("cityStateZip", profile.cityStateZip)
            put("currencySymbol", profile.currencySymbol)
            put("invoicePrefix", profile.invoicePrefix)
            put("nextInvoiceNumber", profile.nextInvoiceNumber)
            put("quotePrefix", profile.quotePrefix)
            put("nextQuoteNumber", profile.nextQuoteNumber)
            put("paymentInstructions", profile.paymentInstructions)
            put("defaultPaymentTermsDays", profile.defaultPaymentTermsDays)
        }
        root.put("profile", profObj)

        // Clients
        val clientsArr = JSONArray()
        for (c in clients) {
            clientsArr.put(
                JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("company", c.company)
                    put("address", c.address)
                    put("suburb", c.suburb)
                    put("city", c.city)
                    put("postalCode", c.postalCode)
                    put("cityStateZip", c.cityStateZip)
                    put("vatNo", c.vatNo)
                    put("regNo", c.regNo)
                    put("email", c.email)
                    put("quoteEmail", c.quoteEmail)
                    put("invoiceEmail", c.invoiceEmail)
                    put("invoiceEmail2", c.invoiceEmail2)
                    put("phone", c.phone)
                    put("notes", c.notes)
                }
            )
        }
        root.put("clients", clientsArr)

        // Invoices
        val invoicesArr = JSONArray()
        for (inv in invoices) {
            invoicesArr.put(
                JSONObject().apply {
                    put("id", inv.id)
                    put("invoiceNumber", inv.invoiceNumber)
                    put("invoiceSequence", inv.invoiceSequence)
                    put("poNumber", inv.poNumber)
                    put("inviteNumber", inv.inviteNumber)
                    put("quoteId", inv.quoteId)
                    put("clientId", inv.clientId)
                    put("clientName", inv.clientName)
                    put("clientCompany", inv.clientCompany)
                    put("clientEmail", inv.clientEmail)
                    put("clientEmail2", inv.clientEmail2)
                    put("clientPhone", inv.clientPhone)
                    put("clientAddress", inv.clientAddress)
                    put("clientSuburb", inv.clientSuburb)
                    put("clientCity", inv.clientCity)
                    put("clientPostalCode", inv.clientPostalCode)
                    put("clientCityStateZip", inv.clientCityStateZip)
                    put("clientVatNo", inv.clientVatNo)
                    put("clientRegNo", inv.clientRegNo)
                    put("issueDate", inv.issueDate)
                    put("hasDueDate", inv.hasDueDate)
                    put("dueDate", inv.dueDate)
                    put("itemsJson", inv.itemsJson)
                    put("subtotal", inv.subtotal)
                    put("total", inv.total)
                    put("amountPaid", inv.amountPaid)
                    put("notes", inv.notes)
                    put("paymentInstructions", inv.paymentInstructions)
                    put("status", inv.status)
                    put("paidDate", inv.paidDate)
                }
            )
        }
        root.put("invoices", invoicesArr)

        // Quotes
        val quotesArr = JSONArray()
        for (q in quotes) {
            quotesArr.put(
                JSONObject().apply {
                    put("id", q.id)
                    put("quoteNumber", q.quoteNumber)
                    put("inviteNumber", q.inviteNumber)
                    put("poNumber", q.poNumber)
                    put("clientId", q.clientId)
                    put("clientName", q.clientName)
                    put("clientCompany", q.clientCompany)
                    put("clientEmail", q.clientEmail)
                    put("clientEmail2", q.clientEmail2)
                    put("clientPhone", q.clientPhone)
                    put("clientAddress", q.clientAddress)
                    put("clientSuburb", q.clientSuburb)
                    put("clientCity", q.clientCity)
                    put("clientPostalCode", q.clientPostalCode)
                    put("clientCityStateZip", q.clientCityStateZip)
                    put("clientVatNo", q.clientVatNo)
                    put("clientRegNo", q.clientRegNo)
                    put("issueDate", q.issueDate)
                    put("expiryDate", q.expiryDate)
                    put("itemsJson", q.itemsJson)
                    put("subtotal", q.subtotal)
                    put("total", q.total)
                    put("notes", q.notes)
                    put("terms", q.terms)
                    put("status", q.status)
                    put("convertedInvoiceId", q.convertedInvoiceId)
                    put("convertedInvoiceNumber", q.convertedInvoiceNumber)
                }
            )
        }
        root.put("quotes", quotesArr)

        return root.toString(2)
    }

    fun parseFullBackupFromJson(jsonStr: String): FullBackupData {
        val root = JSONObject(jsonStr)

        val clients = importClientsFromJson(root.optJSONArray("clients")?.toString() ?: "[]")

        val invoices = mutableListOf<Invoice>()
        val invoicesArr = root.optJSONArray("invoices") ?: JSONArray()
        for (i in 0 until invoicesArr.length()) {
            val obj = invoicesArr.getJSONObject(i)
            invoices.add(
                Invoice(
                    id = 0L, // re-generate ID on restore
                    invoiceNumber = obj.optString("invoiceNumber", "INV-RESTORED"),
                    invoiceSequence = obj.optInt("invoiceSequence", 1000),
                    poNumber = obj.optString("poNumber", ""),
                    inviteNumber = obj.optString("inviteNumber", ""),
                    quoteId = if (obj.has("quoteId") && !obj.isNull("quoteId")) obj.getLong("quoteId") else null,
                    clientId = obj.optLong("clientId", 0L),
                    clientName = obj.optString("clientName", ""),
                    clientCompany = obj.optString("clientCompany", ""),
                    clientEmail = obj.optString("clientEmail", ""),
                    clientEmail2 = obj.optString("clientEmail2", ""),
                    clientPhone = obj.optString("clientPhone", ""),
                    clientAddress = obj.optString("clientAddress", ""),
                    clientSuburb = obj.optString("clientSuburb", ""),
                    clientCity = obj.optString("clientCity", ""),
                    clientPostalCode = obj.optString("clientPostalCode", ""),
                    clientCityStateZip = obj.optString("clientCityStateZip", ""),
                    clientVatNo = obj.optString("clientVatNo", ""),
                    clientRegNo = obj.optString("clientRegNo", ""),
                    issueDate = obj.optLong("issueDate", System.currentTimeMillis()),
                    hasDueDate = obj.optBoolean("hasDueDate", true),
                    dueDate = obj.optLong("dueDate", System.currentTimeMillis() + 14L * 86400000),
                    itemsJson = obj.optString("itemsJson", "[]"),
                    subtotal = obj.optDouble("subtotal", 0.0),
                    total = obj.optDouble("total", 0.0),
                    amountPaid = obj.optDouble("amountPaid", 0.0),
                    notes = obj.optString("notes", ""),
                    paymentInstructions = obj.optString("paymentInstructions", ""),
                    status = obj.optString("status", "Draft"),
                    paidDate = if (obj.has("paidDate") && !obj.isNull("paidDate")) obj.getLong("paidDate") else null
                )
            )
        }

        val quotes = mutableListOf<Quote>()
        val quotesArr = root.optJSONArray("quotes") ?: JSONArray()
        for (i in 0 until quotesArr.length()) {
            val obj = quotesArr.getJSONObject(i)
            quotes.add(
                Quote(
                    id = 0L,
                    quoteNumber = obj.optString("quoteNumber", "EST-RESTORED"),
                    inviteNumber = obj.optString("inviteNumber", ""),
                    poNumber = obj.optString("poNumber", ""),
                    clientId = obj.optLong("clientId", 0L),
                    clientName = obj.optString("clientName", ""),
                    clientCompany = obj.optString("clientCompany", ""),
                    clientEmail = obj.optString("clientEmail", ""),
                    clientEmail2 = obj.optString("clientEmail2", ""),
                    clientPhone = obj.optString("clientPhone", ""),
                    clientAddress = obj.optString("clientAddress", ""),
                    clientSuburb = obj.optString("clientSuburb", ""),
                    clientCity = obj.optString("clientCity", ""),
                    clientPostalCode = obj.optString("clientPostalCode", ""),
                    clientCityStateZip = obj.optString("clientCityStateZip", ""),
                    clientVatNo = obj.optString("clientVatNo", ""),
                    clientRegNo = obj.optString("clientRegNo", ""),
                    issueDate = obj.optLong("issueDate", System.currentTimeMillis()),
                    expiryDate = obj.optLong("expiryDate", System.currentTimeMillis() + 30L * 86400000),
                    itemsJson = obj.optString("itemsJson", "[]"),
                    subtotal = obj.optDouble("subtotal", 0.0),
                    total = obj.optDouble("total", 0.0),
                    notes = obj.optString("notes", ""),
                    terms = obj.optString("terms", ""),
                    status = obj.optString("status", "Pending"),
                    convertedInvoiceId = if (obj.has("convertedInvoiceId") && !obj.isNull("convertedInvoiceId")) obj.getLong("convertedInvoiceId") else null,
                    convertedInvoiceNumber = if (obj.has("convertedInvoiceNumber") && !obj.isNull("convertedInvoiceNumber")) obj.getString("convertedInvoiceNumber") else null
                )
            )
        }

        var profile: FreelancerProfile? = null
        if (root.has("profile")) {
            val pObj = root.getJSONObject("profile")
            profile = FreelancerProfile(
                id = 1,
                entityType = pObj.optString("entityType", "INDIVIDUAL"),
                businessName = pObj.optString("businessName", "Freelance Creative"),
                ownerName = pObj.optString("ownerName", "Freelancer"),
                email = pObj.optString("email", "billing@freelancer.co.za"),
                phone = pObj.optString("phone", "+27 82 123 4567"),
                address = pObj.optString("address", "12 Kloof Street"),
                cityStateZip = pObj.optString("cityStateZip", "Cape Town, 8001"),
                currencySymbol = pObj.optString("currencySymbol", "R"),
                invoicePrefix = pObj.optString("invoicePrefix", "INV-"),
                nextInvoiceNumber = pObj.optInt("nextInvoiceNumber", 1000),
                quotePrefix = pObj.optString("quotePrefix", "EST-"),
                nextQuoteNumber = pObj.optInt("nextQuoteNumber", 500),
                paymentInstructions = pObj.optString("paymentInstructions", ""),
                defaultPaymentTermsDays = pObj.optInt("defaultPaymentTermsDays", 14)
            )
        }

        return FullBackupData(clients, invoices, quotes, profile)
    }

    // --- FILE UTILITIES & DIRECT GOOGLE DRIVE / SYSTEM SHARE ---

    fun createTemporaryFile(context: Context, content: String, fileName: String): File {
        val outputDir = File(context.cacheDir, "shared_docs").apply { mkdirs() }
        val file = File(outputDir, fileName)
        FileOutputStream(file).use { out ->
            out.write(content.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    fun shareBackupFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Save to Google Drive / Share")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun writeToOutputStream(outputStream: OutputStream, content: String) {
        outputStream.use { out ->
            out.write(content.toByteArray(Charsets.UTF_8))
            out.flush()
        }
    }

    fun readFromInputStream(inputStream: InputStream): String {
        return inputStream.use { input ->
            input.bufferedReader(Charsets.UTF_8).use { it.readText() }
        }
    }
}
