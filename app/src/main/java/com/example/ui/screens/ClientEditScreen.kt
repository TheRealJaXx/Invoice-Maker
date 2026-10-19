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
import com.example.data.model.Client
import com.example.ui.viewmodel.FreelanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientEditScreen(
    viewModel: FreelanceViewModel,
    clientId: Long?,
    onNavigateBack: () -> Unit
) {
    val clients by viewModel.clients.collectAsState(initial = emptyList())
    val client = clients.find { it.id == clientId }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var quoteEmail by remember { mutableStateOf("") }
    var invoiceEmail by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var lastInvoiceNumberStr by remember { mutableStateOf("0") }

    LaunchedEffect(clientId, client) {
        if (clientId == null || clientId == 0L) {
            // Clear fields explicitly when creating a new client
            name = ""
            email = ""
            quoteEmail = ""
            invoiceEmail = ""
            phone = ""
            address = ""
            lastInvoiceNumberStr = "0"
        } else {
            client?.let {
                name = it.name
                email = it.email
                quoteEmail = it.quoteEmail
                invoiceEmail = it.invoiceEmail
                phone = it.phone
                address = it.address
                lastInvoiceNumberStr = it.lastInvoiceNumber.toString()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (clientId == null || clientId == 0L) "Add Client" else "Edit Client") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                val lastInvNum = lastInvoiceNumberStr.toIntOrNull() ?: 0
                                if (clientId == null || clientId == 0L) {
                                    viewModel.insertClient(
                                        Client(
                                            name = name,
                                            email = email,
                                            quoteEmail = quoteEmail,
                                            invoiceEmail = invoiceEmail,
                                            phone = phone,
                                            address = address,
                                            lastInvoiceNumber = lastInvNum
                                        )
                                    )
                                } else {
                                    viewModel.updateClient(
                                        Client(
                                            id = clientId,
                                            name = name,
                                            email = email,
                                            quoteEmail = quoteEmail,
                                            invoiceEmail = invoiceEmail,
                                            phone = phone,
                                            address = address,
                                            lastInvoiceNumber = lastInvNum
                                        )
                                    )
                                }
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save Client")
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
                value = name,
                onValueChange = { name = it },
                label = { Text("Client Name *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("General Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value = quoteEmail,
                onValueChange = { quoteEmail = it },
                label = { Text("Quote Email (Optional - separate person)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value = invoiceEmail,
                onValueChange = { invoiceEmail = it },
                label = { Text("Invoice Email (Optional - separate person)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = lastInvoiceNumberStr,
                onValueChange = { lastInvoiceNumberStr = it },
                label = { Text("Last Invoice Number Sent") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text("Next invoice generated for this client will be this number + 1.") }
            )
        }
    }
}
