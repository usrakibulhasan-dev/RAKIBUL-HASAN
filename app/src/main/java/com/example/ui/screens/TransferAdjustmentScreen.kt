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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.calculator.RebarCalculator
import com.example.ui.theme.AlertWarning
import com.example.ui.theme.IndustrialAmber
import com.example.ui.theme.ScrapRed
import com.example.ui.theme.SteelBlue
import com.example.ui.theme.SteelCardBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.InventoryViewModel

@Composable
fun TransferAdjustmentScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit
) {
    val balances by viewModel.stockBalances.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    // Transfer State
    var fromLocation by remember { mutableStateOf("RM-BAY-01") }
    var toLocation by remember { mutableStateOf("LINE-01-CUT") }
    var diameterInput by remember { mutableStateOf("12") }
    var lengthInput by remember { mutableStateOf("12.0") }
    var gradeInput by remember { mutableStateOf("Grade 60") }
    var finishInput by remember { mutableStateOf("BLACK") }
    var batchInput by remember { mutableStateOf("B-2026-1020") }
    var heatInput by remember { mutableStateOf("HT-88421") }
    var companyInput by remember { mutableStateOf("TALAI") }
    var supplierInput by remember { mutableStateOf("MITWALLI") }
    var transferPiecesInput by remember { mutableStateOf("188") } // 1 bundle of 12mm

    var allowNegativeOverride by remember { mutableStateOf(false) }
    var overrideReason by remember { mutableStateOf("") }

    // Physical Count State
    var countLocation by remember { mutableStateOf("RM-BAY-01") }
    var physicalPiecesInput by remember { mutableStateOf("940") }
    var countReason by remember { mutableStateOf("Monthly physical stock count verification") }

    Column(modifier = Modifier.fillMaxSize().testTag("transfer_adjustment_screen")) {
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
                text = { Text("Internal Location Transfer", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Physical Stock Count", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (selectedTab == 0) {
                item {
                    IndustrialCard(borderColor = IndustrialAmber.copy(alpha = 0.5f)) {
                        SectionHeader(
                            title = "Atomic Location Stock Transfer",
                            subtitle = "Transfers stock between warehouse bays, cutting lines, and yards atomically",
                            icon = Icons.Default.SwapHoriz,
                            accentColor = IndustrialAmber
                        )
                    }
                }

                // Locations
                item {
                    IndustrialCard {
                        Text(text = "SOURCE & DESTINATION LOCATIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = fromLocation,
                                onValueChange = { fromLocation = it },
                                label = { Text("From Location") },
                                modifier = Modifier.weight(1f).testTag("input_transfer_from"),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = toLocation,
                                onValueChange = { toLocation = it },
                                label = { Text("To Location") },
                                modifier = Modifier.weight(1f).testTag("input_transfer_to"),
                                colors = outlinedTextFieldColors()
                            )
                        }
                    }
                }

                // Item & Quantity
                item {
                    IndustrialCard {
                        Text(text = "ITEM SPECIFICATION & QUANTITY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = diameterInput,
                                onValueChange = { diameterInput = it },
                                label = { Text("Diameter (mm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = transferPiecesInput,
                                onValueChange = { transferPiecesInput = it },
                                label = { Text("Transfer Pieces") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("input_transfer_pcs"),
                                colors = outlinedTextFieldColors()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = batchInput,
                                onValueChange = { batchInput = it },
                                label = { Text("Batch Number") },
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = finishInput,
                                onValueChange = { finishInput = it },
                                label = { Text("Finish (BLACK/EPOXY_GREEN)") },
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                        }

                        // Calculated Transfer weight
                        val d = diameterInput.toIntOrNull() ?: 12
                        val pcs = transferPiecesInput.toIntOrNull() ?: 0
                        val len = lengthInput.toDoubleOrNull() ?: 12.0
                        val uWt = RebarCalculator.getUnitWeight(d, finishInput == "EPOXY_GREEN")
                        val kg = RebarCalculator.round(pcs * len * uWt, 2)
                        val mt = RebarCalculator.round(kg / 1000.0, 3)

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Calculated Transfer: $kg KG ($mt MT) • Standard Bundle: ${RebarCalculator.getPiecesPer2TonBundle(d)} pcs",
                            fontSize = 11.sp,
                            color = IndustrialAmber,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Negative override
                item {
                    IndustrialCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Supervisor Override Authorization", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Required only if source location has insufficient recorded stock", fontSize = 10.sp, color = TextMuted)
                            }
                            Switch(
                                checked = allowNegativeOverride,
                                onCheckedChange = { allowNegativeOverride = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = ScrapRed, checkedTrackColor = ScrapRed.copy(alpha = 0.5f))
                            )
                        }
                        if (allowNegativeOverride) {
                            OutlinedTextField(
                                value = overrideReason,
                                onValueChange = { overrideReason = it },
                                label = { Text("Override Justification") },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                colors = outlinedTextFieldColors()
                            )
                        }
                    }
                }

                // Post Transfer Button
                item {
                    Button(
                        onClick = {
                            viewModel.postInternalTransfer(
                                companyCode = companyInput,
                                fromLocation = fromLocation,
                                toLocation = toLocation,
                                diameterMm = diameterInput.toIntOrNull() ?: 12,
                                lengthM = lengthInput.toDoubleOrNull() ?: 12.0,
                                grade = gradeInput,
                                finish = finishInput,
                                batchNumber = batchInput,
                                heatNumber = heatInput,
                                supplierOrigin = supplierInput,
                                transferPieces = transferPiecesInput.toIntOrNull() ?: 0,
                                allowNegativeOverride = allowNegativeOverride,
                                overrideReason = overrideReason,
                                onSuccess = { onNavigateBack() }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_post_transfer"),
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialAmber),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "POST ATOMIC STOCK TRANSFER",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            } else {
                // Tab 1: Physical Stock Count
                item {
                    IndustrialCard(borderColor = SteelBlue.copy(alpha = 0.5f)) {
                        SectionHeader(
                            title = "Physical Stock Count Reconciliation",
                            subtitle = "Adjust system recorded inventory against physical yard count",
                            icon = Icons.Default.SwapHoriz,
                            accentColor = SteelBlue
                        )
                    }
                }

                item {
                    IndustrialCard {
                        Text(text = "LOCATION & PHYSICAL PIECES COUNTED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = countLocation,
                            onValueChange = { countLocation = it },
                            label = { Text("Location (e.g. RM-BAY-01)") },
                            modifier = Modifier.fillMaxWidth().testTag("input_count_location"),
                            colors = outlinedTextFieldColors()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = physicalPiecesInput,
                            onValueChange = { physicalPiecesInput = it },
                            label = { Text("Physical Pieces Counted") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("input_physical_pcs"),
                            colors = outlinedTextFieldColors()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = countReason,
                            onValueChange = { countReason = it },
                            label = { Text("Reason for Adjustment") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = outlinedTextFieldColors()
                        )
                    }
                }

                item {
                    Button(
                        onClick = {
                            viewModel.postStockAdjustment(
                                balanceId = 1L,
                                physicalPieces = physicalPiecesInput.toIntOrNull() ?: 0,
                                physicalKg = 10000.0,
                                reason = countReason,
                                userEmail = "controller@talaisteel.com",
                                userRole = com.example.data.model.UserRole.INVENTORY_CONTROLLER
                            )
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_post_adjustment"),
                        colors = ButtonDefaults.buttonColors(containerColor = SteelBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("RECORD PHYSICAL COUNT ADJUSTMENT", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
