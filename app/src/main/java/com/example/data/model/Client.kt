package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",                 // Name / Company (e.g. Settlers Media Pty Ltd)
    val company: String = "",              // Secondary Company / Trading Name
    val address: String = "",              // Street Address (e.g. 93 1st Avenue)
    val suburb: String = "",               // Suburb / Area (e.g. Fontainebleau)
    val city: String = "",                 // City (e.g. Randburg)
    val postalCode: String = "",           // Postal Code (e.g. 2194)
    val cityStateZip: String = "",         // Combined address
    val vatNo: String = "",                // VAT Number (e.g. 4460266721)
    val regNo: String = "",                // Registration Number (e.g. 2014/082117/07)
    val email: String = "",                // Primary / General Contact Email
    val quoteEmail: String = "",           // Email for Quotes (e.g. quote recipient)
    val invoiceEmail: String = "",         // Email for Invoices #1 (e.g. billing recipient)
    val invoiceEmail2: String = "",        // Email for Invoices #2 (when invoices must go to 2 different people)
    val phone: String = "",                // Contact Phone (e.g. (072) 052 2463)
    val notes: String = "",                // Internal notes
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayName: String
        get() = when {
            name.isNotBlank() -> name
            company.isNotBlank() -> company
            invoiceEmail.isNotBlank() -> invoiceEmail
            quoteEmail.isNotBlank() -> quoteEmail
            email.isNotBlank() -> email
            else -> "Client"
        }

    val effectiveQuoteEmail: String
        get() = quoteEmail.ifBlank { email }

    val effectiveInvoiceEmail: String
        get() = invoiceEmail.ifBlank { email }

    val fullAddress: String
        get() = buildString {
            val parts = listOf(address, suburb, city, postalCode).filter { it.isNotBlank() }
            if (parts.isNotEmpty()) {
                append(parts.joinToString(", "))
            } else if (cityStateZip.isNotBlank()) {
                append(cityStateZip)
            }
        }

    val initials: String
        get() {
            val textToUse = name.ifBlank { company }
            val parts = textToUse.trim().split("\\s+".toRegex())
            return when {
                parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercaseChar() ?: ""}${parts[1].firstOrNull()?.uppercaseChar() ?: ""}"
                textToUse.isNotBlank() -> textToUse.take(2).uppercase()
                email.isNotBlank() -> email.take(2).uppercase()
                invoiceEmail.isNotBlank() -> invoiceEmail.take(2).uppercase()
                else -> "CL"
            }
        }
}
