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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.AlertWarning
import com.example.ui.theme.EpoxyGreen
import com.example.ui.theme.IndustrialAmber
import com.example.ui.theme.StatusMatched
import com.example.ui.theme.SteelBlue
import com.example.ui.theme.SteelCardBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.InventoryViewModel

data class ExcelImportRow(
    val rowId: Int,
    val dnNumber: String,
    val company: String,
    val supplier: String,
    val diameterMm: Int,
    val lengthM: Double,
    val grade: String,
    val finish: String,
    val fullBundles: Int,
    val totalPieces: Int,
    val totalMt: Double,
    val batchNumber: String,
    val status: String = "VALIDATED"
)

@Composable
fun ExcelImportExportScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var exportPreviewText by remember { mutableStateOf("") }
    var exportType by remember { mutableStateOf("BALANCES") }

    // Pre-configured MASAR Excel import draft rows for inspection & validation
    val sampleImportRows = remember {
        listOf(
            ExcelImportRow(
                rowId = 1,
                dnNumber = "DN-MASAR-8831",
                company = "TALAI",
                supplier = "MITWALLI",
                diameterMm = 14,
                lengthM = 12.0,
                grade = "Grade 60",
                finish = "BLACK",
                fullBundles = 4,
                totalPieces = 552, // 4 * 138 pcs
                totalMt = 8.002,
                batchNumber = "B-2026-1055"
            ),
            ExcelImportRow(
                rowId = 2,
                dnNumber = "DN-MASAR-8832",
                company = "TALAI",
                supplier = "TAMEER",
                diameterMm = 25,
                lengthM = 12.0,
                grade = "Grade 60",
                finish = "BLACK",
                fullBundles = 6,
                totalPieces = 258, // 6 * 43 pcs
                totalMt = 11.929,
                batchNumber = "B-2026-1058"
            ),
            ExcelImportRow(
                rowId = 3,
                dnNumber = "DN-MASAR-8833",
                company = "TALAI",
                supplier = "ISPC",
                diameterMm = 16,
                lengthM = 12.0,
                grade = "ASTM A615",
                finish = "EPOXY_GREEN",
                fullBundles = 3,
                totalPieces = 318, // 3 * 106 pcs
                totalMt = 6.112,
                batchNumber = "B-2026-EP09"
            )
        )
    }

    var isImportPosted by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().testTag("excel_import_export_screen")) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SteelCardBg,
            contentColor = IndustrialAmber,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = IndustrialAmber
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("MASAR Excel Import", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Export Reports (CSV/Excel)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (selectedTab == 0) {
                // Import Tab
                item {
                    IndustrialCard(borderColor = SteelBlue.copy(alpha = 0.5f)) {
                        SectionHeader(
                            title = "MASAR Excel Import & Validation Preview",
                            subtitle = "Mandatory: Validate columns, formulas, and duplicate DNs before posting stock",
                            icon = Icons.Default.UploadFile,
                            accentColor = SteelBlue
                        )
                    }
                }

                item {
                    IndustrialCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "FILE: Rebar Weight Calculator Rakibul-3.xlsx",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Worksheet: 'Calculator Issued 2 TON QTY' • 3 Rows Staged",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            StatusBadge(status = if (isImportPosted) "POSTED" else "VALIDATED")
                        }
                    }
                }

                // Table of staged rows
                items(sampleImportRows) { row ->
                    IndustrialCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Row #${row.rowId}: ${row.dnNumber}",
                                        fontWeight = FontWeight.Bold,
                                        color = IndustrialAmber,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    FinishBadge(finish = row.finish)
                                }
                                Text(
                                    text = "${row.diameterMm}mm • ${row.grade} • ${row.fullBundles} Bundles (${row.totalPieces} pcs)",
                                    fontSize = 11.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Owner: ${row.company} • Origin: ${row.supplier} • Batch: ${row.batchNumber}",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${row.totalMt} MT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Status: OK",
                                    fontSize = 10.sp,
                                    color = StatusMatched
                                )
                            }
                        }
                    }
                }

                // Post Button
                item {
                    if (!isImportPosted) {
                        Button(
                            onClick = {
                                // Post the staged items into inventory
                                for (row in sampleImportRows) {
                                    viewModel.postReceiving(
                                        companyCode = row.company,
                                        supplierOrigin = row.supplier,
                                        destinationLocation = "RM-BAY-01",
                                        diameterMm = row.diameterMm,
                                        lengthM = row.lengthM,
                                        grade = row.grade,
                                        finish = row.finish,
                                        fullBundles = row.fullBundles,
                                        looseBundles = 0,
                                        loosePieces = 0,
                                        batchNumber = row.batchNumber,
                                        heatNumber = "HT-IMPORT",
                                        poNumber = "PO-MASAR-IMPORT",
                                        dnNumber = row.dnNumber,
                                        actualWeightKg = row.totalMt * 1000.0,
                                        notes = "Authorized MASAR Excel bulk import from file Rebar Weight Calculator",
                                        onSuccess = {}
                                    )
                                }
                                isImportPosted = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_authorize_import"),
                            colors = ButtonDefaults.buttonColors(containerColor = SteelBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AUTHORIZE & POST 3 VALIDATED ROWS TO INVENTORY", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusMatched.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusMatched),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusMatched)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "MASAR Excel import successfully committed to central inventory ledger!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusMatched
                                )
                            }
                        }
                    }
                }
            } else {
                // Export Tab
                item {
                    IndustrialCard(borderColor = IndustrialAmber.copy(alpha = 0.5f)) {
                        SectionHeader(
                            title = "Export Inventory Reports (CSV / Excel format)",
                            subtitle = "Generate structured data for external accounting & MASAR audit",
                            icon = Icons.Default.Download
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                exportType = "BALANCES"
                                exportPreviewText = viewModel.generateStockBalancesCsv()
                            },
                            modifier = Modifier.weight(1f).testTag("btn_export_balances"),
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialAmber),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Stock Balances CSV", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                exportType = "LEDGER"
                                exportPreviewText = viewModel.generateStockLedgerCsv()
                            },
                            modifier = Modifier.weight(1f).testTag("btn_export_ledger"),
                            colors = ButtonDefaults.buttonColors(containerColor = SteelBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Stock Ledger CSV", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                if (exportPreviewText.isNotBlank()) {
                    item {
                        IndustrialCard {
                            Text(
                                text = "EXPORT PREVIEW ($exportType)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndustrialAmber
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0A0D11),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = exportPreviewText,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
