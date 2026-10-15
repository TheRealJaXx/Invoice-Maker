package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val invoiceSequence: Int,
    val poNumber: String = "",
    val inviteNumber: String = "",
    val quoteId: Long? = null,
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
    val dueDate: Long = System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000), // Default 14 days
    val itemsJson: String = "[]",
    val subtotal: Double = 0.0,
    val total: Double = 0.0,
    val amountPaid: Double = 0.0,
    val notes: String = "Thank you for your business!",
    val paymentInstructions: String = "",
    val status: String = "Draft", // Draft, Sent, Paid, Overdue, Cancelled
    val paidDate: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
