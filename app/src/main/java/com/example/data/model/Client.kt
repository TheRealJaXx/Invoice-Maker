package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val email: String, // General / Default Email
    val quoteEmail: String = "", // Separate email for quotes
    val invoiceEmail: String = "", // Separate email for invoices
    val phone: String,
    val address: String,
    val lastInvoiceNumber: Int = 0 // Tracks last sent invoice number
)
