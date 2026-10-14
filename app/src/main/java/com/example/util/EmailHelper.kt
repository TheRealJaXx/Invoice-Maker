package com.example.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.FreelancerProfile
import com.example.data.model.Invoice
import com.example.data.model.Quote
import java.io.File

object EmailHelper {

    fun sendInvoiceEmail(context: Context, invoice: Invoice, profile: FreelancerProfile): Boolean {
        return try {
            val pdfFile = PdfGenerator.generateInvoicePdf(context, invoice, profile)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val subject = "Invoice ${invoice.invoiceNumber} from ${profile.businessName}"
            val bodyBuilder = StringBuilder().apply {
                append("Dear ${invoice.clientName},\n\n")
                append("Please find attached invoice ${invoice.invoiceNumber} ")
                if (invoice.poNumber.isNotBlank()) {
                    append("(PO Ref: ${invoice.poNumber}) ")
                }
                append("for the total amount of ${Formatters.formatCurrency(invoice.total, profile.currencySymbol)}.\n\n")
                append("Due Date: ${Formatters.formatDate(invoice.dueDate)}\n\n")
                if (invoice.paymentInstructions.isNotBlank()) {
                    append("Payment Instructions:\n${invoice.paymentInstructions}\n\n")
                } else if (profile.paymentInstructions.isNotBlank()) {
                    append("Payment Instructions:\n${profile.paymentInstructions}\n\n")
                }
                append("Thank you for your business!\n\n")
                append("Best regards,\n")
                append("${profile.ownerName}\n")
                append(profile.businessName)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(invoice.clientEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, bodyBuilder.toString())
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Send Invoice to ${invoice.clientName}")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open email client: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun sendQuoteEmail(context: Context, quote: Quote, profile: FreelancerProfile): Boolean {
        return try {
            val pdfFile = PdfGenerator.generateQuotePdf(context, quote, profile)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val subject = "Quote ${quote.quoteNumber} from ${profile.businessName}"
            val bodyBuilder = StringBuilder().apply {
                append("Dear ${quote.clientName},\n\n")
                append("Thank you for the opportunity to submit our proposal")
                if (quote.inviteNumber.isNotBlank()) {
                    append(" regarding Job Invite #${quote.inviteNumber}")
                }
                append(".\n\n")
                append("Please find attached Quote ${quote.quoteNumber} for your review, ")
                append("totaling ${Formatters.formatCurrency(quote.total, profile.currencySymbol)}.\n")
                append("This estimate is valid until ${Formatters.formatDate(quote.expiryDate)}.\n\n")
                if (quote.terms.isNotBlank()) {
                    append("Terms: ${quote.terms}\n\n")
                }
                append("Please let me know if you have any questions or would like to proceed.\n\n")
                append("Best regards,\n")
                append("${profile.ownerName}\n")
                append(profile.businessName)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(quote.clientEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, bodyBuilder.toString())
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Send Quote to ${quote.clientName}")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open email client: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun viewPdf(context: Context, pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer app found", Toast.LENGTH_SHORT).show()
        }
    }
}
