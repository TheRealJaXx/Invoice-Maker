package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "freelancer_profile")
data class FreelancerProfile(
    @PrimaryKey
    val id: Int = 1,
    val businessName: String = "Freelance Creative",
    val ownerName: String = "Freelancer",
    val email: String = "billing@freelancer.co.za",
    val phone: String = "+27 82 123 4567",
    val address: String = "12 Kloof Street, Gardens",
    val cityStateZip: String = "Cape Town, 8001",
    val currencySymbol: String = "R",
    val invoicePrefix: String = "INV-",
    val nextInvoiceNumber: Int = 1,
    val quotePrefix: String = "EST-",
    val nextQuoteNumber: Int = 1,
    val paymentInstructions: String = "EFT Payment: FNB (First National Bank)\nAccount: 62890012345\nBranch Code: 250655\nReference: [Invoice Number]",
    val defaultPaymentTermsDays: Int = 14
)
