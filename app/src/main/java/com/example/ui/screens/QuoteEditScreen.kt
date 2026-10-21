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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.data.model.LineItem
import com.example.data.model.LineItemConverter
import com.example.data.model.Quote
import com.example.ui.components.ClientPickerBottomSheet
import com.example.ui.components.LineItemEditor
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteEditScreen(
    existingQuote: Quote?,
    clients: List<Client>,
    profile: FreelancerProfile,
    initialClientId: Long?,
    initialInviteNumber: String,
    onBack: () -> Unit,
    onSaveQuote: (Quote) -> Unit,
    onAddNewClient: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var showClientPicker by remember { mutableStateOf(false) }

    var selectedClientId by remember { mutableLongStateOf(existingQuote?.clientId ?: initialClientId ?: 0L) }
    var clientName by remember { mutableStateOf(existingQuote?.clientName ?: "") }
    var clientCompany by remember { mutableStateOf(existingQuote?.clientCompany ?: "") }
    var clientEmail by remember { mutableStateOf(existingQuote?.clientEmail ?: "") }
    var clientEmail2 by remember { mutableStateOf(existingQuote?.clientEmail2 ?: "") }
    var clientPhone by remember { mutableStateOf(existingQuote?.clientPhone ?: "") }
    var clientAddress by remember { mutableStateOf(existingQuote?.clientAddress ?: "") }
    var clientSuburb by remember { mutableStateOf(existingQuote?.clientSuburb ?: "") }
    var clientCity by remember { mutableStateOf(existingQuote?.clientCity ?: "") }
    var clientPostalCode by remember { mutableStateOf(existingQuote?.clientPostalCode ?: "") }
    var clientCityStateZip by remember { mutableStateOf(existingQuote?.clientCityStateZip ?: "") }
    var clientVatNo by remember { mutableStateOf(existingQuote?.clientVatNo ?: "") }
    var clientRegNo by remember { mutableStateOf(existingQuote?.clientRegNo ?: "") }

    LaunchedEffect(initialClientId, clients) {
        if (existingQuote == null && initialClientId != null && initialClientId > 0) {
            clients.find { it.id == initialClientId }?.let { cl ->
                selectedClientId = cl.id
                clientName = cl.name
                clientCompany = cl.company
                clientEmail = cl.effectiveQuoteEmail
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

    var inviteNumber by remember { mutableStateOf(existingQuote?.inviteNumber ?: initialInviteNumber) }
    var terms by remember { mutableStateOf(existingQuote?.terms ?: "50% deposit upon kickoff, balance net 14 upon delivery.") }
    var notes by remember { mutableStateOf(existingQuote?.notes ?: "Quote is valid for 30 days.") }

    var lineItems by remember {
        mutableStateOf(
            if (existingQuote != null) LineItemConverter.fromJson(existingQuote.itemsJson)
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
                title = { Text(if (existingQuote != null) "Edit Quote ${existingQuote.quoteNumber}" else "New Price Quote", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("quote_edit_back_button")) {
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

                            val now = existingQuote?.issueDate ?: System.currentTimeMillis()
                            val expiry = now + (30L * 24 * 60 * 60 * 1000)

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

                            val quoteToSave = Quote(
                                id = existingQuote?.id ?: 0L,
                                quoteNumber = existingQuote?.quoteNumber ?: "",
                                inviteNumber = inviteNumber.trim(),
                                poNumber = existingQuote?.poNumber ?: "",
                                clientId = selectedClientId,
                                clientName = clientName.trim().ifBlank { matchingClient?.name ?: "" },
                                clientCompany = clientCompany.trim().ifBlank { matchingClient?.company ?: "" },
                                clientEmail = clientEmail.trim().ifBlank { matchingClient?.effectiveQuoteEmail ?: "" },
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
                                expiryDate = expiry,
                                itemsJson = LineItemConverter.toJson(lineItems),
                                subtotal = subtotal,
                                total = total,
                                notes = notes.trim(),
                                terms = terms.trim(),
                                status = existingQuote?.status ?: "Pending",
                                convertedInvoiceId = existingQuote?.convertedInvoiceId,
                                convertedInvoiceNumber = existingQuote?.convertedInvoiceNumber
                            )
                            onSaveQuote(quoteToSave)
                        },
                        modifier = Modifier.testTag("save_quote_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Save Quote")
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
                .testTag("quote_edit_scroll_content"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Job Invite Reference Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Job Invite Reference (Optional)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inviteNumber,
                        onValueChange = { inviteNumber = it },
                        label = { Text("Job Invite Number (Optional)") },
                        placeholder = { Text("e.g. INVITE-8491, LEAD-2026-X") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quote_invite_number_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Client Info Card
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
                            text = "Client Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = { showClientPicker = true },
                            modifier = Modifier.testTag("quote_select_client_button")
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
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Select an existing client to auto-fill or enter manually below",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Client Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quote_client_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = clientEmail,
                        onValueChange = { clientEmail = it },
                        label = { Text("Client Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quote_client_email_input"),
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
                            label = { Text("Company") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = clientPhone,
                            onValueChange = { clientPhone = it },
                            label = { Text("Phone") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = clientAddress,
                        onValueChange = { clientAddress = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = clientCityStateZip,
                        onValueChange = { clientCityStateZip = it },
                        label = { Text("City, Province, Postal Code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Line Items
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
                        Text("Quote Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            Formatters.formatCurrency(total, currency),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Terms & Notes
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
                        text = "Terms & Validity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = terms,
                        onValueChange = { terms = it },
                        label = { Text("Payment & Delivery Terms") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Expiry") },
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
                clientEmail = client.effectiveQuoteEmail
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
