package com.example.data.model

data class DayRate(
    val date: String = "",       // e.g. "15 Oct 2026"
    val rate: Double = 0.0,      // Daily rate in Rand for this day
    val label: String = ""       // Optional note, e.g. "Setup & Rehearsal", "Live Match"
)

data class LineItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val project: String = "",          // Project Name / Production (e.g. "Springbok Tour", "Gala Live")
    val service: String = "",          // Service Rendered (e.g. "vMix Operator", "Vision Mixer", etc.)
    val dateMode: String = "RANGE",    // "RANGE" or "SEPARATE"
    val startDate: String = "",        // For RANGE mode: "14 Oct 2026"
    val endDate: String = "",          // For RANGE mode: "16 Oct 2026"
    val dates: String = "",            // Selected date range or separate dates summary (e.g. "14 Oct – 16 Oct 2026")
    val days: Double = 1.0,            // Number of days
    val dailyRate: Double = 0.0,       // Standard daily rate in Rand
    val separateDays: List<DayRate> = emptyList(), // For SEPARATE mode: individual days with rates
    val notes: String = ""             // Additional notes / specifics
) {
    // Secondary constructor for backwards compatibility with any existing calls
    constructor(
        description: String,
        quantity: Double = 1.0,
        unitPrice: Double = 0.0,
        id: String = java.util.UUID.randomUUID().toString()
    ) : this(
        id = id,
        project = if (description.contains(" • ")) description.substringAfter(" • ").trim() else "",
        service = if (description.contains(" • ")) description.substringBefore(" • ").trim() else description.trim(),
        dateMode = "RANGE",
        startDate = "",
        endDate = "",
        dates = "",
        days = quantity,
        dailyRate = unitPrice,
        separateDays = emptyList(),
        notes = ""
    )

    val description: String
        get() = when {
            service.isNotBlank() && project.isNotBlank() -> "$service • $project"
            service.isNotBlank() -> service
            project.isNotBlank() -> project
            else -> "Broadcast / Technical Service"
        }

    val quantity: Double
        get() = if (dateMode == "SEPARATE" && separateDays.isNotEmpty()) {
            separateDays.size.toDouble()
        } else {
            days
        }

    val unitPrice: Double
        get() = if (dateMode == "SEPARATE" && separateDays.isNotEmpty()) {
            if (separateDays.isNotEmpty()) total / separateDays.size else dailyRate
        } else {
            dailyRate
        }

    val total: Double
        get() = if (dateMode == "SEPARATE" && separateDays.isNotEmpty()) {
            separateDays.sumOf { it.rate }
        } else {
            days * dailyRate
        }

    val subtotal: Double
        get() = total

    // Helper formatting for dates displayed on invoice & PDF
    val formattedDatesSummary: String
        get() = when {
            dateMode == "SEPARATE" && separateDays.isNotEmpty() -> {
                separateDays.joinToString(", ") { d ->
                    if (d.label.isNotBlank()) "${d.date} (${d.label})" else d.date
                }
            }
            dates.isNotBlank() -> dates
            startDate.isNotBlank() && endDate.isNotBlank() -> {
                if (startDate == endDate) startDate else "$startDate – $endDate"
            }
            startDate.isNotBlank() -> startDate
            else -> ""
        }
}
