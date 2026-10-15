package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.JobInviteDialog
import com.example.ui.components.PoNumberDialog
import com.example.ui.screens.ClientDetailScreen
import com.example.ui.screens.ClientEditScreen
import com.example.ui.screens.ClientsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InvoiceDetailScreen
import com.example.ui.screens.InvoiceEditScreen
import com.example.ui.screens.InvoicesScreen
import com.example.ui.screens.QuoteDetailScreen
import com.example.ui.screens.QuoteEditScreen
import com.example.ui.screens.QuotesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FreelanceViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private val viewModel: FreelanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FreelanceApp(viewModel)
            }
        }
    }
}

@Composable
fun FreelanceApp(viewModel: FreelanceViewModel) {
    val context = LocalContext.current

    val screenStack by viewModel.screenStack.collectAsStateWithLifecycle()
    val currentScreen = screenStack.lastOrNull() ?: Screen.Dashboard

    val clients by viewModel.clients.collectAsStateWithLifecycle()
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    val quotes by viewModel.quotes.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    val showJobInviteDialog by viewModel.showJobInviteDialog.collectAsStateWithLifecycle()
    val quoteToConvert by viewModel.quoteToConvert.collectAsStateWithLifecycle()

    val isRootScreen = currentScreen is Screen.Dashboard ||
            currentScreen is Screen.Invoices ||
            currentScreen is Screen.Quotes ||
            currentScreen is Screen.Clients ||
            currentScreen is Screen.Settings

    Scaffold(
        bottomBar = {
            if (isRootScreen) {
                NavigationBar(
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = currentScreen is Screen.Dashboard,
                        onClick = { viewModel.switchTab(Screen.Dashboard) },
                        icon = {
                            Icon(
                                if (currentScreen is Screen.Dashboard) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                                contentDescription = "Dashboard"
                            )
                        },
                        label = { Text("Dashboard", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.Invoices,
                        onClick = { viewModel.switchTab(Screen.Invoices) },
                        icon = {
                            Icon(
                                if (currentScreen is Screen.Invoices) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                                contentDescription = "Invoices"
                            )
                        },
                        label = { Text("Invoices", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_invoices")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.Quotes,
                        onClick = { viewModel.switchTab(Screen.Quotes) },
                        icon = {
                            Icon(
                                if (currentScreen is Screen.Quotes) Icons.Filled.Description else Icons.Outlined.Description,
                                contentDescription = "Quotes"
                            )
                        },
                        label = { Text("Quotes", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_quotes")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.Clients,
                        onClick = { viewModel.switchTab(Screen.Clients) },
                        icon = {
                            Icon(
                                if (currentScreen is Screen.Clients) Icons.Filled.People else Icons.Outlined.People,
                                contentDescription = "Clients"
                            )
                        },
                        label = { Text("Clients", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_clients")
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.Settings,
                        onClick = { viewModel.switchTab(Screen.Settings) },
                        icon = {
                            Icon(
                                if (currentScreen is Screen.Settings) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_settings")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentScreen) {
                is Screen.Dashboard -> {
                    DashboardScreen(
                        stats = stats,
                        invoices = invoices,
                        quotes = quotes,
                        clients = clients,
                        profile = profile,
                        onNavigate = { viewModel.navigateTo(it) },
                        onOpenJobInvite = { viewModel.openJobInviteDialog() },
                        onConvertQuote = { viewModel.openConvertQuoteDialog(it) }
                    )
                }

                is Screen.Invoices -> {
                    InvoicesScreen(
                        invoices = invoices,
                        currencySymbol = profile.currencySymbol,
                        onNavigate = { viewModel.navigateTo(it) },
                        onMarkAsPaid = { invoice ->
                            viewModel.updateInvoiceStatus(invoice.id, "Paid")
                            Toast.makeText(context, "${invoice.invoiceNumber} marked as Paid!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                is Screen.InvoiceDetail -> {
                    val invoice = invoices.find { it.id == currentScreen.invoiceId }
                    if (invoice != null) {
                        InvoiceDetailScreen(
                            invoice = invoice,
                            profile = profile,
                            onBack = { viewModel.navigateBack() },
                            onEdit = { viewModel.navigateTo(Screen.InvoiceEdit(invoiceId = invoice.id)) },
                            onDelete = {
                                viewModel.deleteInvoice(invoice) {
                                    viewModel.navigateBack()
                                    Toast.makeText(context, "Invoice deleted", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onUpdateStatus = { newStatus ->
                                viewModel.updateInvoiceStatus(invoice.id, newStatus)
                            }
                        )
                    } else {
                        viewModel.navigateBack()
                    }
                }

                is Screen.InvoiceEdit -> {
                    val existingInvoice = currentScreen.invoiceId?.let { id -> invoices.find { it.id == id } }
                    InvoiceEditScreen(
                        existingInvoice = existingInvoice,
                        clients = clients,
                        profile = profile,
                        initialClientId = currentScreen.initialClientId,
                        initialInviteNumber = currentScreen.initialInviteNumber,
                        initialPoNumber = currentScreen.initialPoNumber,
                        onBack = { viewModel.navigateBack() },
                        onSaveInvoice = { invoiceToSave ->
                            viewModel.saveInvoice(invoiceToSave) { savedId ->
                                viewModel.navigateBack()
                                viewModel.navigateTo(Screen.InvoiceDetail(savedId))
                                Toast.makeText(context, "Invoice saved successfully!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onAddNewClient = { viewModel.navigateTo(Screen.ClientEdit()) }
                    )
                }

                is Screen.Quotes -> {
                    QuotesScreen(
                        quotes = quotes,
                        currencySymbol = profile.currencySymbol,
                        onNavigate = { viewModel.navigateTo(it) },
                        onConvertQuote = { quote -> viewModel.openConvertQuoteDialog(quote) }
                    )
                }

                is Screen.QuoteDetail -> {
                    val quote = quotes.find { it.id == currentScreen.quoteId }
                    if (quote != null) {
                        QuoteDetailScreen(
                            quote = quote,
                            profile = profile,
                            onBack = { viewModel.navigateBack() },
                            onEdit = { viewModel.navigateTo(Screen.QuoteEdit(quoteId = quote.id)) },
                            onDelete = {
                                viewModel.deleteQuote(quote) {
                                    viewModel.navigateBack()
                                    Toast.makeText(context, "Quote deleted", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onConvertQuote = { q -> viewModel.openConvertQuoteDialog(q) },
                            onViewConvertedInvoice = { invId ->
                                viewModel.navigateTo(Screen.InvoiceDetail(invId))
                            },
                            onUpdateStatus = { newStatus ->
                                viewModel.updateQuoteStatus(quote, newStatus)
                            }
                        )
                    } else {
                        viewModel.navigateBack()
                    }
                }

                is Screen.QuoteEdit -> {
                    val existingQuote = currentScreen.quoteId?.let { id -> quotes.find { it.id == id } }
                    QuoteEditScreen(
                        existingQuote = existingQuote,
                        clients = clients,
                        profile = profile,
                        initialClientId = currentScreen.initialClientId,
                        initialInviteNumber = currentScreen.initialInviteNumber,
                        onBack = { viewModel.navigateBack() },
                        onSaveQuote = { quoteToSave ->
                            viewModel.saveQuote(quoteToSave) { savedId ->
                                viewModel.navigateBack()
                                viewModel.navigateTo(Screen.QuoteDetail(savedId))
                                Toast.makeText(context, "Quote saved successfully!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onAddNewClient = { viewModel.navigateTo(Screen.ClientEdit()) }
                    )
                }

                is Screen.Clients -> {
                    ClientsScreen(
                        clients = clients,
                        onNavigate = { viewModel.navigateTo(it) },
                        onImportClients = { importedList ->
                            viewModel.importClients(importedList) { count ->
                                Toast.makeText(context, "Imported $count clients!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                is Screen.ClientDetail -> {
                    val client = clients.find { it.id == currentScreen.clientId }
                    if (client != null) {
                        ClientDetailScreen(
                            client = client,
                            invoices = invoices,
                            quotes = quotes,
                            profile = profile,
                            onBack = { viewModel.navigateBack() },
                            onEdit = { viewModel.navigateTo(Screen.ClientEdit(clientId = client.id)) },
                            onDelete = {
                                viewModel.deleteClient(client) {
                                    viewModel.navigateBack()
                                    Toast.makeText(context, "Client deleted", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    } else {
                        viewModel.navigateBack()
                    }
                }

                is Screen.ClientEdit -> {
                    val client = currentScreen.clientId?.let { id -> clients.find { it.id == id } }
                    ClientEditScreen(
                        client = client,
                        onBack = { viewModel.navigateBack() },
                        onSaveClient = { clientToSave ->
                            viewModel.saveClient(clientToSave) {
                                viewModel.navigateBack()
                                Toast.makeText(context, "Client saved!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                is Screen.Settings -> {
                    SettingsScreen(
                        profile = profile,
                        clients = clients,
                        invoices = invoices,
                        quotes = quotes,
                        onSaveProfile = { updated -> viewModel.saveProfile(updated) },
                        onRestoreBackup = { backup ->
                            viewModel.restoreFullBackup(backup) {
                                Toast.makeText(context, "Full backup restored!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onImportClients = { importedList ->
                            viewModel.importClients(importedList) { count ->
                                Toast.makeText(context, "Imported $count clients!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            // Dialogs
            if (showJobInviteDialog) {
                JobInviteDialog(
                    onDismiss = { viewModel.closeJobInviteDialog() },
                    onProceedWithQuote = { inviteNumber ->
                        viewModel.closeJobInviteDialog()
                        viewModel.navigateTo(Screen.QuoteEdit(initialInviteNumber = inviteNumber))
                    },
                    onProceedWithInvoice = { inviteNumber, poNumber ->
                        viewModel.closeJobInviteDialog()
                        viewModel.navigateTo(Screen.InvoiceEdit(initialInviteNumber = inviteNumber, initialPoNumber = poNumber))
                    }
                )
            }

            quoteToConvert?.let { quote ->
                PoNumberDialog(
                    quote = quote,
                    currencySymbol = profile.currencySymbol,
                    onDismiss = { viewModel.closeConvertQuoteDialog() },
                    onConfirm = { poNumber ->
                        viewModel.executeConvertQuoteToInvoice(poNumber) { newInvoice ->
                            Toast.makeText(
                                context,
                                "Converted to Invoice ${newInvoice.invoiceNumber}!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
            }
        }
    }
}
