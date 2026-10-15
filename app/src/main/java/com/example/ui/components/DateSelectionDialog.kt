package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayRate
import com.example.util.Formatters
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed interface DateSelectionResult {
    data class Range(
        val startDate: String,
        val endDate: String,
        val datesFormatted: String,
        val daysCount: Double
    ) : DateSelectionResult

    data class SeparateDays(
        val days: List<DayRate>
    ) : DateSelectionResult
}

@Composable
fun DateSelectionDialog(
    initialMode: String = "RANGE",
    initialStartDate: String = "",
    initialEndDate: String = "",
    initialSeparateDays: List<DayRate> = emptyList(),
    defaultDailyRate: Double = 0.0,
    currencySymbol: String = "R",
    onDismiss: () -> Unit,
    onDatesSelected: (DateSelectionResult) -> Unit
) {
    val context = LocalContext.current
    var selectedMode by remember { mutableStateOf(if (initialMode == "SEPARATE" || initialSeparateDays.isNotEmpty()) "separate" else "range") }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    // Range state
    var startCal by remember {
        mutableStateOf(Calendar.getInstance())
    }
    var endCal by remember {
        mutableStateOf(Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) })
    }

    // Separate dates with custom rates
    val separateDays = remember {
        mutableStateListOf<DayRate>().apply {
            if (initialSeparateDays.isNotEmpty()) {
                addAll(initialSeparateDays)
            }
        }
    }

    fun showDatePicker(initialCalendar: Calendar, onPicked: (Calendar) -> Unit) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                onPicked(picked)
            },
            initialCalendar.get(Calendar.YEAR),
            initialCalendar.get(Calendar.MONTH),
            initialCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text("Select Booking Dates", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Select whether this booking is for a continuous Date Range or Separate Days (which can have different rates).",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(14.dp))

                // Mode Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedMode == "range",
                        onClick = { selectedMode = "range" },
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text("Date Range", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mode_date_range")
                    )

                    FilterChip(
                        selected = selectedMode == "separate",
                        onClick = { selectedMode = "separate" },
                        leadingIcon = {
                            Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text("Separate Days", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mode_separate_days")
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (selectedMode == "range") {
                    // Start date selector
                    OutlinedCard(
                        onClick = {
                            showDatePicker(startCal) { picked ->
                                startCal = picked
                                if (endCal.before(picked)) {
                                    endCal = picked.clone() as Calendar
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pick_start_date_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Start Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(dateFormat.format(startCal.time), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // End date selector
                    OutlinedCard(
                        onClick = {
                            showDatePicker(endCal) { picked ->
                                if (picked.before(startCal)) {
                                    startCal = picked.clone() as Calendar
                                }
                                endCal = picked
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pick_end_date_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("End Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(dateFormat.format(endCal.time), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    val diffMillis = endCal.timeInMillis - startCal.timeInMillis
                    val calculatedDays = (diffMillis / (24L * 60 * 60 * 1000) + 1).toDouble().coerceAtLeast(1.0)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Duration: ${calculatedDays.toInt()} ${if (calculatedDays == 1.0) "day" else "days"} (${dateFormat.format(startCal.time)} – ${dateFormat.format(endCal.time)})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    // Separate dates mode with individual day rates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selected Days (${separateDays.size})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedButton(
                            onClick = {
                                showDatePicker(Calendar.getInstance()) { picked ->
                                    val formatted = dateFormat.format(picked.time)
                                    separateDays.add(
                                        DayRate(
                                            date = formatted,
                                            rate = defaultDailyRate,
                                            label = ""
                                        )
                                    )
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("add_separate_date_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Day")
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    if (separateDays.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tap '+ Add Day' to pick specific dates and assign rates to each.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            separateDays.forEachIndexed { index, dayItem ->
                                OutlinedCard(
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable {
                                                    showDatePicker(Calendar.getInstance()) { picked ->
                                                        val formatted = dateFormat.format(picked.time)
                                                        separateDays[index] = dayItem.copy(date = formatted)
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                                Spacer(Modifier.width(6.dp))
                                                Text(dayItem.date.ifBlank { "Select Date" }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }

                                            IconButton(
                                                onClick = { separateDays.removeAt(index) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                            }
                                        }

                                        Spacer(Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = dayItem.label,
                                                onValueChange = { newLabel ->
                                                    separateDays[index] = dayItem.copy(label = newLabel)
                                                },
                                                label = { Text("Day Note (e.g. Rehearsal / Match)") },
                                                modifier = Modifier.weight(1.4f),
                                                singleLine = true,
                                                shape = RoundedCornerShape(8.dp)
                                            )

                                            OutlinedTextField(
                                                value = if (dayItem.rate == 0.0) "" else dayItem.rate.toString().removeSuffix(".0"),
                                                onValueChange = { newRateStr ->
                                                    val r = newRateStr.toDoubleOrNull() ?: 0.0
                                                    separateDays[index] = dayItem.copy(rate = r)
                                                },
                                                label = { Text("Rate ($currencySymbol)") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Total summary of separate days
                            val totalSeparate = separateDays.sumOf { it.rate }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "Total: ${separateDays.size} days • ${Formatters.formatCurrency(totalSeparate, currencySymbol)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedMode == "range") {
                        val diffMillis = endCal.timeInMillis - startCal.timeInMillis
                        val calculatedDays = (diffMillis / (24L * 60 * 60 * 1000) + 1).toDouble().coerceAtLeast(1.0)
                        val startStr = dateFormat.format(startCal.time)
                        val endStr = dateFormat.format(endCal.time)
                        val formattedRange = if (calculatedDays == 1.0) startStr else "$startStr – $endStr"
                        onDatesSelected(
                            DateSelectionResult.Range(
                                startDate = startStr,
                                endDate = endStr,
                                datesFormatted = formattedRange,
                                daysCount = calculatedDays
                            )
                        )
                    } else {
                        if (separateDays.isEmpty()) {
                            val today = dateFormat.format(Calendar.getInstance().time)
                            onDatesSelected(
                                DateSelectionResult.SeparateDays(
                                    listOf(DayRate(date = today, rate = defaultDailyRate, label = ""))
                                )
                            )
                        } else {
                            onDatesSelected(DateSelectionResult.SeparateDays(separateDays.toList()))
                        }
                    }
                },
                modifier = Modifier.testTag("apply_dates_button")
            ) {
                Text("Apply Dates")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
