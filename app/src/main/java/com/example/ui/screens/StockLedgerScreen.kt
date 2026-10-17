package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.StockLedgerEntity
import com.example.ui.theme.AlertWarning
import com.example.ui.theme.IndustrialAmber
import com.example.ui.theme.ScrapRed
import com.example.ui.theme.SteelBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.InventoryViewModel

@Composable
fun StockLedgerScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit
) {
    val ledger by viewModel.stockLedger.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") }

    var itemToReverse by remember { mutableStateOf<StockLedgerEntity?>(null) }
    var reversalReason by remember { mutableStateOf("") }

    val filteredLedger = remember(ledger, searchQuery, selectedTypeFilter) {
        ledger.filter { entry ->
            val matchesSearch = searchQuery.isBlank() ||
                entry.docNumber.contains(searchQuery, ignoreCase = true) ||
                entry.batchNumber.contains(searchQuery, ignoreCase = true) ||
                entry.dnNumber.contains(searchQuery, ignoreCase = true) ||
                entry.userEmail.contains(searchQuery, ignoreCase = true)

            val matchesType = selectedTypeFilter == "ALL" || entry.movementType == selectedTypeFilter

            matchesSearch && matchesType
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("stock_ledger_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            IndustrialCard(borderColor = IndustrialAmber.copy(alpha = 0.5f)) {
                SectionHeader(
                    title = "Double-Entry Stock Ledger",
                    subtitle = "Immutable audit log of all raw receiving, mfg issues, transfers & deliveries",
                    icon = Icons.Default.CompareArrows
                )
            }
        }

        // Search & Movement Type Filters
        item {
            IndustrialCard {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by Document, Batch, DN, or User") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IndustrialAmber) },
                    modifier = Modifier.fillMaxWidth().testTag("input_ledger_search"),
                    colors = outlinedTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ALL", "RECEIVING_2TON", "BBS_DELIVERY", "PRODUCTION_OUTPUT", "TRANSFER_IN").forEach { type ->
                        FilterChip(
                            selected = selectedTypeFilter == type,
                            onClick = { selectedTypeFilter = type },
                            label = {
                                Text(
                                    when (type) {
                                        "RECEIVING_2TON" -> "Receiving"
                                        "BBS_DELIVERY" -> "Delivery"
                                        "PRODUCTION_OUTPUT" -> "Mfg Output"
                                        "TRANSFER_IN" -> "Transfer"
                                        else -> "All Moves"
                                    },
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                }
            }
        }

        // Item Cards
        items(filteredLedger) { entry ->
            IndustrialCard(
                modifier = Modifier.testTag("ledger_item_${entry.docNumber}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = entry.docNumber,
                                fontWeight = FontWeight.Bold,
                                color = IndustrialAmber,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadge(status = entry.status)
                            Spacer(modifier = Modifier.width(6.dp))
                            FinishBadge(finish = entry.finish)
                        }

                        Text(
                            text = "${entry.movementType} • ${entry.transactionDate}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Text(
                            text = "Path: ${entry.fromLocation ?: "External Vendor"} -> ${entry.toLocation ?: "Customer Dispatch"}",
                            fontSize = 11.sp,
                            color = TextPrimary
                        )

                        Text(
                            text = "${entry.diameterMm}mm • ${entry.pieces} pcs • Batch: ${entry.batchNumber} • Heat: ${entry.heatNumber}",
                            fontSize = 10.sp,
                            color = TextMuted
                        )

                        if (entry.notes.isNotBlank()) {
                            Text(
                                text = "Note: ${entry.notes}",
                                fontSize = 10.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Text(
                            text = "Logged by: ${entry.userEmail} (${entry.userRole})",
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${entry.weightMt} MT",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${entry.weightKg} KG",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )

                        if (entry.status != "REVERSED") {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { itemToReverse = entry },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("btn_reverse_${entry.docNumber}")
                            ) {
                                Icon(Icons.Default.Replay, contentDescription = null, tint = ScrapRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reverse", fontSize = 10.sp, color = ScrapRed)
                            }
                        }
                    }
                }
            }
        }
    }

    // Reversal Confirmation Dialog
    if (itemToReverse != null) {
        val target = itemToReverse!!
        AlertDialog(
            onDismissRequest = { itemToReverse = null },
            title = { Text("Confirm Transaction Reversal", fontWeight = FontWeight.Bold, color = ScrapRed) },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to reverse transaction ${target.docNumber}? This will create a corrective entry and restore the inventory balance non-destructively.",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reversalReason,
                        onValueChange = { reversalReason = it },
                        label = { Text("Mandatory Reversal Justification") },
                        placeholder = { Text("e.g. Weighbridge calibration correction") },
                        modifier = Modifier.fillMaxWidth().testTag("input_reversal_reason"),
                        colors = outlinedTextFieldColors()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reverseTransaction(
                            docNumber = target.docNumber,
                            reason = reversalReason.ifBlank { "Administrative corrective reversal" }
                        )
                        itemToReverse = null
                        reversalReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ScrapRed)
                ) {
                    Text("AUTHORIZE REVERSAL", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemToReverse = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
