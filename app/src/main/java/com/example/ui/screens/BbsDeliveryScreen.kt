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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
fun BbsDeliveryScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit
) {
    val balances by viewModel.stockBalances.collectAsStateWithLifecycle()
    val diameterOptions = listOf(8, 10, 12, 14, 16, 18, 20, 22, 25, 32)

    var customerName by remember { mutableStateOf("Nesma & Partners") }
    var projectName by remember { mutableStateOf("Jeddah Metro Station Phase 2") }
    var deliveryNote by remember { mutableStateOf("DN-TALAI-2026-081") }
    var bbsRef by remember { mutableStateOf("BBS-METRO-PIER-C3") }
    var barMark by remember { mutableStateOf("M-01-MAIN") }

    var selectedDiameter by remember { mutableIntStateOf(16) }
    var cutLengthInput by remember { mutableStateOf("5.80") }
    var requiredPiecesInput by remember { mutableStateOf("150") }
    var selectedGrade by remember { mutableStateOf("Grade 60") }
    var selectedFinish by remember { mutableStateOf("BLACK") }
    var sourceLocation by remember { mutableStateOf("FG-YARD-A") }
    var selectedCompany by remember { mutableStateOf("TALAI") }
    var batchNumber by remember { mutableStateOf("B-2026-1025") }
    var heatNumber by remember { mutableStateOf("HT-99312") }

    var allowNegativeOverride by remember { mutableStateOf(false) }
    var overrideReason by remember { mutableStateOf("") }

    // Live BBS Calculation
    val bbsCalc by remember(selectedDiameter, cutLengthInput, requiredPiecesInput, selectedFinish) {
        derivedStateOf {
            val cutL = cutLengthInput.toDoubleOrNull() ?: 1.0
            val reqP = requiredPiecesInput.toIntOrNull() ?: 0
            RebarCalculator.calculateBbsDelivery(
                diameterMm = selectedDiameter,
                cutLengthM = cutL,
                requiredPieces = reqP,
                isEpoxy = selectedFinish == "EPOXY_GREEN"
            )
        }
    }

    // Check available inventory in sourceLocation
    val matchingBalance = remember(balances, selectedCompany, sourceLocation, selectedDiameter, selectedFinish, batchNumber) {
        balances.find {
            it.companyCode == selectedCompany &&
            it.locationCode == sourceLocation &&
            it.diameterMm == selectedDiameter &&
            it.finish == selectedFinish &&
            it.batchNumber == batchNumber
        }
    }

    val availablePcs = matchingBalance?.totalPieces ?: 0
    val availableKg = matchingBalance?.balanceKg ?: 0.0
    val isShortage = availablePcs < (requiredPiecesInput.toIntOrNull() ?: 0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("bbs_delivery_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            IndustrialCard(borderColor = SteelBlue.copy(alpha = 0.5f)) {
                SectionHeader(
                    title = "Customer Delivery (BBS Engine)",
                    subtitle = "Factory standard: Calculator Issued BBS QTY rule",
                    icon = Icons.Default.LocalShipping,
                    accentColor = SteelBlue
                )
            }
        }

        // Customer & Project Info
        item {
            IndustrialCard {
                Text(text = "CUSTOMER & PROJECT INFORMATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        modifier = Modifier.weight(1f).testTag("input_customer_name"),
                        colors = outlinedTextFieldColors()
                    )
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        label = { Text("Project Name") },
                        modifier = Modifier.weight(1f).testTag("input_project_name"),
                        colors = outlinedTextFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = deliveryNote,
                        onValueChange = { deliveryNote = it },
                        label = { Text("App Delivery Note (DN)") },
                        modifier = Modifier.weight(1f).testTag("input_delivery_note"),
                        colors = outlinedTextFieldColors()
                    )
                    OutlinedTextField(
                        value = bbsRef,
                        onValueChange = { bbsRef = it },
                        label = { Text("BBS Reference No.") },
                        modifier = Modifier.weight(1f).testTag("input_bbs_ref"),
                        colors = outlinedTextFieldColors()
                    )
                }
            }
        }

        // Rebar Size & Cut Length
        item {
            IndustrialCard {
                Text(text = "REBAR DIAMETER SPECIFICATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    diameterOptions.take(5).forEach { d ->
                        DiameterChip(
                            diameter = d,
                            selected = selectedDiameter == d,
                            onClick = { selectedDiameter = d },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    diameterOptions.drop(5).forEach { d ->
                        DiameterChip(
                            diameter = d,
                            selected = selectedDiameter == d,
                            onClick = { selectedDiameter = d },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cutLengthInput,
                        onValueChange = { cutLengthInput = it },
                        label = { Text("Cut Length (m)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("input_cut_length"),
                        colors = outlinedTextFieldColors()
                    )
                    OutlinedTextField(
                        value = requiredPiecesInput,
                        onValueChange = { requiredPiecesInput = it },
                        label = { Text("Required PCS") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_required_pcs"),
                        colors = outlinedTextFieldColors()
                    )
                    OutlinedTextField(
                        value = barMark,
                        onValueChange = { barMark = it },
                        label = { Text("Bar Mark") },
                        modifier = Modifier.weight(1f),
                        colors = outlinedTextFieldColors()
                    )
                }
            }
        }

        // Finish & Grade
        item {
            IndustrialCard {
                Text(text = "SURFACE FINISH & GRADE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = selectedFinish == "BLACK",
                        onClick = { selectedFinish = "BLACK" },
                        label = { Text("Black Steel") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedFinish == "EPOXY_GREEN",
                        onClick = { selectedFinish = "EPOXY_GREEN" },
                        label = { Text("Epoxy Green") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Live BBS Calculation Summary
        item {
            IndustrialCard(
                borderColor = SteelBlue,
                containerColor = SteelBlue.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BBS DELIVERY WEIGHT SUMMARY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SteelBlue
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${bbsCalc.requiredPieces} pcs × ${bbsCalc.cutLengthM}m = ${bbsCalc.totalLengthM} Total Meters",
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Unit: ${bbsCalc.unitWeightKgPerM} kg/m • Bundles: ${bbsCalc.bundleCount} + ${bbsCalc.loosePieces} loose",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${bbsCalc.weightMt} MT",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SteelBlue,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${bbsCalc.weightKg} KG",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Inventory Stock Availability & Negative Check
        item {
            IndustrialCard(
                borderColor = if (isShortage) ScrapRed.copy(alpha = 0.6f) else StatusMatched.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SOURCE LOCATION & STOCK VERIFICATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Text(
                            text = "$sourceLocation • Batch: $batchNumber",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Available in Location: $availablePcs PCS (${availableKg} KG)",
                            fontSize = 11.sp,
                            color = if (isShortage) ScrapRed else StatusMatched,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isShortage) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ScrapRed.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ScrapRed)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = ScrapRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "STOCK SHORTAGE - NEGATIVE STOCK BLOCKED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ScrapRed
                                )
                            }
                            Text(
                                text = "Requested ${requiredPiecesInput} pcs exceeds available $availablePcs pcs. By default, negative stock is blocked.",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Supervisor Override Authorization", fontSize = 11.sp, color = TextPrimary)
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
                                    label = { Text("Required Override Justification") },
                                    placeholder = { Text("e.g. Urgent site pour - physical delivery confirmed") },
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                    colors = outlinedTextFieldColors()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Post Delivery Button
        item {
            Button(
                onClick = {
                    viewModel.postCustomerDelivery(
                        companyCode = selectedCompany,
                        sourceLocation = sourceLocation,
                        customerName = customerName,
                        projectName = projectName,
                        deliveryNote = deliveryNote,
                        bbsRef = bbsRef,
                        diameterMm = selectedDiameter,
                        cutLengthM = cutLengthInput.toDoubleOrNull() ?: 1.0,
                        grade = selectedGrade,
                        finish = selectedFinish,
                        batchNumber = batchNumber,
                        heatNumber = heatNumber,
                        requiredPieces = requiredPiecesInput.toIntOrNull() ?: 0,
                        allowNegativeOverride = allowNegativeOverride,
                        overrideReason = overrideReason,
                        onSuccess = { onNavigateBack() }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_post_delivery"),
                colors = ButtonDefaults.buttonColors(containerColor = SteelBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "POST BBS CUSTOMER DELIVERY",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
