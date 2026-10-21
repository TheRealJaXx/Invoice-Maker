package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.data.model.FreelancerProfile
import com.example.data.model.Invoice
import com.example.data.model.LineItem
import com.example.data.model.LineItemConverter
import com.example.data.model.Quote
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object PdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN = 40f

    private val DATE_FORMATS = listOf(
        "d MMM yyyy",
        "dd MMM yyyy",
        "yyyy-MM-dd",
        "d/M/yyyy",
        "dd/MM/yyyy",
        "d MMMM yyyy"
    )

    private fun parseDate(dateStr: String): Date? {
        val clean = dateStr.trim()
        for (fmt in DATE_FORMATS) {
            try {
                val sdf = SimpleDateFormat(fmt, Locale.US)
                sdf.isLenient = false
                return sdf.parse(clean)
            } catch (_: Exception) {}
        }
        return null
    }

    /**
     * Expands dates into individual date strings.
     * Supports:
     * - Explicit separate days
     * - Date ranges (expanded day by day)
     * - Comma or newline separated dates
     * - Single date
     */
    fun expandDates(item: LineItem): List<String> {
        // 1. Separate days explicit list
        if (item.dateMode == "SEPARATE" && item.separateDays.isNotEmpty()) {
            return item.separateDays.map { d ->
                if (d.label.isNotBlank()) "${d.date} (${d.label})" else d.date
            }
        }

        // 2. Range mode with startDate and endDate
        if (item.startDate.isNotBlank() && item.endDate.isNotBlank()) {
            val start = parseDate(item.startDate)
            val end = parseDate(item.endDate)
            if (start != null && end != null && !start.after(end)) {
                val out = mutableListOf<String>()
                val cal = Calendar.getInstance()
                cal.time = start
                val endCal = Calendar.getInstance()
                endCal.time = end
                val outFmt = SimpleDateFormat("d MMM yyyy", Locale.US)
                var count = 0
                while (!cal.after(endCal) && count < 60) {
                    out.add(outFmt.format(cal.time))
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                    count++
                }
                if (out.isNotEmpty()) return out
            }
            if (item.startDate == item.endDate) {
                return listOf(item.startDate)
            }
        }

        // 3. String dates with newlines
        if (item.dates.contains("\n")) {
            val lines = item.dates.lines().map { it.trim().removePrefix("•").trim() }.filter { it.isNotBlank() }
            if (lines.isNotEmpty()) return lines
        }

        // 4. String dates with commas
        if (item.dates.contains(",")) {
            val parts = item.dates.split(",").map { it.trim().removePrefix("•").trim() }.filter { it.isNotBlank() }
            if (parts.isNotEmpty()) return parts
        }

        // 5. String dates with range dash
        if (item.dates.isNotBlank()) {
            val dashSplit = if (item.dates.contains("–")) item.dates.split("–") else if (item.dates.contains(" - ")) item.dates.split(" - ") else null
            if (dashSplit != null && dashSplit.size == 2) {
                val s = parseDate(dashSplit[0])
                val e = parseDate(dashSplit[1])
                if (s != null && e != null && !s.after(e)) {
                    val out = mutableListOf<String>()
                    val cal = Calendar.getInstance()
                    cal.time = s
                    val endCal = Calendar.getInstance()
                    endCal.time = e
                    val outFmt = SimpleDateFormat("d MMM yyyy", Locale.US)
                    var count = 0
                    while (!cal.after(endCal) && count < 60) {
                        out.add(outFmt.format(cal.time))
                        cal.add(Calendar.DAY_OF_MONTH, 1)
                        count++
                    }
                    if (out.isNotEmpty()) return out
                }
            }
            return listOf(item.dates.trim())
        }

        if (item.startDate.isNotBlank()) {
            return listOf(item.startDate.trim())
        }

        return emptyList()
    }

    private class PdfPageManager(
        private val document: PdfDocument,
        val docFooterTitle: String
    ) {
        var pageNumber = 1
        private var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var currentPage: PdfDocument.Page = document.startPage(pageInfo)
        var canvas: Canvas = currentPage.canvas
        var y: Float = 0f

        fun startNewPage() {
            drawFooter()
            document.finishPage(currentPage)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            currentPage = document.startPage(pageInfo)
            canvas = currentPage.canvas
            y = MARGIN
        }

        fun drawFooter() {
            val footerPaint = Paint().apply {
                color = Color.rgb(160, 170, 165)
                textSize = 8.5f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(
                "$docFooterTitle • Page $pageNumber",
                PAGE_WIDTH / 2f,
                PAGE_HEIGHT - 20f,
                footerPaint
            )
        }

        fun finish() {
            drawFooter()
            document.finishPage(currentPage)
        }
    }

    fun generateInvoicePdf(context: Context, invoice: Invoice, profile: FreelancerProfile): File {
        val document = PdfDocument()
        val pageManager = PdfPageManager(document, "Invoice ${invoice.invoiceNumber}")

        drawInvoiceContent(pageManager, invoice, profile)

        pageManager.finish()

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
        val pageManager = PdfPageManager(document, "Quote ${quote.quoteNumber}")

        drawQuoteContent(pageManager, quote, profile)

        pageManager.finish()

        val outputDir = File(context.cacheDir, "shared_docs").apply { mkdirs() }
        val sanitizedQteNum = quote.quoteNumber.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val file = File(outputDir, "Quote_$sanitizedQteNum.pdf")

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    private fun drawTableHeader(canvas: Canvas, y: Float) {
        val rightX = PAGE_WIDTH - MARGIN
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
        canvas.drawText("DATE / JOB / SERVICE", descX, textOffsetY, thTextPaint)
        canvas.drawText("DAYS", qtyX, textOffsetY, thTextPaint)
        canvas.drawText("DAILY RATE", rateX, textOffsetY, thTextPaint)
        canvas.drawText("AMOUNT", amountX, textOffsetY, Paint(thTextPaint).apply { textAlign = Paint.Align.RIGHT })
    }

    private fun drawInvoiceContent(pageManager: PdfPageManager, invoice: Invoice, profile: FreelancerProfile) {
        val items = LineItemConverter.fromJson(invoice.itemsJson)
        val currency = profile.currencySymbol.ifBlank { "R" }
        var canvas = pageManager.canvas

        // Common Paints
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
        if (profile.isIndividual) {
            val displayName = profile.ownerName.ifBlank { "Freelance Professional" }
            canvas.drawText(displayName, MARGIN, y, headerTitlePaint)
            y += 16f
        } else {
            val companyName = profile.businessName.ifBlank { "Freelance Services" }
            canvas.drawText(companyName, MARGIN, y, headerTitlePaint)
            y += 15f
            if (profile.ownerName.isNotBlank()) {
                canvas.drawText(profile.ownerName, MARGIN, y, bodyBoldPaint)
                y += 13f
            }
        }
        if (profile.email.isNotBlank()) {
            canvas.drawText(profile.email, MARGIN, y, subheaderPaint)
            y += 12f
        }
        if (profile.phone.isNotBlank()) {
            canvas.drawText(profile.phone, MARGIN, y, subheaderPaint)
            y += 12f
        }
        val addrStr = listOf(profile.address, profile.cityStateZip).filter { it.isNotBlank() }.joinToString(", ")
        if (addrStr.isNotBlank()) {
            canvas.drawText(addrStr, MARGIN, y, subheaderPaint)
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
        // In both PDFs, the Bill To / Prepared For block must show: client name, company, VAT No, Reg No, the full address (street, suburb, city, postal code), email and phone. Skip lines that are blank.
        val clientStartY = y
        canvas.drawText("BILLED TO", MARGIN, y, sectionHeaderPaint)
        var clientY = y + 14f

        val clientLines = mutableListOf<Pair<String, Paint>>()
        if (invoice.clientName.isNotBlank()) {
            clientLines.add(invoice.clientName to bodyBoldPaint)
        }
        if (invoice.clientCompany.isNotBlank()) {
            clientLines.add(invoice.clientCompany to bodyPaint)
        }
        if (invoice.clientVatNo.isNotBlank()) {
            clientLines.add("VAT No: ${invoice.clientVatNo}" to bodyPaint)
        }
        if (invoice.clientRegNo.isNotBlank()) {
            clientLines.add("Reg No: ${invoice.clientRegNo}" to bodyPaint)
        }
        val fullClientAddress = listOf(invoice.clientAddress, invoice.clientSuburb, invoice.clientCity, invoice.clientPostalCode)
            .filter { it.isNotBlank() }
            .joinToString(", ")
        val finalAddr = fullClientAddress.ifBlank { invoice.clientCityStateZip }
        if (finalAddr.isNotBlank()) {
            clientLines.add(finalAddr to bodyPaint)
        }
        if (invoice.clientEmail.isNotBlank()) {
            clientLines.add(invoice.clientEmail to bodyPaint)
        }
        if (invoice.clientEmail2.isNotBlank()) {
            clientLines.add(invoice.clientEmail2 to bodyPaint)
        }
        if (invoice.clientPhone.isNotBlank()) {
            clientLines.add(invoice.clientPhone to bodyPaint)
        }

        for ((lineText, paint) in clientLines) {
            canvas.drawText(lineText, MARGIN, clientY, paint)
            clientY += 12f
        }

        // Right Column: INVOICE DETAILS
        var metaY = clientStartY
        canvas.drawText("INVOICE DETAILS", col2X, metaY, sectionHeaderPaint)
        metaY += 14f
        canvas.drawText("Issue Date: ${Formatters.formatDate(invoice.issueDate)}", col2X, metaY, bodyPaint)
        metaY += 12f

        // When hasDueDate is false, the due date must not appear anywhere
        if (invoice.hasDueDate) {
            canvas.drawText("Due Date: ${Formatters.formatDate(invoice.dueDate)}", col2X, metaY, bodyBoldPaint)
            metaY += 12f
        }

        if (invoice.poNumber.isNotBlank()) {
            canvas.drawText("PO Number: ${invoice.poNumber}", col2X, metaY, bodyBoldPaint)
            metaY += 12f
        }
        if (invoice.inviteNumber.isNotBlank()) {
            canvas.drawText("Job Invite: ${invoice.inviteNumber}", col2X, metaY, bodyPaint)
            metaY += 12f
        }

        y = maxOf(clientY, metaY) + 16f
        pageManager.y = y

        // Draw Table Header
        drawTableHeader(canvas, pageManager.y)
        pageManager.y += 24f + 8f

        // Table Rows Paints
        val rowDatePaint = Paint().apply {
            color = Color.rgb(20, 30, 25)
            textSize = 9.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val rowJobPaint = Paint().apply {
            color = Color.rgb(40, 55, 48)
            textSize = 9f
            isAntiAlias = true
        }
        val rowServicePaint = Paint().apply {
            color = Color.rgb(80, 95, 88)
            textSize = 9f
            isAntiAlias = true
        }
        val rowValPaint = Paint().apply {
            color = Color.rgb(30, 40, 35)
            textSize = 9.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val rightAlignRowPaint = Paint(rowValPaint).apply { textAlign = Paint.Align.RIGHT }

        val descX = MARGIN + 12f
        val qtyX = rightX - 220f
        val rateX = rightX - 140f
        val amountX = rightX - 12f

        for (item in items) {
            val dateList = expandDates(item)
            val dateEntries = if (dateList.isNotEmpty()) dateList else listOf("")
            val numBlocks = dateEntries.size
            val blockHeight = 3 * 12f + 4f
            val itemHeight = numBlocks * blockHeight + 10f

            // Page break check
            if (pageManager.y + itemHeight > PAGE_HEIGHT - 65f) {
                pageManager.startNewPage()
                drawTableHeader(pageManager.canvas, pageManager.y)
                pageManager.y += 24f + 8f
            }

            canvas = pageManager.canvas
            val itemStartY = pageManager.y + 10f

            // Numbers: Days, Daily Rate, Amount aligned to the first line of the item
            val daysDisplay = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
            canvas.drawText(daysDisplay, qtyX, itemStartY, rowValPaint)

            val rateDisplay = if (item.dateMode == "SEPARATE" && item.separateDays.isNotEmpty()) {
                "Var (${Formatters.formatCurrency(item.unitPrice, currency)})"
            } else {
                Formatters.formatCurrency(item.dailyRate, currency)
            }
            canvas.drawText(rateDisplay, rateX, itemStartY, rowValPaint)
            canvas.drawText(Formatters.formatCurrency(item.total, currency), amountX, itemStartY, rightAlignRowPaint)

            var curY = itemStartY
            for (dateStr in dateEntries) {
                // Line 1: Date
                val displayDate = if (dateStr.isNotBlank()) dateStr else "Date TBA"
                canvas.drawText(displayDate, descX, curY, rowDatePaint)
                curY += 12f

                // Line 2: Job (the project)
                val jobText = item.project.ifBlank { "Production / Job" }
                canvas.drawText(jobText, descX, curY, rowJobPaint)
                curY += 12f

                // Line 3: Service rendered
                val serviceText = item.service.ifBlank { "Broadcast Service" }
                canvas.drawText(serviceText, descX, curY, rowServicePaint)
                curY += 14f
            }

            pageManager.y = curY + 2f
            canvas.drawLine(MARGIN, pageManager.y, rightX, pageManager.y, Paint().apply {
                color = Color.rgb(235, 240, 236)
                strokeWidth = 1f
            })
            pageManager.y += 6f
        }

        pageManager.y += 12f

        // Summary Totals Box & Payment Info check for page break
        if (pageManager.y + 150f > PAGE_HEIGHT - 55f) {
            pageManager.startNewPage()
        }

        canvas = pageManager.canvas
        y = pageManager.y

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

        y += 48f

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

        pageManager.y = y
    }

    private fun drawQuoteContent(pageManager: PdfPageManager, quote: Quote, profile: FreelancerProfile) {
        val items = LineItemConverter.fromJson(quote.itemsJson)
        val currency = profile.currencySymbol.ifBlank { "R" }
        var canvas = pageManager.canvas

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

        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 8f, primaryPaint)

        var y = 45f

        // Freelancer Info
        if (profile.isIndividual) {
            val displayName = profile.ownerName.ifBlank { "Freelance Professional" }
            canvas.drawText(displayName, MARGIN, y, headerTitlePaint)
            y += 16f
        } else {
            val companyName = profile.businessName.ifBlank { "Freelance Services" }
            canvas.drawText(companyName, MARGIN, y, headerTitlePaint)
            y += 15f
            if (profile.ownerName.isNotBlank()) {
                canvas.drawText(profile.ownerName, MARGIN, y, bodyBoldPaint)
                y += 13f
            }
        }
        if (profile.email.isNotBlank()) {
            canvas.drawText(profile.email, MARGIN, y, subheaderPaint)
            y += 12f
        }
        if (profile.phone.isNotBlank()) {
            canvas.drawText(profile.phone, MARGIN, y, subheaderPaint)
            y += 12f
        }
        val addrStr = listOf(profile.address, profile.cityStateZip).filter { it.isNotBlank() }.joinToString(", ")
        if (addrStr.isNotBlank()) {
            canvas.drawText(addrStr, MARGIN, y, subheaderPaint)
            y += 12f
        }

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

        if (quote.inviteNumber.isNotBlank()) {
            canvas.drawText("Job Invite: ${quote.inviteNumber}", rightX, currentRightY, refPaint)
            currentRightY += 13f
        }
        if (quote.poNumber.isNotBlank()) {
            canvas.drawText("PO Ref: ${quote.poNumber}", rightX, currentRightY, refPaint)
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

        // Left: PREPARED FOR
        // In both PDFs, the Bill To / Prepared For block must show: client name, company, VAT No, Reg No, the full address (street, suburb, city, postal code), email and phone. Skip lines that are blank.
        val clientStartY = y
        canvas.drawText("PREPARED FOR", MARGIN, y, sectionHeaderPaint)
        var clientY = y + 14f

        val clientLines = mutableListOf<Pair<String, Paint>>()
        if (quote.clientName.isNotBlank()) {
            clientLines.add(quote.clientName to bodyBoldPaint)
        }
        if (quote.clientCompany.isNotBlank()) {
            clientLines.add(quote.clientCompany to bodyPaint)
        }
        if (quote.clientVatNo.isNotBlank()) {
            clientLines.add("VAT No: ${quote.clientVatNo}" to bodyPaint)
        }
        if (quote.clientRegNo.isNotBlank()) {
            clientLines.add("Reg No: ${quote.clientRegNo}" to bodyPaint)
        }
        val fullClientAddress = listOf(quote.clientAddress, quote.clientSuburb, quote.clientCity, quote.clientPostalCode)
            .filter { it.isNotBlank() }
            .joinToString(", ")
        val finalAddr = fullClientAddress.ifBlank { quote.clientCityStateZip }
        if (finalAddr.isNotBlank()) {
            clientLines.add(finalAddr to bodyPaint)
        }
        if (quote.clientEmail.isNotBlank()) {
            clientLines.add(quote.clientEmail to bodyPaint)
        }
        if (quote.clientEmail2.isNotBlank()) {
            clientLines.add(quote.clientEmail2 to bodyPaint)
        }
        if (quote.clientPhone.isNotBlank()) {
            clientLines.add(quote.clientPhone to bodyPaint)
        }

        for ((lineText, paint) in clientLines) {
            canvas.drawText(lineText, MARGIN, clientY, paint)
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
            canvas.drawText("Job Invite: ${quote.inviteNumber}", col2X, metaY, bodyBoldPaint)
            metaY += 12f
        }
        if (quote.convertedInvoiceNumber != null) {
            canvas.drawText("Converted to Invoice: ${quote.convertedInvoiceNumber}", col2X, metaY, bodyBoldPaint)
            metaY += 12f
        }

        y = maxOf(clientY, metaY) + 16f
        pageManager.y = y

        // Table header
        drawTableHeader(canvas, pageManager.y)
        pageManager.y += 24f + 8f

        val rowDatePaint = Paint().apply {
            color = Color.rgb(20, 30, 25)
            textSize = 9.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val rowJobPaint = Paint().apply {
            color = Color.rgb(40, 55, 48)
            textSize = 9f
            isAntiAlias = true
        }
        val rowServicePaint = Paint().apply {
            color = Color.rgb(80, 95, 88)
            textSize = 9f
            isAntiAlias = true
        }
        val rowValPaint = Paint().apply {
            color = Color.rgb(30, 40, 35)
            textSize = 9.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val rightAlignRowPaint = Paint(rowValPaint).apply { textAlign = Paint.Align.RIGHT }

        val descX = MARGIN + 12f
        val qtyX = rightX - 220f
        val rateX = rightX - 140f
        val amountX = rightX - 12f

        for (item in items) {
            val dateList = expandDates(item)
            val dateEntries = if (dateList.isNotEmpty()) dateList else listOf("")
            val numBlocks = dateEntries.size
            val blockHeight = 3 * 12f + 4f
            val itemHeight = numBlocks * blockHeight + 10f

            if (pageManager.y + itemHeight > PAGE_HEIGHT - 65f) {
                pageManager.startNewPage()
                drawTableHeader(pageManager.canvas, pageManager.y)
                pageManager.y += 24f + 8f
            }

            canvas = pageManager.canvas
            val itemStartY = pageManager.y + 10f

            val daysDisplay = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
            canvas.drawText(daysDisplay, qtyX, itemStartY, rowValPaint)

            val rateDisplay = if (item.dateMode == "SEPARATE" && item.separateDays.isNotEmpty()) {
                "Var (${Formatters.formatCurrency(item.unitPrice, currency)})"
            } else {
                Formatters.formatCurrency(item.dailyRate, currency)
            }
            canvas.drawText(rateDisplay, rateX, itemStartY, rowValPaint)
            canvas.drawText(Formatters.formatCurrency(item.total, currency), amountX, itemStartY, rightAlignRowPaint)

            var curY = itemStartY
            for (dateStr in dateEntries) {
                val displayDate = if (dateStr.isNotBlank()) dateStr else "Date TBA"
                canvas.drawText(displayDate, descX, curY, rowDatePaint)
                curY += 12f

                val jobText = item.project.ifBlank { "Production / Job" }
                canvas.drawText(jobText, descX, curY, rowJobPaint)
                curY += 12f

                val serviceText = item.service.ifBlank { "Broadcast Service" }
                canvas.drawText(serviceText, descX, curY, rowServicePaint)
                curY += 14f
            }

            pageManager.y = curY + 2f
            canvas.drawLine(MARGIN, pageManager.y, rightX, pageManager.y, Paint().apply {
                color = Color.rgb(235, 240, 236)
                strokeWidth = 1f
            })
            pageManager.y += 6f
        }

        pageManager.y += 12f

        if (pageManager.y + 150f > PAGE_HEIGHT - 55f) {
            pageManager.startNewPage()
        }

        canvas = pageManager.canvas
        y = pageManager.y

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

        y += 48f

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

        pageManager.y = y
    }
}
