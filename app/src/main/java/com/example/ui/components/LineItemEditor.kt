package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.FreelanceServices
import com.example.data.model.LineItem
import com.example.util.Formatters

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LineItemEditor(
    items: List<LineItem>,
    currencySymbol: String = "R",
    onItemsChange: (List<LineItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var availableServices by remember {
        mutableStateOf(FreelanceServices.getAllServices(context))
    }

    var showAddCustomServiceDialog by remember { mutableStateOf(false) }
    var newCustomServiceText by remember { mutableStateOf("") }
    var activeItemIndexForNewService by remember { mutableStateOf<Int?>(null) }

    // State for opening the DateSelectionDialog
    var editingDatesForIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Services & Projects (${items.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedButton(
                onClick = {
                    val newItem = LineItem(
                        project = "",
                        service = availableServices.firstOrNull() ?: "vMix Operator",
                        dateMode = "RANGE",
                        startDate = "",
                        endDate = "",
                        dates = "",
                        days = 1.0,
                        dailyRate = 4500.0
                    )
                    onItemsChange(items + newItem)
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_line_item_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Service")
            }
        }

        Spacer(Modifier.height(12.dp))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No services / projects added yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            onItemsChange(
                                listOf(
                                    LineItem(
                                        project = "Broadcast Production",
                                        service = "TD/Vision Mixer",
                                        days = 1.0,
                                        dailyRate = 4500.0
                                    )
                                )
                            )
                        }
                    ) {
                        Text("Add First Service")
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.forEachIndexed { index, item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("line_item_card_$index")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            // Header: Item # and Delete
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Item #${index + 1}: ${item.service.ifBlank { "Service" }}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(
                                    onClick = {
                                        val updated = items.toMutableList().apply { removeAt(index) }
                                        onItemsChange(updated)
                                    },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("delete_item_button_$index")
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Remove Item",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            // 1. Service Selection & Custom Service
                            Text(
                                text = "Service Rendered:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))

                            // Chip selector row with scroll
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                availableServices.forEach { serviceName ->
                                    val isSelected = item.service.equals(serviceName, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            val updated = items.toMutableList().apply {
                                                this[index] = item.copy(service = serviceName)
                                            }
                                            onItemsChange(updated)
                                        },
                                        label = { Text(serviceName, fontSize = 11.sp) },
                                        modifier = Modifier.testTag("chip_service_${serviceName.replace(" ", "_")}_$index")
                                    )
                                }

                                SuggestionChip(
                                    onClick = {
                                        activeItemIndexForNewService = index
                                        newCustomServiceText = ""
                                        showAddCustomServiceDialog = true
                                    },
                                    label = { Text("+ Add Other", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            // 2. Project Input (formerly Description)
                            OutlinedTextField(
                                value = item.project,
                                onValueChange = { newProject ->
                                    val updated = items.toMutableList().apply {
                                        this[index] = item.copy(project = newProject)
                                    }
                                    onItemsChange(updated)
                                },
                                label = { Text("Project (e.g. Springbok Tour, Live Stream Gala)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("item_project_input_$index"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            Spacer(Modifier.height(10.dp))

                            // 3. Date Selection & Rate Configuration
                            Text(
                                text = "Booking Dates & Rates:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))

                            OutlinedCard(
                                onClick = { editingDatesForIndex = index },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pick_dates_button_$index")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            if (item.dateMode == "SEPARATE") Icons.Default.Event else Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = if (item.dateMode == "SEPARATE") "Separate Days Mode" else "Date Range Mode",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            val summaryText = item.formattedDatesSummary.ifBlank { "Tap to select dates (Range or Separate days)" }
                                            Text(
                                                text = summaryText,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Icon(
                                        Icons.Default.EditCalendar,
                                        contentDescription = "Edit dates",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            // If in RANGE mode, show Days & Daily Rate inputs
                            if (item.dateMode != "SEPARATE" || item.separateDays.isEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = if (item.days == 0.0) "" else item.days.toString().removeSuffix(".0"),
                                        onValueChange = { newDaysStr ->
                                            val d = newDaysStr.toDoubleOrNull() ?: 0.0
                                            val updated = items.toMutableList().apply {
                                                this[index] = item.copy(days = d)
                                            }
                                            onItemsChange(updated)
                                        },
                                        label = { Text("Days") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("item_days_input_$index"),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    OutlinedTextField(
                                        value = if (item.dailyRate == 0.0) "" else item.dailyRate.toString().removeSuffix(".0"),
                                        onValueChange = { newRateStr ->
                                            val rate = newRateStr.toDoubleOrNull() ?: 0.0
                                            val updated = items.toMutableList().apply {
                                                this[index] = item.copy(dailyRate = rate)
                                            }
                                            onItemsChange(updated)
                                        },
                                        label = { Text("Daily Rate ($currencySymbol)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier
                                            .weight(1.4f)
                                            .testTag("item_daily_rate_input_$index"),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            } else {
                                // In SEPARATE mode with individual days
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "Individual Days & Rates (${item.separateDays.size} days):",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    item.separateDays.forEach { d ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "• ${d.date}" + if (d.label.isNotBlank()) " (${d.label})" else "",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = Formatters.formatCurrency(d.rate, currencySymbol),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            // Total
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "Line Total: ${Formatters.formatCurrency(item.total, currencySymbol)}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Date Selection Dialog
    editingDatesForIndex?.let { idx ->
        if (idx in items.indices) {
            val currentItem = items[idx]
            DateSelectionDialog(
                initialMode = currentItem.dateMode,
                initialStartDate = currentItem.startDate,
                initialEndDate = currentItem.endDate,
                initialSeparateDays = currentItem.separateDays,
                defaultDailyRate = currentItem.dailyRate,
                currencySymbol = currencySymbol,
                onDismiss = { editingDatesForIndex = null },
                onDatesSelected = { result ->
                    val updated = items.toMutableList()
                    when (result) {
                        is DateSelectionResult.Range -> {
                            updated[idx] = currentItem.copy(
                                dateMode = "RANGE",
                                startDate = result.startDate,
                                endDate = result.endDate,
                                dates = result.datesFormatted,
                                days = result.daysCount,
                                separateDays = emptyList()
                            )
                        }
                        is DateSelectionResult.SeparateDays -> {
                            val totalDays = result.days.size.toDouble().coerceAtLeast(1.0)
                            val summary = result.days.joinToString(", ") { it.date }
                            updated[idx] = currentItem.copy(
                                dateMode = "SEPARATE",
                                dates = summary,
                                days = totalDays,
                                separateDays = result.days
                            )
                        }
                    }
                    onItemsChange(updated)
                    editingDatesForIndex = null
                }
            )
        }
    }

    // Custom Service Dialog
    if (showAddCustomServiceDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomServiceDialog = false },
            title = { Text("Add Custom Service", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Enter the title of the service you render (e.g. Fiber Engineer, Drone Operator, Audio Technician):",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newCustomServiceText,
                        onValueChange = { newCustomServiceText = it },
                        label = { Text("Service Title") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newCustomServiceText.trim()
                        if (trimmed.isNotBlank()) {
                            val newServices = FreelanceServices.addCustomService(context, trimmed)
                            availableServices = newServices
                            activeItemIndexForNewService?.let { idx ->
                                if (idx in items.indices) {
                                    val updated = items.toMutableList()
                                    updated[idx] = updated[idx].copy(service = trimmed)
                                    onItemsChange(updated)
                                }
                            }
                        }
                        showAddCustomServiceDialog = false
                    }
                ) {
                    Text("Add & Select")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddCustomServiceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
