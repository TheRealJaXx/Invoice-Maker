package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Invoice
import com.example.data.model.LineItem
import com.example.ui.viewmodel.FreelanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceEditScreen(
    viewModel: FreelanceViewModel,
    invoiceId: Long?,
    onNavigateBack: () -> Unit
) {
    val invoices by viewModel.invoices.collectAsState(initial = emptyList())
    val clients by viewModel.clients.collectAsState(initial = emptyList())
    val invoice = invoices.find { it.id == invoiceId }

    var invoiceNumber by remember { mutableStateOf("") }
    var selectedClientId by remember { mutableStateOf<Long?>(null) }
    var clientName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var lineItems by remember { mutableStateOf(listOf<LineItem>()) }

    var showClientPicker by remember { mutableStateOf(false) }

    LaunchedEffect(invoice) {
        invoice?.let {
            invoiceNumber = it.invoiceNumber
            selectedClientId = it.clientId
            clientName = it.clientName
            date = it.date
            dueDate = it.dueDate
            lineItems = it.lineItems
        }
    }

    // Automatically set next invoice number when client is selected (if creating a new invoice)
    LaunchedEffect(selectedClientId) {
        if (invoiceId == null || invoiceId == 0L) {
            selectedClientId?.let { clientId ->
                val client = clients.find { it.id == clientId }
                if (client != null && invoiceNumber.isBlank()) {
                    invoiceNumber = (client.lastInvoiceNumber + 1).toString()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (invoiceId == null || invoiceId == 0L) "Create Invoice" else "Edit Invoice") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (invoiceNumber.isNotBlank()) {
                                val total = lineItems.sumOf { it.total }
                                val newInvoice = Invoice(
                                    id = invoiceId ?: 0L,
                                    invoiceNumber = invoiceNumber,
                                    clientId = selectedClientId ?: 0L,
                                    clientName = clientName,
                                    date = date,
                                    dueDate = dueDate,
                                    lineItems = lineItems,
                                    totalAmount = total
                                )
                                if (invoiceId == null || invoiceId == 0L) {
                                    viewModel.insertInvoice(newInvoice)
                                    // Optionally update the client's lastInvoiceNumber here if needed
                                    selectedClientId?.let { cId ->
                                        clients.find { it.id == cId }?.let { client ->
                                            val invNumInt = invoiceNumber.toIntOrNull() ?: client.lastInvoiceNumber
                                            if (invNumInt > client.lastInvoiceNumber) {
                                                viewModel.updateClient(client.copy(lastInvoiceNumber = invNumInt))
                                            }
                                        }
                                    }
                                } else {
                                    viewModel.updateInvoice(newInvoice)
                                }
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save Invoice")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = invoiceNumber,
                onValueChange = { invoiceNumber = it },
                label = { Text("Invoice Number *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = { showClientPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (clientName.isBlank()) "Select Client" else "Client: $clientName")
            }

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = { Text("Due Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Line Items", style = MaterialTheme.typography.titleMedium)

            // Simple summary of items or add items UI if present
            lineItems.forEachIndexed { index, item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Item: ${item.description}")
                        Text("Qty: ${item.quantity} | Unit Price: ${item.unitPrice}")
                    }
                }
            }
        }
    }

    if (showClientPicker) {
        AlertDialog(
            onDismissRequest = { showClientPicker = false },
            title = { Text("Select Client") },
            text = {
                Column {
                    clients.forEach { client ->
                        TextButton(
                            onClick = {
                                selectedClientId = client.id
                                clientName = client.name
                                showClientPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(client.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showClientPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
