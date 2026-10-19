package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.data.model.Client
import com.example.data.model.FreelancerProfile
import com.example.data.model.Invoice
import com.example.data.model.Quote
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object PdfGenerator {

    fun generateInvoicePdf(
        context: Context,
        invoice: Invoice,
        client: Client?,
        profile: FreelancerProfile?
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        try {
            // Header / Profile Info
            paint.textSize = 20f
            paint.isFakeBoldText = true
            canvas.drawText(profile?.name ?: "Freelancer", 40f, 50f, paint)

            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText(profile?.email ?: "", 40f, 65f, paint)
            canvas.drawText(profile?.phone ?: "", 40f, 80f, paint)
            canvas.drawText(profile?.address ?: "", 40f, 95f, paint)

            // Invoice Title
            paint.textSize = 24f
            paint.isFakeBoldText = true
            canvas.drawText("INVOICE", 450f, 50f, paint)

            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("Invoice #: ${invoice.invoiceNumber}", 450f, 70f, paint)
            canvas.drawText("Date: ${invoice.date}", 450f, 85f, paint)
            canvas.drawText("Due Date: ${invoice.dueDate}", 450f, 100f, paint)

            // Bill To
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("Bill To:", 40f, 140f, paint)

            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText(client?.name ?: invoice.clientName, 40f, 160f, paint)
            canvas.drawText(client?.address ?: "", 40f, 175f, paint)
            canvas.drawText(client?.email ?: "", 40f, 190f, paint)

            // Table Header
            var startY = 240f
            paint.isFakeBoldText = true
            canvas.drawText("Description", 40f, startY, paint)
            canvas.drawText("Qty", 350f, startY, paint)
            canvas.drawText("Price", 420f, startY, paint)
            canvas.drawText("Total", 500f, startY, paint)

            paint.isFakeBoldText = false
            startY += 10f
            canvas.drawLine(40f, startY, 555f, startY, paint)

            // Line Items
            startY += 20f
            for (item in invoice.lineItems) {
                canvas.drawText(item.description, 40f, startY, paint)
                canvas.drawText(item.quantity.toString(), 350f, startY, paint)
                canvas.drawText(Formatters.formatCurrency(item.unitPrice), 420
