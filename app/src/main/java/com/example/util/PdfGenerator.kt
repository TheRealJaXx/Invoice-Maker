package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.data.model.FreelancerProfile
import com.example.data.model.Invoice
import com.example.data.model.LineItemConverter
import com.example.data.model.Quote
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN = 40f

    fun generateInvoicePdf(context: Context, invoice: Invoice, profile: FreelancerProfile): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawInvoiceContent(canvas, invoice, profile)

        document.finishPage(page)

        val outputDir = File(context.cacheDir, "shared_docs").apply { mkdirs() }
        val sanitizedInvNum = invoice.invoiceNumber.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val file = File(outputDir, "Invoice_$sanitizedInvNum.pdf")

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    fun generateQuotePdf(context: Context, quote: Quote, profile: FreelancerProfile): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawQuoteContent(canvas, quote, profile)

        document.finishPage(page)

        val outputDir = File(context.cacheDir, "shared_docs").apply { mkdirs() }
        val sanitizedQteNum = quote.quoteNumber.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val file = File(outputDir, "Quote_$sanitizedQteNum.pdf")

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    private fun drawInvoiceContent(canvas: Canvas, invoice: Invoice, profile: FreelancerProfile) {
        val items = LineItemConverter.fromJson(invoice.itemsJson)
        val currency = profile.currencySymbol.ifBlank { "R" }

        // Paints
        val primaryPaint = Paint().apply {
            color = Color.rgb(13, 92, 58) // Emerald
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val headerTitlePaint = Paint().apply {
            color = Color.rgb(20, 30, 25)
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subheaderPaint = Paint().apply {
            color = Color.rgb(90, 100, 95)
            textSize = 9.5f
            isAntiAlias = true
        }
        val docTypePaint = Paint().apply {
            color = Color.rgb(13, 92, 58)
            textSize = 26f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        val docNumberPaint = Paint().apply {
            color = Color.rgb(50, 60, 55)
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        val sectionHeaderPaint = Paint().apply {
            color = Color.rgb(13, 92, 58)
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bodyBoldPaint = Paint().apply {
            color = Color.rgb(30, 40, 35)
            textSize = 9.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(60, 70, 65)
            textSize = 9f
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.rgb(220, 225, 220)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        // Top banner accent
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 8f, primaryPaint)

        var y = 45f

        // Freelancer Info (Top Left)
        canvas.drawText(profile.businessName.ifBlank { "Freelance Services" }, MARGIN, y, headerTitlePaint)
        y += 15f
        canvas.drawText(profile.ownerName, MARGIN, y, bodyBoldPaint)
        y += 13f
        if (profile.email.isNotBlank()) {
            canvas.drawText(profile.email, MARGIN, y, subheaderPaint)
            y += 12f
        }
        if (profile.phone.isNotBlank()) {
            canvas.drawText(profile.phone, MARGIN, y, subheaderPaint)
            y += 12f
        }
        if (profile.address.isNotBlank()) {
            canvas.drawText("${profile.address}, ${profile.cityStateZip}", MARGIN, y, subheaderPaint)
            y += 12f
        }

        // Document title (Top Right)
        val rightX = PAGE_WIDTH - MARGIN
        canvas.drawText("INVOICE", rightX, 48f, docTypePaint)
        canvas.drawText(invoice.invoiceNumber, rightX, 66f, docNumberPaint)

        var currentRightY = 80f
        val refPaint = Paint().apply {
            color = Color.rgb(40, 60, 50)
            textSize = 9.5f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        // Put either the invite number or po number top right just below where the invoice number goes!
        if (invoice.poNumber.isNotBlank()) {
            canvas.drawText("PO Number: ${invoice.poNumber}", rightX, currentRightY, refPaint)
            currentRightY += 13f
        }
        if (invoice.inviteNumber.isNotBlank()) {
            canvas.drawText("Job Invite: ${invoice.inviteNumber}", rightX, currentRightY, refPaint)
            currentRightY += 13f
        }

        canvas.drawText("Status: ${invoice.status.uppercase()}", rightX, currentRightY, Paint().apply {
            color = when (invoice.status) {
                "Paid" -> Color.rgb(22, 130, 70)
                "Sent" -> Color.rgb(30, 90, 180)
                "Overdue" -> Color.rgb(190, 40, 40)
                else -> Color.rgb(110, 110, 110)
            }
            textSize = 10f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        })

        y = maxOf(y + 15f, currentRightY + 16f)

        // Divider
        canvas.drawLine(MARGIN, y, rightX, y, linePaint)
        y += 18f

        // Meta & Client Grid
        val col2X = PAGE_WIDTH / 2f + 20f

        // Left Column: BILLED TO
        val clientStartY = y
        canvas.drawText("BILLED TO", MARGIN, y, sectionHeaderPaint)
        var clientY = y + 14f
        canvas.drawText(invoice.clientName, MARGIN, clientY, bodyBoldPaint)
        clientY += 13f
        if (invoice.clientCompany.isNotBlank()) {
            canvas.drawText(invoice.clientCompany, MARGIN, clientY, bodyPaint)
            clientY += 12f
        }
        if (invoice.clientEmail.isNotBlank()) {
            canvas.drawText(invoice.clientEmail, MARGIN, clientY, bodyPaint)
            clientY += 12f
        }
        if (invoice.clientPhone.isNotBlank()) {
            canvas.drawText(invoice.clientPhone, MARGIN, clientY, bodyPaint)
            clientY += 12f
        }
        if (invoice.clientAddress.isNotBlank()) {
            canvas.drawText(invoice.clientAddress, MARGIN, clientY, bodyPaint)
            clientY += 12f
        }
        if (invoice.clientCityStateZip.isNotBlank()) {
            canvas.drawText(invoice.clientCityStateZip, MARGIN, clientY, bodyPaint)
            clientY += 12f
        }

        // Right Column: INVOICE DETAILS
        var metaY = clientStartY
        canvas.drawText("INVOICE DETAILS", col2X, metaY, sectionHeaderPaint)
        metaY += 14f
        canvas.drawText("Issue Date: ${Formatters.formatDate(invoice.issueDate)}", col2X, metaY, bodyPaint)
        metaY += 12f
        canvas.drawText("Due Date: ${Formatters.formatDate(invoice.dueDate)}", col2X, metaY, bodyBoldPaint)
        metaY += 12f

        if (invoice.poNumber.isNotBlank()) {
            canvas.drawText("PO Number: ${invoice.poNumber}", col2X, metaY, bodyBoldPaint)
            metaY += 12f
        }
        if (invoice.inviteNumber.isNotBlank()) {
            canvas.drawText("Job Invite / RFP Ref: ${invoice.inviteNumber}", col2X, metaY, bodyPaint)
            metaY += 12f
        }

        y = maxOf(clientY, metaY) + 16f

        // Table Header (Service & Project, Days, Daily Rate, Amount - NO TAX)
        val thBackground = Paint().apply {
            color = Color.rgb(240, 245, 242)
            style = Paint.Style.FILL
        }
        val thHeight = 24f
        canvas.drawRoundRect(RectF(MARGIN, y, rightX, y + thHeight), 4f, 4f, thBackground)

        val descX = MARGIN + 12f
        val qtyX = rightX - 220f
        val rateX = rightX - 140f
        val amountX = rightX - 12f

        val thTextPaint = Paint().apply {
            color = Color.rgb(40, 50, 45)
            textSize = 9.5f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val textOffsetY = y + 16f
        canvas.drawText("SERVICE & PROJECT / DATES", descX, textOffsetY, thTextPaint)
        canvas.drawText("DAYS", qtyX, textOffsetY, thTextPaint)
        canvas.drawText("DAILY RATE", rateX, textOffsetY, thTextPaint)
        canvas.drawText("AMOUNT", amountX, textOffsetY, Paint(thTextPaint).apply { textAlign = Paint.Align.RIGHT })

        y += thHeight + 8f

        // Table Rows
        val rowTitlePaint = Paint().apply {
            color = Color.rgb(25, 35, 30)
            textSize = 9.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val rowSubPaint = Paint().apply {
            color = Color.rgb(90, 100, 95)
            textSize = 8.5f
            isAntiAlias = true
        }
        val rowValPaint = Paint().apply {
            color = Color.rgb(40, 40, 40)
            textSize = 9.5f
            isAntiAlias = true
        }
        val rightAlignRowPaint = Paint(rowValPaint).apply { textAlign = Paint.Align.RIGHT }

        for (item in items) {
            val itemY = y + 10f
            // Main title: Service • Project
            val mainTitle = buildString {
                if (item.service.isNotBlank()) append(item.service)
                if (item.project.isNotBlank()) {
                    if (isNotEmpty()) append(" • ")
                    append(item.project)
                }
                if (isEmpty()) append("Broadcast Service")
            }
            canvas.drawText(mainTitle, descX, itemY, rowTitlePaint)

            // Numbers: Days, Daily Rate, Amount
            canvas.drawText(if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString(), qtyX, itemY, rowValPaint)
            val rateDisplay = if (item.dateMode == "SEPARATE" && item.separateDays.isNotEmpty()) {
                "Var (${Formatters.formatCurrency(item.unitPrice, currency)})"
            } else {
                Formatters.formatCurrency(item.dailyRate, currency)
            }
            canvas.drawText(rateDisplay, rateX, itemY, rowValPaint)
            canvas.drawText(Formatters.formatCurrency(item.total, currency), amountX, itemY, rightAlignRowPaint)

            // Subtitle: Dates or breakdown
            val datesSummary = item.formattedDatesSummary
            if (datesSummary.isNotBlank()) {
                canvas.drawText("Dates: $datesSummary", descX, itemY + 12f, rowSubPaint)
                y += 28f
            } else {
                y += 22f
            }

            canvas.drawLine(MARGIN, y, rightX, y, Paint().apply {
                color = Color.rgb(242, 244, 242)
                strokeWidth = 1f
            })
            y += 5f
        }

        y += 15f

        // Summary Totals Box (Right aligned - NO TAX)
        val sumBoxLeft = rightX - 220f
        val sumLabelPaint = Paint().apply {
            color = Color.rgb(80, 90, 85)
            textSize = 10f
            isAntiAlias = true
        }
        val sumValPaint = Paint().apply {
            color = Color.rgb(30, 40, 35)
            textSize = 10f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        canvas.drawText("Subtotal", sumBoxLeft, y, sumLabelPaint)
        canvas.drawText(Formatters.formatCurrency(invoice.subtotal, currency), rightX - 10f, y, sumValPaint)
        y += 18f

        // Total Highlight Box
        val totalHighlight = Paint().apply {
            color = Color.rgb(235, 246, 240)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(sumBoxLeft - 8f, y - 2f, rightX, y + 28f), 4f, 4f, totalHighlight)

        val totalTitlePaint = Paint().apply {
            color = Color.rgb(13, 92, 58)
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val totalValPaint = Paint().apply {
            color = Color.rgb(13, 92, 58)
            textSize = 13f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        canvas.drawText("TOTAL DUE", sumBoxLeft, y + 17f, totalTitlePaint)
        canvas.drawText(Formatters.formatCurrency(invoice.total, currency), rightX - 10f, y + 17f, totalValPaint)

        y += 50f

        // Payment Notes & Banking Instructions (Bottom Left)
        val instr = invoice.paymentInstructions.ifBlank { profile.paymentInstructions }
        if (instr.isNotBlank()) {
            canvas.drawText("BANKING & PAYMENT DETAILS", MARGIN, y, sectionHeaderPaint)
            y += 14f
            val lines = instr.split("\n")
            for (line in lines) {
                canvas.drawText(line, MARGIN, y, bodyPaint)
                y += 12f
            }
            y += 8f
        }

        if (invoice.notes.isNotBlank()) {
            canvas.drawText("NOTES", MARGIN, y, sectionHeaderPaint)
            y += 14f
            canvas.drawText(invoice.notes, MARGIN, y, bodyPaint)
            y += 16f
        }

        // Footer
        val footerPaint = Paint().apply {
            color = Color.rgb(160, 170, 165)
            textSize = 8.5f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Thank you for your business! • Freelance Invoice", PAGE_WIDTH / 2f, PAGE_HEIGHT - 20f, footerPaint)
    }

    private fun drawQuoteContent(canvas: Canvas, quote: Quote, profile: FreelancerProfile) {
        val items = LineItemConverter.fromJson(quote.itemsJson)
        val currency = profile.currencySymbol.ifBlank { "R" }

        val primaryPaint = Paint().apply {
            color = Color.rgb(13, 92, 58)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val headerTitlePaint = Paint().apply {
            color = Color.rgb(20, 30, 25)
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subheaderPaint = Paint().apply {
            color = Color.rgb(90, 100, 95)
            textSize = 9.5f
            isAntiAlias = true
        }
        val docTypePaint = Paint().apply {
            color = Color.rgb(13, 92, 58)
            textSize = 26f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        val docNumberPaint = Paint().apply {
            color = Color.rgb(50, 60, 55)
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        val sectionHeaderPaint = Paint().apply {
            color = Color.rgb(13, 92, 58)
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bodyBoldPaint = Paint().apply {
            color = Color.rgb(30, 40, 35)
            textSize = 9.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(60, 70, 65)
            textSize = 9f
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.rgb(220, 225, 220)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        // Top accent
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 8f, primaryPaint)

        var y = 45f

        // Top Left Freelancer info
        canvas.drawText(profile.businessName.ifBlank { "Freelance Studio" }, MARGIN, y, headerTitlePaint)
        y += 15f
        canvas.drawText(profile.ownerName, MARGIN, y, bodyBoldPaint)
        y += 13f
        if (profile.email.isNotBlank()) {
            canvas.drawText(profile.email, MARGIN, y, subheaderPaint)
            y += 12f
        }
        if (profile.phone.isNotBlank()) {
            canvas.drawText(profile.phone, MARGIN, y, subheaderPaint)
            y += 12f
        }

        // Top Right
        val rightX = PAGE_WIDTH - MARGIN
        canvas.drawText("PRICE QUOTE", rightX, 48f, docTypePaint)
        canvas.drawText(quote.quoteNumber, rightX, 66f, docNumberPaint)

        var currentRightY = 80f
        val refPaint = Paint().apply {
            color = Color.rgb(40, 60, 50)
            textSize = 9.5f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        // Put either the invite number or po number top right just below where the quote/invoice number goes!
        if (quote.inviteNumber.isNotBlank()) {
            canvas.drawText("Job Invite: ${quote.inviteNumber}", rightX, currentRightY, refPaint)
            currentRightY += 13f
        }
        if (quote.poNumber.isNotBlank()) {
            canvas.drawText("PO Number: ${quote.poNumber}", rightX, currentRightY, refPaint)
            currentRightY += 13f
        }

        canvas.drawText("Status: ${quote.status.uppercase()}", rightX, currentRightY, Paint().apply {
            color = when (quote.status) {
                "Accepted" -> Color.rgb(22, 130, 70)
                "Converted" -> Color.rgb(13, 92, 58)
                else -> Color.rgb(190, 110, 20)
            }
            textSize = 10f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        })

        y = maxOf(y + 15f, currentRightY + 16f)
        canvas.drawLine(MARGIN, y, rightX, y, linePaint)
        y += 18f

        // Grid
        val col2X = PAGE_WIDTH / 2f + 20f

        // Left: Prepared For
        val clientStartY = y
        canvas.drawText("PREPARED FOR", MARGIN, y, sectionHeaderPaint)
        var clientY = y + 14f
        canvas.drawText(quote.clientName, MARGIN, clientY, bodyBoldPaint)
        clientY += 13f
        if (quote.clientCompany.isNotBlank()) {
            canvas.drawText(quote.clientCompany, MARGIN, clientY, bodyPaint)
            clientY += 12f
        }
        if (quote.clientEmail.isNotBlank()) {
            canvas.drawText(quote.clientEmail, MARGIN, clientY, bodyPaint)
            clientY += 12f
        }
        if (quote.clientPhone.isNotBlank()) {
            canvas.drawText(quote.clientPhone, MARGIN, clientY, bodyPaint)
            clientY += 12f
        }

        // Right: Quote details
        var metaY = clientStartY
        canvas.drawText("QUOTE DETAILS", col2X, metaY, sectionHeaderPaint)
        metaY += 14f
        canvas.drawText("Quote Date: ${Formatters.formatDate(quote.issueDate)}", col2X, metaY, bodyPaint)
        metaY += 12f
        canvas.drawText("Valid Until: ${Formatters.formatDate(quote.expiryDate)}", col2X, metaY, bodyBoldPaint)
        metaY += 12f

        if (quote.inviteNumber.isNotBlank()) {
            canvas.drawText("Job Invite / RFP #: ${quote.inviteNumber}", col2X, metaY, bodyBoldPaint)
            metaY += 12f
        }
        if (quote.convertedInvoiceNumber != null) {
            canvas.drawText("Converted to Invoice: ${quote.convertedInvoiceNumber}", col2X, metaY, bodyBoldPaint)
            metaY += 12f
        }

        y = maxOf(clientY, metaY) + 16f

        // Table header (NO TAX)
        val thBackground = Paint().apply {
            color = Color.rgb(240, 245, 242)
            style = Paint.Style.FILL
        }
        val thHeight = 24f
        canvas.drawRoundRect(RectF(MARGIN, y, rightX, y + thHeight), 4f, 4f, thBackground)

        val descX = MARGIN + 12f
        val qtyX = rightX - 220f
        val rateX = rightX - 140f
        val amountX = rightX - 12f

        val thTextPaint = Paint().apply {
            color = Color.rgb(40, 50, 45)
            textSize = 9.5f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val textOffsetY = y + 16f
        canvas.drawText("SERVICE & PROJECT / DATES", descX, textOffsetY, thTextPaint)
        canvas.drawText("DAYS", qtyX, textOffsetY, thTextPaint)
        canvas.drawText("DAILY RATE", rateX, textOffsetY, thTextPaint)
        canvas.drawText("AMOUNT", amountX, textOffsetY, Paint(thTextPaint).apply { textAlign = Paint.Align.RIGHT })

        y += thHeight + 8f

        val rowTitlePaint = Paint().apply {
            color = Color.rgb(25, 35, 30)
            textSize = 9.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val rowSubPaint = Paint().apply {
            color = Color.rgb(90, 100, 95)
            textSize = 8.5f
            isAntiAlias = true
        }
        val rowValPaint = Paint().apply {
            color = Color.rgb(40, 40, 40)
            textSize = 9.5f
            isAntiAlias = true
        }
        val rightAlignRowPaint = Paint(rowValPaint).apply { textAlign = Paint.Align.RIGHT }

        for (item in items) {
            val itemY = y + 10f
            val mainTitle = buildString {
                if (item.service.isNotBlank()) append(item.service)
                if (item.project.isNotBlank()) {
                    if (isNotEmpty()) append(" • ")
                    append(item.project)
                }
                if (isEmpty()) append("Broadcast Service")
            }
            canvas.drawText(mainTitle, descX, itemY, rowTitlePaint)

            canvas.drawText(if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString(), qtyX, itemY, rowValPaint)
            val rateDisplay = if (item.dateMode == "SEPARATE" && item.separateDays.isNotEmpty()) {
                "Var (${Formatters.formatCurrency(item.unitPrice, currency)})"
            } else {
                Formatters.formatCurrency(item.dailyRate, currency)
            }
            canvas.drawText(rateDisplay, rateX, itemY, rowValPaint)
            canvas.drawText(Formatters.formatCurrency(item.total, currency), amountX, itemY, rightAlignRowPaint)

            val datesSummary = item.formattedDatesSummary
            if (datesSummary.isNotBlank()) {
                canvas.drawText("Dates: $datesSummary", descX, itemY + 12f, rowSubPaint)
                y += 28f
            } else {
                y += 22f
            }

            canvas.drawLine(MARGIN, y, rightX, y, Paint().apply {
                color = Color.rgb(242, 244, 242)
                strokeWidth = 1f
            })
            y += 5f
        }

        y += 15f

        val sumBoxLeft = rightX - 220f
        val sumLabelPaint = Paint().apply {
            color = Color.rgb(80, 90, 85)
            textSize = 10f
            isAntiAlias = true
        }
        val sumValPaint = Paint().apply {
            color = Color.rgb(30, 40, 35)
            textSize = 10f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        canvas.drawText("Subtotal", sumBoxLeft, y, sumLabelPaint)
        canvas.drawText(Formatters.formatCurrency(quote.subtotal, currency), rightX - 10f, y, sumValPaint)
        y += 18f

        val totalHighlight = Paint().apply {
            color = Color.rgb(235, 246, 240)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(sumBoxLeft - 8f, y - 2f, rightX, y + 28f), 4f, 4f, totalHighlight)

        val totalTitlePaint = Paint().apply {
            color = Color.rgb(13, 92, 58)
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val totalValPaint = Paint().apply {
            color = Color.rgb(13, 92, 58)
            textSize = 13f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        canvas.drawText("ESTIMATED TOTAL", sumBoxLeft, y + 17f, totalTitlePaint)
        canvas.drawText(Formatters.formatCurrency(quote.total, currency), rightX - 10f, y + 17f, totalValPaint)

        y += 50f

        if (quote.terms.isNotBlank()) {
            canvas.drawText("PROPOSED TERMS", MARGIN, y, sectionHeaderPaint)
            y += 14f
            canvas.drawText(quote.terms, MARGIN, y, bodyPaint)
            y += 16f
        }

        if (quote.notes.isNotBlank()) {
            canvas.drawText("NOTES", MARGIN, y, sectionHeaderPaint)
            y += 14f
            canvas.drawText(quote.notes, MARGIN, y, bodyPaint)
            y += 16f
        }

        val footerPaint = Paint().apply {
            color = Color.rgb(160, 170, 165)
            textSize = 8.5f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Questions? Contact ${profile.email} • Freelance Quote", PAGE_WIDTH / 2f, PAGE_HEIGHT - 20f, footerPaint)
    }
}
