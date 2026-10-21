package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Client
import com.example.data.model.FreelancerProfile
import com.example.data.model.Invoice
import com.example.data.model.Quote
import com.example.data.repository.FreelanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Dashboard : Screen
    data object Invoices : Screen
    data class InvoiceDetail(val invoiceId: Long) : Screen
    data class InvoiceEdit(val invoiceId: Long? = null, val initialClientId: Long? = null, val initialInviteNumber: String = "", val initialPoNumber: String = "") : Screen
    data object Quotes : Screen
    data class QuoteDetail(val quoteId: Long) : Screen
    data class QuoteEdit(val quoteId: Long? = null, val initialClientId: Long? = null, val initialInviteNumber: String = "") : Screen
    data object Clients : Screen
    data class ClientDetail(val clientId: Long) : Screen
    data class ClientEdit(val clientId: Long? = null) : Screen
    data object Settings : Screen
}

data class FinancialStats(
    val totalInvoiced: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalOutstanding: Double = 0.0,
    val totalOverdue: Double = 0.0,
    val totalQuotesPipeline: Double = 0.0,
    val activeInvoicesCount: Int = 0,
    val paidInvoicesCount: Int = 0,
    val pendingQuotesCount: Int = 0
)

class FreelanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FreelanceRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FreelanceRepository(db)
    }

    val clients: StateFlow<List<Client>> = repository.allClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<Invoice>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quotes: StateFlow<List<Quote>> = repository.allQuotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<FreelancerProfile> = repository.profile
        .combine(MutableStateFlow(FreelancerProfile())) { current, default ->
            current ?: default
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FreelancerProfile())

    // Financial Metrics derived from live invoice & quote streams
    val stats: StateFlow<FinancialStats> = combine(invoices, quotes) { invList, qteList ->
        var totalInv = 0.0
        var totalPaid = 0.0
        var totalOut = 0.0
        var totalOver = 0.0
        var activeCount = 0
        var paidCount = 0

        val now = System.currentTimeMillis()

        for (inv in invList) {
            totalInv += inv.total
            when (inv.status) {
                "Paid" -> {
                    totalPaid += inv.total
                    paidCount++
                }
                "Draft" -> {
                    activeCount++
                }
                "Sent" -> {
                    activeCount++
                    if (inv.hasDueDate && now > inv.dueDate) {
                        totalOver += (inv.total - inv.amountPaid)
                    } else {
                        totalOut += (inv.total - inv.amountPaid)
                    }
                }
                "Overdue" -> {
                    activeCount++
                    if (inv.hasDueDate) {
                        totalOver += (inv.total - inv.amountPaid)
                    } else {
                        totalOut += (inv.total - inv.amountPaid)
                    }
                }
            }
        }

        var quotesPipeline = 0.0
        var pendingQuotes = 0
        for (qte in qteList) {
            if (qte.status == "Pending" || qte.status == "Sent") {
                quotesPipeline += qte.total
                pendingQuotes++
            }
        }

        FinancialStats(
            totalInvoiced = totalInv,
            totalPaid = totalPaid,
            totalOutstanding = totalOut,
            totalOverdue = totalOver,
            totalQuotesPipeline = quotesPipeline,
            activeInvoicesCount = activeCount,
            paidInvoicesCount = paidCount,
            pendingQuotesCount = pendingQuotes
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialStats())

    // Navigation Stack
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Dashboard))
    val screenStack: StateFlow<List<Screen>> = _screenStack.asStateFlow()

    val currentScreen: Screen
        get() = _screenStack.value.lastOrNull() ?: Screen.Dashboard

    fun navigateTo(screen: Screen) {
        _screenStack.value = _screenStack.value + screen
    }

    fun navigateBack(): Boolean {
        if (_screenStack.value.size > 1) {
            _screenStack.value = _screenStack.value.dropLast(1)
            return true
        }
        return false
    }

    fun switchTab(rootScreen: Screen) {
        _screenStack.value = listOf(rootScreen)
    }

    // Interactive Dialogs
    // 1. Job Invite Prompt Dialog: Asks for "Quote or Direct Invoice" and "Invite Number"
    private val _showJobInviteDialog = MutableStateFlow(false)
    val showJobInviteDialog: StateFlow<Boolean> = _showJobInviteDialog.asStateFlow()

    fun openJobInviteDialog() {
        _showJobInviteDialog.value = true
    }

    fun closeJobInviteDialog() {
        _showJobInviteDialog.value = false
    }

    // 2. Convert Quote Dialog: Asks for "PO Number"
    private val _quoteToConvert = MutableStateFlow<Quote?>(null)
    val quoteToConvert: StateFlow<Quote?> = _quoteToConvert.asStateFlow()

    fun openConvertQuoteDialog(quote: Quote) {
        _quoteToConvert.value = quote
    }

    fun closeConvertQuoteDialog() {
        _quoteToConvert.value = null
    }

    fun executeConvertQuoteToInvoice(poNumber: String, onConverted: (Invoice) -> Unit) {
        val quote = _quoteToConvert.value ?: return
        viewModelScope.launch {
            val invoice = repository.convertQuoteToInvoice(quote.id, poNumber)
            _quoteToConvert.value = null
            if (invoice != null) {
                // Navigate directly to the newly generated invoice detail view!
                navigateTo(Screen.InvoiceDetail(invoice.id))
                onConverted(invoice)
            }
        }
    }

    // Client CRUD
    fun saveClient(client: Client, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (client.id == 0L) {
                repository.insertClient(client)
            } else {
                repository.updateClient(client)
            }
            onComplete()
        }
    }

    fun deleteClient(client: Client, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteClient(client)
            onComplete()
        }
    }

    // Invoice CRUD & Actions
    suspend fun peekNextInvoiceNumber(): String = repository.peekNextInvoiceNumber()

    fun saveInvoice(invoice: Invoice, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            if (invoice.id == 0L) {
                val nextSeq = repository.getNextInvoiceSequence()
                val profileVal = repository.getProfileSync()
                val prefix = profileVal.invoicePrefix.ifBlank { "INV-" }
                val number = String.format("%s%04d", prefix, nextSeq)
                val newId = repository.insertInvoice(invoice.copy(invoiceNumber = number, invoiceSequence = nextSeq))
                onComplete(newId)
            } else {
                repository.updateInvoice(invoice)
                onComplete(invoice.id)
            }
        }
    }

    fun updateInvoiceStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateInvoiceStatus(id, status)
        }
    }

    fun deleteInvoice(invoice: Invoice, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
            onComplete()
        }
    }

    // Quote CRUD & Actions
    suspend fun peekNextQuoteNumber(): String = repository.peekNextQuoteNumber()

    fun saveQuote(quote: Quote, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            if (quote.id == 0L) {
                val number = repository.peekNextQuoteNumber()
                val newId = repository.insertQuote(quote.copy(quoteNumber = number))
                onComplete(newId)
            } else {
                repository.updateQuote(quote)
                onComplete(quote.id)
            }
        }
    }

    fun updateQuoteStatus(quote: Quote, status: String) {
        viewModelScope.launch {
            repository.updateQuote(quote.copy(status = status, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteQuote(quote: Quote, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteQuote(quote)
            onComplete()
        }
    }

    // Profile Settings
    fun saveProfile(profile: FreelancerProfile) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    // Data Import & Backup Restore
    fun importClients(newClients: List<Client>, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            if (newClients.isNotEmpty()) {
                repository.insertClients(newClients)
            }
            onComplete(newClients.size)
        }
    }

    fun restoreFullBackup(backupData: com.example.util.FullBackupData, onComplete: () -> Unit) {
        viewModelScope.launch {
            if (backupData.clients.isNotEmpty()) {
                repository.insertClients(backupData.clients)
            }
            if (backupData.invoices.isNotEmpty()) {
                repository.insertInvoices(backupData.invoices)
            }
            if (backupData.quotes.isNotEmpty()) {
                repository.insertQuotes(backupData.quotes)
            }
            backupData.profile?.let { repository.updateProfile(it) }
            onComplete()
        }
    }
}
