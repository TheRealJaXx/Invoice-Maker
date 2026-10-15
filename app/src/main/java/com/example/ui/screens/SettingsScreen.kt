package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Client
import com.example.data.model.FreelancerProfile
import com.example.data.model.Invoice
import com.example.data.model.Quote
import com.example.util.DataBackupHelper
import com.example.util.FullBackupData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    profile: FreelancerProfile,
    clients: List<Client> = emptyList(),
    invoices: List<Invoice> = emptyList(),
    quotes: List<Quote> = emptyList(),
    onSaveProfile: (FreelancerProfile) -> Unit,
    onRestoreBackup: (FullBackupData) -> Unit = {},
    onImportClients: (List<Client>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var businessName by remember(profile) { mutableStateOf(profile.businessName) }
    var ownerName by remember(profile) { mutableStateOf(profile.ownerName) }
    var email by remember(profile) { mutableStateOf(profile.email) }
    var phone by remember(profile) { mutableStateOf(profile.phone) }
    var address by remember(profile) { mutableStateOf(profile.address) }
    var cityStateZip by remember(profile) { mutableStateOf(profile.cityStateZip) }
    var currencySymbol by remember(profile) { mutableStateOf(profile.currencySymbol.ifBlank { "R" }) }
    var invoicePrefix by remember(profile) { mutableStateOf(profile.invoicePrefix) }
    var quotePrefix by remember(profile) { mutableStateOf(profile.quotePrefix) }
    var paymentInstructions by remember(profile) { mutableStateOf(profile.paymentInstructions) }
    var defaultTermsDays by remember(profile) { mutableIntStateOf(profile.defaultPaymentTermsDays) }

    val currencies = listOf("R", "$", "€", "£", "AU$", "CA$")

    // Full Backup Export Launcher (Save to Google Drive / Storage)
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        val json = DataBackupHelper.exportFullBackupToJson(clients, invoices, quotes, profile)
                        DataBackupHelper.writeToOutputStream(out, json)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Full backup saved to Google Drive / Storage successfully!", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Full Backup Restore Launcher (Select from Google Drive / Storage)
    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val content = context.contentResolver.openInputStream(uri)?.use {
                        DataBackupHelper.readFromInputStream(it)
                    } ?: ""

                    if (content.isNotBlank()) {
                        val backup = DataBackupHelper.parseFullBackupFromJson(content)
                        withContext(Dispatchers.Main) {
                            onRestoreBackup(backup)
                            Toast.makeText(context, "Restored ${backup.clients.size} clients, ${backup.invoices.size} invoices, ${backup.quotes.size} quotes!", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("settings_content"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Freelancer & Business Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Business Profile Card
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
                        Text("Freelancer / Studio Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Business / Trading Name") },
                        placeholder = { Text("Apex Creative Studio") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_business_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Your Name (Freelancer)") },
                        placeholder = { Text("Jacques Du Plessis") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_owner_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Billing / Contact Email") },
                        placeholder = { Text("billing@freelancer.co.za") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_email_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("+27 82 123 4567") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Street Address") },
                        placeholder = { Text("12 Kloof Street, Gardens") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = cityStateZip,
                        onValueChange = { cityStateZip = it },
                        label = { Text("City, Postal Code") },
                        placeholder = { Text("Cape Town, 8001") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Invoicing & Currency Preferences
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
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Currency & Numbering", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(14.dp))

                    Text("Billing Currency:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currencies.forEach { sym ->
                            val isSelected = currencySymbol == sym
                            FilterChip(
                                selected = isSelected,
                                onClick = { currencySymbol = sym },
                                label = { Text(sym, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = invoicePrefix,
                            onValueChange = { invoicePrefix = it },
                            label = { Text("Invoice Prefix") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = quotePrefix,
                            onValueChange = { quotePrefix = it },
                            label = { Text("Quote Prefix") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Payment Instructions Card
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
                        Icon(Icons.Default.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("EFT / Banking Payment Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = paymentInstructions,
                        onValueChange = { paymentInstructions = it },
                        label = { Text("EFT Details (Bank, Account #, Branch Code)") },
                        placeholder = { Text("Bank: FNB\nAccount: 62890012345\nBranch Code: 250655\nRef: [Invoice #]") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Google Drive Cloud Backup & Cross-Device Transfer Card
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
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Google Drive & Cross-Device Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Save and sync all your client details, invoices, and quotes with Google Drive so you never lose them when switching phones or devices.",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = {
                            exportBackupLauncher.launch("FreelanceBooks_FullBackup_${System.currentTimeMillis()}.json")
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save Full Backup to Google Drive / Files")
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            val json = DataBackupHelper.exportFullBackupToJson(clients, invoices, quotes, profile)
                            val file = DataBackupHelper.createTemporaryFile(context, json, "FreelanceBooks_FullBackup.json")
                            DataBackupHelper.shareBackupFile(context, file, "application/json", "Freelance Books Full Backup")
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Send Backup to Google Drive (via Share Sheet)")
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            restoreBackupLauncher.launch("application/json")
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Restore Backup from Google Drive / Files")
                    }
                }
            }

            Button(
                onClick = {
                    val updated = profile.copy(
                        businessName = businessName.trim(),
                        ownerName = ownerName.trim(),
                        email = email.trim(),
                        phone = phone.trim(),
                        address = address.trim(),
                        cityStateZip = cityStateZip.trim(),
                        currencySymbol = currencySymbol.trim().ifBlank { "R" },
                        invoicePrefix = invoicePrefix.trim().ifBlank { "INV-" },
                        quotePrefix = quotePrefix.trim().ifBlank { "EST-" },
                        paymentInstructions = paymentInstructions.trim(),
                        defaultPaymentTermsDays = defaultTermsDays
                    )
                    onSaveProfile(updated)
                    Toast.makeText(context, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_settings_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Profile & Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
