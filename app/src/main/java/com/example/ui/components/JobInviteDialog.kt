package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun JobInviteDialog(
    onDismiss: () -> Unit,
    onProceedWithQuote: (inviteNumber: String) -> Unit,
    onProceedWithInvoice: (inviteNumber: String, poNumber: String) -> Unit
) {
    var selectedType by remember { mutableStateOf("quote") } // "quote" or "invoice"
    var inviteNumber by remember { mutableStateOf("") }
    var poNumber by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text("Process Job Invite", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Did you receive a new freelance job invite? Choose whether to create a preliminary Price Quote or bill directly with an Invoice.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Document Type",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                // Option 1: Quote
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedType == "quote") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .clickable { selectedType = "quote" }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedType == "quote",
                        onClick = { selectedType = "quote" },
                        modifier = Modifier.testTag("radio_select_quote")
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text("Price Quote (Estimate)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Text("Send estimate first; can convert to Invoice later", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Option 2: Direct Invoice
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedType == "invoice") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .clickable { selectedType = "invoice" }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedType == "invoice",
                        onClick = { selectedType = "invoice" },
                        modifier = Modifier.testTag("radio_select_invoice")
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text("Direct Invoice", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Text("Bill client immediately with auto-incremented invoice #", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = inviteNumber,
                    onValueChange = {
                        inviteNumber = it
                        errorMessage = ""
                    },
                    label = { Text("Job Invite / RFP Reference #") },
                    placeholder = { Text("e.g. INVITE-8491, JOB-2026-X") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invite_number_input"),
                    singleLine = true,
                    isError = errorMessage.isNotBlank(),
                    supportingText = if (errorMessage.isNotBlank()) {
                        { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    shape = RoundedCornerShape(10.dp)
                )

                if (selectedType == "invoice") {
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = poNumber,
                        onValueChange = { poNumber = it },
                        label = { Text("PO Number (Optional)") },
                        placeholder = { Text("e.g. PO-8921") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("job_invite_po_number_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inviteNumber.isBlank()) {
                        errorMessage = "Please enter the job invite number"
                        return@Button
                    }
                    if (selectedType == "quote") {
                        onProceedWithQuote(inviteNumber.trim())
                    } else {
                        onProceedWithInvoice(inviteNumber.trim(), poNumber.trim())
                    }
                },
                modifier = Modifier.testTag("proceed_job_invite_button")
            ) {
                Text(if (selectedType == "quote") "Create Quote" else "Create Invoice")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
