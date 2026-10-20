package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Client
import com.example.data.model.FreelancerProfile
import com.example.data.model.Invoice
import com.example.data.model.Quote
import com.example.ui.components.StatusBadge
import com.example.ui.viewmodel.Screen
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    client: Client,
    invoices: List<Invoice>,
    quotes: List<Quote>,
    profile: FreelancerProfile,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val clientInvoices = remember(invoices, client.id) {
        invoices.filter { it.clientId == client.id }
    }
    val clientQuotes = remember(quotes, client.id) {
        quotes.filter { it.clientId == client.id }
    }

    val totalInvoiced = remember(clientInvoices) { clientInvoices.sumOf { it.total } }
    val totalPaid = remember(clientInvoices) { clientInvoices.filter { it.status == "Paid" }.sumOf { it.total } }
    val balanceDue = totalInvoiced - totalPaid

    val currency = profile.currencySymbol

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(client.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("client_detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit, modifier = Modifier.testTag("client_detail_edit_button")) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.testTag("client_detail_delete_button")) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
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
                .testTag("client_detail_content"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = client.name.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column {
                            Text(client.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            if (client.company.isNotBlank()) {
                                Text(client.company, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(Modifier.height(14.dp))

                    // Contact emails
                    if (client.email.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${client.email}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                }
                                .padding(vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Primary Email", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(client.email, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    if (client.quoteEmail.isNotBlank() && client.quoteEmail != client.email) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${client.quoteEmail}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                }
                                .padding(vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Quotes Email", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                Text(client.quoteEmail, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    if (client.invoiceEmail.isNotBlank() && client.invoiceEmail != client.email) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${client.invoiceEmail}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                }
                                .padding(vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Invoices Email #1", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(client.invoiceEmail, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    if (client.invoiceEmail2.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${client.invoiceEmail2}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                }
                                .padding(vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Invoices Email #2 (2nd Recipient)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(client.invoiceEmail2, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    if (client.quoteEmail.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${client.quoteEmail.replace(" ", "")}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Quotes: ${client.quoteEmail}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    if (client.invoiceEmail.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${client.invoiceEmail.replace(" ", "")}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Invoices: ${client.invoiceEmail}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    if (client.phone.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${client.phone}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(client.phone, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    val addrDisplay = client.fullAddress
                    if (addrDisplay.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = addrDisplay,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (client.vatNo.isNotBlank() || client.regNo.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (client.vatNo.isNotBlank()) {
                                Column {
                                    Text("VAT Number", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(client.vatNo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            if (client.regNo.isNotBlank()) {
                                Column {
                                    Text("Company Reg No", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(client.regNo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }


                    if (client.notes.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(10.dp)
                        ) {
                            Text("Note: ${client.notes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Quick Create for this Client
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onNavigate(Screen.InvoiceEdit(initialClientId = client.id)) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("client_create_invoice_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("New Invoice")
                }

                OutlinedButton(
                    onClick = { onNavigate(Screen.QuoteEdit(initialClientId = client.id)) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("client_create_quote_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("New Quote")
                }
            }

            // Financial Summary for Client
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Financial Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Billed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(Formatters.formatCurrency(totalInvoiced, currency), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Column {
                            Text("Total Paid", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(Formatters.formatCurrency(totalPaid, currency), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Column {
                            Text("Balance Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(Formatters.formatCurrency(balanceDue, currency), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            // Invoices for this client
            Text("Invoices (${clientInvoices.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (clientInvoices.isEmpty()) {
                Text("No invoices yet for this client.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    clientInvoices.forEach { invoice ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate(Screen.InvoiceDetail(invoice.id)) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(Modifier.width(8.dp))
                                        StatusBadge(status = invoice.status)
                                    }
                                    Text("Due: ${Formatters.formatDate(invoice.dueDate)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(Formatters.formatCurrency(invoice.total, currency), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }

            // Quotes for this client
            Text("Quotes (${clientQuotes.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (clientQuotes.isEmpty()) {
                Text("No quotes yet for this client.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    clientQuotes.forEach { quote ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate(Screen.QuoteDetail(quote.id)) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(quote.quoteNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(Modifier.width(8.dp))
                                        StatusBadge(status = quote.status)
                                    }
                                    if (quote.inviteNumber.isNotBlank()) {
                                        Text("Invite: ${quote.inviteNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Text(Formatters.formatCurrency(quote.total, currency), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Client?") },
            text = { Text("Are you sure you want to delete ${client.name}? Existing invoices and quotes will be retained.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
