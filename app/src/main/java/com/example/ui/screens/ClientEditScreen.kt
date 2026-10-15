package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Client

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientEditScreen(
    client: Client?,
    onBack: () -> Unit,
    onSaveClient: (Client) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    // Bill To fields
    var name by remember { mutableStateOf(client?.name ?: "") }
    var address by remember { mutableStateOf(client?.address ?: "") }
    var suburb by remember { mutableStateOf(client?.suburb ?: "") }
    var city by remember { mutableStateOf(client?.city ?: "") }
    var postalCode by remember { mutableStateOf(client?.postalCode ?: "") }
    var vatNo by remember { mutableStateOf(client?.vatNo ?: "") }
    var regNo by remember { mutableStateOf(client?.regNo ?: "") }

    // Contact fields
    var email by remember { mutableStateOf(client?.email ?: "") }
    var phone by remember { mutableStateOf(client?.phone ?: "") }

    // Additional notes
    var notes by remember { mutableStateOf(client?.notes ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (client != null) "Edit Client" else "Add Client", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("client_edit_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            // No client details are mandatory requirements. If they don't have that info, they just don't!
                            val finalName = name.trim().ifBlank {
                                email.trim().ifBlank { "Client" }
                            }
                            val clientToSave = Client(
                                id = client?.id ?: 0L,
                                name = finalName,
                                company = client?.company ?: "",
                                address = address.trim(),
                                suburb = suburb.trim(),
                                city = city.trim(),
                                postalCode = postalCode.trim(),
                                cityStateZip = listOf(suburb.trim(), city.trim(), postalCode.trim()).filter { it.isNotBlank() }.joinToString(", "),
                                vatNo = vatNo.trim(),
                                regNo = regNo.trim(),
                                email = email.trim(),
                                phone = phone.trim(),
                                notes = notes.trim(),
                                createdAt = client?.createdAt ?: System.currentTimeMillis()
                            )
                            onSaveClient(clientToSave)
                        },
                        modifier = Modifier.testTag("save_client_button")
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
                .testTag("client_edit_content"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bill To Card
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Bill To:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name / Company") },
                        placeholder = { Text("e.g. Settlers Media Pty Ltd") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("client_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address (Street)") },
                        placeholder = { Text("e.g. 93 1st Avenue") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("client_address_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = suburb,
                            onValueChange = { suburb = it },
                            label = { Text("Suburb") },
                            placeholder = { Text("e.g. Fontainebleau") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("client_suburb_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            placeholder = { Text("e.g. Randburg") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("client_city_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = postalCode,
                        onValueChange = { postalCode = it },
                        label = { Text("Postal Code") },
                        placeholder = { Text("e.g. 2194") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("client_postal_code_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = vatNo,
                            onValueChange = { vatNo = it },
                            label = { Text("VAT No") },
                            placeholder = { Text("e.g. 4460266721") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("client_vat_no_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = regNo,
                            onValueChange = { regNo = it },
                            label = { Text("Reg No") },
                            placeholder = { Text("e.g. 2014/082117/07") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("client_reg_no_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Contact Details Card
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ContactMail, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Contact:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("e.g. thresini@settlersmedia.co.za") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("client_email_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("e.g. (072) 052 2463") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("client_phone_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Internal Notes Card
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Internal Notes:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (e.g. billing contact, payment terms)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("client_notes_input"),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}
