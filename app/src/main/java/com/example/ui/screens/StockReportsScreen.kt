package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.theme.AlertWarning
import com.example.ui.theme.BlackSteel
import com.example.ui.theme.EpoxyGreen
import com.example.ui.theme.IndustrialAmber
import com.example.ui.theme.ScrapRed
import com.example.ui.theme.StatusMatched
import com.example.ui.theme.SteelBlue
import com.example.ui.theme.SteelCardBg
import com.example.ui.theme.SteelCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.InventoryViewModel

@Composable
fun StockReportsScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit
) {
    val balances by viewModel.stockBalances.collectAsStateWithLifecycle()
    val ledger by viewModel.stockLedger.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) }
    var diameterFilter by remember { mutableStateOf<Int?>(null) }
    var finishFilter by remember { mutableStateOf("ALL") }

    // 1. Raw Material Balances only (Locations: RM-BAY-01, RM-BAY-02, RM-BAY-03, EPOXY-YARD)
    val rawMaterialBalances = remember(balances, diameterFilter, finishFilter) {
        balances.filter { b ->
            val isRawLoc = b.locationCode.startsWith("RM-") || b.locationCode == "EPOXY-YARD"
            val matchesDia = diameterFilter == null || b.diameterMm == diameterFilter
            val matchesFinish = finishFilter == "ALL" || b.finish == finishFilter
            isRawLoc && matchesDia && matchesFinish
        }
    }

    // 2. Receiving Register (All as per 2 TON standard)
    val receivingRegister = remember(ledger, diameterFilter, finishFilter) {
        ledger.filter { l ->
            val isRecv = l.movementType == "RECEIVING_2TON" && l.status != "REVERSED"
            val matchesDia = diameterFilter == null || l.diameterMm == diameterFilter
            val matchesFinish = finishFilter == "ALL" || l.finish == finishFilter
            isRecv && matchesDia && matchesFinish
        }
    }

    // 3. Material Issue Register (Issues to production / shop floor)
    val issueRegister = remember(ledger, diameterFilter, finishFilter) {
        ledger.filter { l ->
            val isIssue = (l.movementType == "ISSUE_PRODUCTION" || l.movementType == "PRODUCTION_OUTPUT" || l.fromLocation?.startsWith("RM-") == true) && l.status != "REVERSED"
            val matchesDia = diameterFilter == null || l.diameterMm == diameterFilter
            val matchesFinish = finishFilter == "ALL" || l.finish == finishFilter
            isIssue && matchesDia && matchesFinish
        }
    }

    val totalRawMt = remember(rawMaterialBalances) { rawMaterialBalances.sumOf { it.balanceMt } }
    val totalRawPieces = remember(rawMaterialBalances) { rawMaterialBalances.sumOf { it.totalPieces } }
    val totalRawBundles = remember(rawMaterialBalances) { rawMaterialBalances.sumOf { it.totalBundles } }

    val totalReceivedMt = remember(receivingRegister) { receivingRegister.sumOf { it.weightMt } }
    val totalIssuedMt = remember(issueRegister) { issueRegister.sumOf { it.weightMt } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("stock_reports_screen")
    ) {
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = SteelCardBg,
            contentColor = IndustrialAmber,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = IndustrialAmber
                )
            }
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Raw Stock On-Hand", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Receiving (2-Ton)", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = { Text("Material Issue", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = activeTab == 3,
                onClick = { activeTab = 3 },
                text = { Text("Summary Ledger", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                IndustrialCard(borderColor = IndustrialAmber.copy(alpha = 0.5f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            SectionHeader(
                                title = "Raw Material Stock, Receiving & Issue Report",
                                subtitle = "Focused factory register: Receiving as per 2-TON | Issues to production",
                                icon = Icons.Default.Assessment,
                                accentColor = IndustrialAmber
                            )
                        }
                    }
                }
            }

            // High Level KPI Summary Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IndustrialCard(modifier = Modifier.weight(1f)) {
                        StatMetric(
                            label = "RAW ON-HAND",
                            value = "${String.format("%.3f", totalRawMt)}",
                            unit = "MT",
                            color = IndustrialAmber,
                            testTag = "report_kpi_raw_on_hand"
                        )
                        Text(text = "$totalRawPieces Pieces ($totalRawBundles Bnd)", fontSize = 10.sp, color = TextMuted)
                    }

                    IndustrialCard(modifier = Modifier.weight(1f)) {
                        StatMetric(
                            label = "TOTAL RECEIVED",
                            value = "${String.format("%.3f", totalReceivedMt)}",
                            unit = "MT",
                            color = EpoxyGreen,
                            testTag = "report_kpi_received"
                        )
                        Text(text = "All as per 2 TON", fontSize = 10.sp, color = TextMuted)
                    }

                    IndustrialCard(modifier = Modifier.weight(1f)) {
                        StatMetric(
                            label = "TOTAL ISSUED",
                            value = "${String.format("%.3f", totalIssuedMt)}",
                            unit = "MT",
                            color = SteelBlue,
                            testTag = "report_kpi_issued"
                        )
                        Text(text = "To Cutting & Bend", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }

            // Quick Filters (Finish & Diameters)
            item {
                IndustrialCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = finishFilter == "ALL",
                            onClick = { finishFilter = "ALL" },
                            label = { Text("All Finishes", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = finishFilter == "BLACK",
                            onClick = { finishFilter = "BLACK" },
                            label = { Text("Black Steel", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = finishFilter == "EPOXY_GREEN",
                            onClick = { finishFilter = "EPOXY_GREEN" },
                            label = { Text("Epoxy Green", fontSize = 10.sp) }
                        )
                    }
                }
            }

            // TAB 0: RAW MATERIAL ON-HAND
            if (activeTab == 0) {
                item {
                    Text(
                        text = "CURRENT RAW MATERIAL ON-HAND (${rawMaterialBalances.size} LOTS)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndustrialAmber
                    )
                }

                items(rawMaterialBalances) { bal ->
                    IndustrialCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${bal.diameterMm}mm Standard Bar",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    FinishBadge(finish = bal.finish)
                                }
                                Text(
                                    text = "Location: ${bal.locationCode} • Owner: ${bal.companyCode} (Origin: ${bal.supplierOrigin})",
                                    fontSize = 11.sp,
                                    color = IndustrialAmber,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Batch: ${bal.batchNumber} • Heat: ${bal.heatNumber} • Grade: ${bal.grade}",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${bal.balanceMt} MT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${bal.balanceKg} KG",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${bal.totalPieces} pcs (${bal.totalBundles} 2T Bnd)",
                                    fontSize = 10.sp,
                                    color = IndustrialAmber,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // TAB 1: RECEIVING REGISTER (2-TON)
            else if (activeTab == 1) {
                item {
                    Text(
                        text = "RAW MATERIAL RECEIVING REGISTER (ALL AS PER 2 TON)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EpoxyGreen
                    )
                }

                items(receivingRegister) { entry ->
                    IndustrialCard {
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
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    FinishBadge(finish = entry.finish)
                                }
                                Text(
                                    text = "Date: ${entry.transactionDate} • ${entry.supplierOrigin} ➔ ${entry.toLocation}",
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "PO: ${entry.poNumber} • DN: ${entry.dnNumber} • Batch: ${entry.batchNumber}",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${entry.diameterMm}mm • ${entry.bundles} Bundles (${entry.pieces} Pieces)",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+${entry.weightMt} MT",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = EpoxyGreen,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${entry.weightKg} KG",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (entry.actualWeightKg != null) {
                                    Text(
                                        text = "Scale: ${entry.actualWeightKg} KG",
                                        fontSize = 9.sp,
                                        color = StatusMatched,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: MATERIAL ISSUE REGISTER
            else if (activeTab == 2) {
                item {
                    Text(
                        text = "MATERIAL ISSUE REGISTER (RAW MATERIAL TO SHOP FLOOR)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SteelBlue
                    )
                }

                items(issueRegister) { entry ->
                    IndustrialCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.docNumber,
                                    fontWeight = FontWeight.Bold,
                                    color = SteelBlue,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "From: ${entry.fromLocation ?: "RM-BAY-01"} ➔ To: ${entry.toLocation ?: "LINE-01-CUT"}",
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Date: ${entry.transactionDate} • ${entry.notes}",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${entry.diameterMm}mm • ${entry.pieces} Consumed Bars",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "-${entry.weightMt} MT",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = SteelBlue,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${entry.weightKg} KG",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // TAB 3: SUMMARY BALANCE LEDGER (Opening + In - Out = Closing)
            else {
                item {
                    IndustrialCard(
                        borderColor = IndustrialAmber,
                        containerColor = IndustrialAmber.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "RAW MATERIAL INVENTORY RECONCILIATION SUMMARY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialAmber
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Opening Stock + Posted 2-TON Received - Material Issued = Closing On-Hand",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMetric(label = "POSTED IN (2-TON)", value = "+${String.format("%.3f", totalReceivedMt)}", unit = "MT", color = EpoxyGreen)
                            StatMetric(label = "POSTED OUT (ISSUE)", value = "-${String.format("%.3f", totalIssuedMt)}", unit = "MT", color = SteelBlue)
                            StatMetric(label = "CLOSING RAW", value = "${String.format("%.3f", totalRawMt)}", unit = "MT", color = IndustrialAmber)
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            // Show export notification
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialAmber),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("EXPORT RAW MATERIAL STOCK & ISSUE REPORT (CSV)", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}
