package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Client
import com.example.data.model.FreelancerProfile
import com.example.data.model.Invoice
import com.example.data.model.LineItem
import com.example.data.model.LineItemConverter
import com.example.ui.components.ClientPickerBottomSheet
import com.example.ui.components.LineItemEditor
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceEditScreen(
    existingInvoice: Invoice?,
    clients: List<Client>,
    profile: FreelancerProfile,
    initialClientId: Long?,
    initialInviteNumber: String,
    initialPoNumber: String,
    onBack: () -> Unit,
    onSaveInvoice: (Invoice) -> Unit,
    onAddNewClient: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var showClientPicker by remember { mutableStateOf(false) }

    // Client state
    var selectedClientId by remember { mutableLongStateOf(existingInvoice?.clientId ?: initialClientId ?: 0L) }
    var clientName by remember { mutableStateOf(existingInvoice?.clientName ?: "") }
    var clientCompany by remember { mutableStateOf(existingInvoice?.clientCompany ?: "") }
    var clientEmail by remember { mutableStateOf(existingInvoice?.clientEmail ?: "") }
    var clientEmail2 by remember { mutableStateOf(existingInvoice?.clientEmail2 ?: "") }
    var clientPhone by remember { mutableStateOf(existingInvoice?.clientPhone ?: "") }
    var clientAddress by remember { mutableStateOf(existingInvoice?.clientAddress ?: "") }
    var clientSuburb by remember { mutableStateOf(existingInvoice?.clientSuburb ?: "") }
    var clientCity by remember { mutableStateOf(existingInvoice?.clientCity ?: "") }
    var clientPostalCode by remember { mutableStateOf(existingInvoice?.clientPostalCode ?: "") }
    var clientCityStateZip by remember { mutableStateOf(existingInvoice?.clientCityStateZip ?: "") }
    var clientVatNo by remember { mutableStateOf(existingInvoice?.clientVatNo ?: "") }
    var clientRegNo by remember { mutableStateOf(existingInvoice?.clientRegNo ?: "") }

    // If initialClientId was passed and no existing invoice, auto-fill from clients list
    LaunchedEffect(initialClientId, clients) {
        if (existingInvoice == null && initialClientId != null && initialClientId > 0) {
            clients.find { it.id == initialClientId }?.let { cl ->
                selectedClientId = cl.id
                clientName = cl.name
                clientCompany = cl.company
                clientEmail = cl.effectiveInvoiceEmail
                clientEmail2 = cl.invoiceEmail2
                clientPhone = cl.phone
                clientAddress = cl.address
                clientSuburb = cl.suburb
                clientCity = cl.city
                clientPostalCode = cl.postalCode
                clientCityStateZip = cl.fullAddress.ifBlank { cl.cityStateZip }
                clientVatNo = cl.vatNo
                clientRegNo = cl.regNo
            }
        }
    }

    // Invoice meta
    var invoiceNumber by remember { mutableStateOf(existingInvoice?.invoiceNumber ?: "Auto-Increment") }
    var poNumber by remember { mutableStateOf(existingInvoice?.poNumber ?: initialPoNumber) }
    var inviteNumber by remember { mutableStateOf(existingInvoice?.inviteNumber ?: initialInviteNumber) }

    fun getEndOfDay(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
        cal.set(java.util.Calendar.MINUTE, 59)
        cal.set(java.util.Calendar.SECOND, 59)
        cal.set(java.util.Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    var hasDueDate by remember { mutableStateOf(existingInvoice?.hasDueDate ?: true) }
    var pickedCustomDate by remember {
        mutableLongStateOf(
            if (existingInvoice != null && existingInvoice.dueDate > 0) getEndOfDay(existingInvoice.dueDate)
            else getEndOfDay(System.currentTimeMillis() + (profile.defaultPaymentTermsDays.toLong() * 24 * 60 * 60 * 1000))
        )
    }

    val initialMode = remember {
        if (existingInvoice != null) {
            if (!existingInvoice.hasDueDate) {
                "NONE"
            } else {
                val dayCount = ((existingInvoice.dueDate - existingInvoice.issueDate) / (24L * 60 * 60 * 1000)).toInt()
                when (dayCount) {
                    0 -> "RECEIPT"
                    7 -> "NET_7"
                    14 -> "NET_14"
                    30 -> "NET_30"
                    else -> "CUSTOM"
                }
            }
        } else {
            when (profile.defaultPaymentTermsDays) {
                0 -> "RECEIPT"
                7 -> "NET_7"
                14 -> "NET_14"
                30 -> "NET_30"
                else -> "NET_14"
            }
        }
    }

    var selectedDueMode by remember { mutableStateOf(initialMode) }

    var notes by remember { mutableStateOf(existingInvoice?.notes ?: "Thank you for your business!") }
    var paymentInstructions by remember { mutableStateOf(existingInvoice?.paymentInstructions ?: profile.paymentInstructions) }
    var status by remember { mutableStateOf(existingInvoice?.status ?: "Draft") }

    // Line items
    var lineItems by remember {
        mutableStateOf(
            if (existingInvoice != null) LineItemConverter.fromJson(existingInvoice.itemsJson)
            else listOf(
                LineItem(project = "", service = "TD/Vision Mixer", days = 1.0, dailyRate = 4500.0)
            )
        )
    }

    val subtotal = remember(lineItems) { lineItems.sumOf { it.subtotal } }
    val total = remember(lineItems) { lineItems.sumOf { it.total } }

    val currency = profile.currencySymbol.ifBlank { "R" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingInvoice != null) "Edit Invoice ${existingInvoice.invoiceNumber}" else "New Invoice", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("invoice_edit_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (lineItems.isEmpty()) {
                                Toast.makeText(context, "Please add at least one line item", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val now = existingInvoice?.issueDate ?: System.currentTimeMillis()
                            val finalDueDate = if (!hasDueDate) {
                                0L
                            } else {
                                when (selectedDueMode) {
                                    "RECEIPT" -> getEndOfDay(now)
                                    "NET_7" -> getEndOfDay(now + 7L * 24 * 60 * 60 * 1000)
                                    "NET_14" -> getEndOfDay(now + 14L * 24 * 60 * 60 * 1000)
                                    "NET_30" -> getEndOfDay(now + 30L * 24 * 60 * 60 * 1000)
                                    "CUSTOM" -> getEndOfDay(pickedCustomDate)
                                    else -> getEndOfDay(now + 14L * 24 * 60 * 60 * 1000)
                                }
                            }

                            val matchingClient = if (selectedClientId > 0) clients.find { it.id == selectedClientId } else null
                            val finalAddress = clientAddress.trim().ifBlank { matchingClient?.address ?: "" }
                            val finalSuburb = clientSuburb.trim().ifBlank { matchingClient?.suburb ?: "" }
                            val finalCity = clientCity.trim().ifBlank { matchingClient?.city ?: "" }
                            val finalPostalCode = clientPostalCode.trim().ifBlank { matchingClient?.postalCode ?: "" }
                            val finalVatNo = clientVatNo.trim().ifBlank { matchingClient?.vatNo ?: "" }
                            val finalRegNo = clientRegNo.trim().ifBlank { matchingClient?.regNo ?: "" }
                            val finalCityStateZip = clientCityStateZip.trim().ifBlank {
                                matchingClient?.fullAddress?.ifBlank { matchingClient.cityStateZip } ?: ""
                            }
                            val finalEmail2 = clientEmail2.trim().ifBlank { matchingClient?.invoiceEmail2 ?: "" }

                            val invoiceToSave = Invoice(
                                id = existingInvoice?.id ?: 0L,
                                invoiceNumber = existingInvoice?.invoiceNumber ?: "",
                                invoiceSequence = existingInvoice?.invoiceSequence ?: 0,
                                poNumber = poNumber.trim(),
                                inviteNumber = inviteNumber.trim(),
                                quoteId = existingInvoice?.quoteId,
                                clientId = selectedClientId,
                                clientName = clientName.trim().ifBlank { matchingClient?.name ?: "" },
                                clientCompany = clientCompany.trim().ifBlank { matchingClient?.company ?: "" },
                                clientEmail = clientEmail.trim().ifBlank { matchingClient?.effectiveInvoiceEmail ?: "" },
                                clientEmail2 = finalEmail2,
                                clientPhone = clientPhone.trim().ifBlank { matchingClient?.phone ?: "" },
                                clientAddress = finalAddress,
                                clientSuburb = finalSuburb,
                                clientCity = finalCity,
                                clientPostalCode = finalPostalCode,
                                clientCityStateZip = finalCityStateZip,
                                clientVatNo = finalVatNo,
                                clientRegNo = finalRegNo,
                                issueDate = now,
                                hasDueDate = hasDueDate,
                                dueDate = finalDueDate,
                                itemsJson = LineItemConverter.toJson(lineItems),
                                subtotal = subtotal,
                                total = total,
                                amountPaid = existingInvoice?.amountPaid ?: 0.0,
                                notes = notes.trim(),
                                paymentInstructions = paymentInstructions.trim(),
                                status = status,
                                paidDate = existingInvoice?.paidDate
                            )
                            onSaveInvoice(invoiceToSave)
                        },
                        modifier = Modifier.testTag("save_invoice_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Save")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("invoice_edit_scroll_content"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Client Selection Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Client Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = { showClientPicker = true },
                            modifier = Modifier.testTag("select_client_button")
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (clientName.isBlank()) "Select Client" else "Change Client")
                        }
                    }

                    if (clientName.isBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { showClientPicker = true }
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tap 'Select Client' to auto-populate from saved clients, or enter details below",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Auto-Populated from Client:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(clientName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                if (clientCompany.isNotBlank()) Text(clientCompany, fontSize = 13.sp)
                                Text(clientEmail, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (clientEmail2.isNotBlank()) {
                                    Text("2nd Recipient: $clientEmail2", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Client Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("invoice_client_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = clientEmail,
                        onValueChange = { clientEmail = it },
                        label = { Text("Client Email (Primary)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("invoice_client_email_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = clientEmail2,
                        onValueChange = { clientEmail2 = it },
                        label = { Text("Secondary Invoice Email (Send to 2nd person)") },
                        placeholder = { Text("e.g. accounts2@company.co.za or manager@company.co.za") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("invoice_client_email2_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = clientCompany,
                            onValueChange = { clientCompany = it },
                            label = { Text("Company (Optional)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("invoice_client_company_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = clientPhone,
                            onValueChange = { clientPhone = it },
                            label = { Text("Phone") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("invoice_client_phone_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = clientAddress,
                        onValueChange = { clientAddress = it },
                        label = { Text("Billing Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = clientCityStateZip,
                        onValueChange = { clientCityStateZip = it },
                        label = { Text("City, Province, Postal Code") },
                        placeholder = { Text("Cape Town, 8001") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Reference & Terms Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "References & Payment Terms",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = poNumber,
                            onValueChange = { poNumber = it },
                            label = { Text("PO Number") },
                            placeholder = { Text("e.g. PO-8921") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("invoice_po_number_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = inviteNumber,
                            onValueChange = { inviteNumber = it },
                            label = { Text("Job Invite # (Optional)") },
                            placeholder = { Text("e.g. INVITE-101") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("invoice_invite_number_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Payment Due In:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = hasDueDate && selectedDueMode == "RECEIPT",
                            onClick = {
                                selectedDueMode = "RECEIPT"
                                hasDueDate = true
                            },
                            label = { Text("Due on Receipt", fontSize = 12.sp) },
                            modifier = Modifier.testTag("terms_chip_0")
                        )
                        FilterChip(
                            selected = hasDueDate && selectedDueMode == "NET_7",
                            onClick = {
                                selectedDueMode = "NET_7"
                                hasDueDate = true
                            },
                            label = { Text("Net 7", fontSize = 12.sp) },
                            modifier = Modifier.testTag("terms_chip_7")
                        )
                        FilterChip(
                            selected = hasDueDate && selectedDueMode == "NET_14",
                            onClick = {
                                selectedDueMode = "NET_14"
                                hasDueDate = true
                            },
                            label = { Text("Net 14", fontSize = 12.sp) },
                            modifier = Modifier.testTag("terms_chip_14")
                        )
                        FilterChip(
                            selected = hasDueDate && selectedDueMode == "NET_30",
                            onClick = {
                                selectedDueMode = "NET_30"
                                hasDueDate = true
                            },
                            label = { Text("Net 30", fontSize = 12.sp) },
                            modifier = Modifier.testTag("terms_chip_30")
                        )
                        FilterChip(
                            selected = hasDueDate && selectedDueMode == "CUSTOM",
                            onClick = {
                                val cal = java.util.Calendar.getInstance().apply { timeInMillis = pickedCustomDate }
                                android.app.DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val chosenCal = java.util.Calendar.getInstance().apply {
                                            set(year, month, dayOfMonth, 23, 59, 59)
                                            set(java.util.Calendar.MILLISECOND, 999)
                                        }
                                        pickedCustomDate = chosenCal.timeInMillis
                                        selectedDueMode = "CUSTOM"
                                        hasDueDate = true
                                    },
                                    cal.get(java.util.Calendar.YEAR),
                                    cal.get(java.util.Calendar.MONTH),
                                    cal.get(java.util.Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            label = {
                                Text(
                                    if (hasDueDate && selectedDueMode == "CUSTOM") "Due ${Formatters.formatDate(pickedCustomDate)}" else "Pick a date",
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            modifier = Modifier.testTag("terms_chip_custom")
                        )
                        FilterChip(
                            selected = !hasDueDate,
                            onClick = {
                                selectedDueMode = "NONE"
                                hasDueDate = false
                            },
                            label = { Text("No due date", fontSize = 12.sp) },
                            modifier = Modifier.testTag("terms_chip_none")
                        )
                    }
                }
            }

            // Line Items Section (Without Tax)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    LineItemEditor(
                        items = lineItems,
                        currencySymbol = currency,
                        onItemsChange = { lineItems = it }
                    )

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Invoice Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            Formatters.formatCurrency(total, currency),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Notes & Bank details
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Notes & EFT Instructions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes for Client") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = paymentInstructions,
                        onValueChange = { paymentInstructions = it },
                        label = { Text("EFT / Banking Instructions") },
                        placeholder = { Text("Bank name, Account number, Branch code...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    if (showClientPicker) {
        ClientPickerBottomSheet(
            clients = clients,
            selectedClientId = selectedClientId,
            onClientSelected = { client ->
                selectedClientId = client.id
                clientName = client.name
                clientCompany = client.company
                clientEmail = client.effectiveInvoiceEmail
                clientEmail2 = client.invoiceEmail2
                clientPhone = client.phone
                clientAddress = client.address
                clientSuburb = client.suburb
                clientCity = client.city
                clientPostalCode = client.postalCode
                clientCityStateZip = client.fullAddress.ifBlank { client.cityStateZip }
                clientVatNo = client.vatNo
                clientRegNo = client.regNo
                showClientPicker = false
            },
            onAddNewClient = {
                showClientPicker = false
                onAddNewClient()
            },
            onDismiss = { showClientPicker = false }
        )
    }
}
