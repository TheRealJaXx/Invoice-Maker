package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class Quote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quoteNumber: String,
    val inviteNumber: String = "",
    val poNumber: String = "",
    val clientId: Long = 0L,
    val clientName: String = "",
    val clientCompany: String = "",
    val clientEmail: String = "",
    val clientPhone: String = "",
    val clientAddress: String = "",
    val clientSuburb: String = "",
    val clientCity: String = "",
    val clientPostalCode: String = "",
    val clientCityStateZip: String = "",
    val clientVatNo: String = "",
    val clientRegNo: String = "",
    val issueDate: Long = System.currentTimeMillis(),
    val expiryDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // Default 30 days
    val itemsJson: String = "[]",
    val subtotal: Double = 0.0,
    val total: Double = 0.0,
    val notes: String = "Quote is valid for 30 days.",
    val terms: String = "50% upfront upon acceptance, remaining on delivery.",
    val status: String = "Pending", // Pending, Sent, Accepted, Converted, Declined
    val convertedInvoiceId: Long? = null,
    val convertedInvoiceNumber: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
