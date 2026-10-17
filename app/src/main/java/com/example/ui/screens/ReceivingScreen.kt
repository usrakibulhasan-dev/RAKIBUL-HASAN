package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivingScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit
) {
    val companies by viewModel.companies.collectAsStateWithLifecycle()
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val locations by viewModel.locations.collectAsStateWithLifecycle()

    val diameterOptions = listOf(8, 10, 12, 14, 16, 18, 20, 22, 25, 32)
    var selectedDiameter by remember { mutableIntStateOf(12) }
    var lengthInput by remember { mutableStateOf("12.0") }
    var fullBundlesInput by remember { mutableStateOf("5") }
    var looseBundlesInput by remember { mutableStateOf("0") }
    var loosePiecesInput by remember { mutableStateOf("0") }

    var selectedFinish by remember { mutableStateOf("BLACK") }
    var selectedGrade by remember { mutableStateOf("Grade 60") }
    var selectedCompany by remember { mutableStateOf("TALAI") }
    var selectedSupplier by remember { mutableStateOf("MITWALLI") }
    var selectedLocation by remember { mutableStateOf("RM-BAY-01") }

    var poNumber by remember { mutableStateOf("PO-2026-9041") }
    var dnNumber by remember { mutableStateOf("DN-MIT-8842") }
    var batchNumber by remember { mutableStateOf("B-2026-1040") }
    var heatNumber by remember { mutableStateOf("HT-99812") }
    var actualWeightInput by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val rawMaterialLocations = remember(locations) {
        locations.filter { it.category == "RAW_MATERIAL" || it.category == "EPOXY_YARD" }.ifEmpty {
            locations
        }
    }

    // Live Calculation
    val calcResult by remember(selectedDiameter, lengthInput, fullBundlesInput, looseBundlesInput, loosePiecesInput, selectedFinish, actualWeightInput) {
        derivedStateOf {
            val length = lengthInput.toDoubleOrNull() ?: 12.0
            val fullB = fullBundlesInput.toIntOrNull() ?: 0
            val looseB = looseBundlesInput.toIntOrNull() ?: 0
            val looseP = loosePiecesInput.toIntOrNull() ?: 0
            val actualWeight = actualWeightInput.toDoubleOrNull()

            RebarCalculator.calculateReceiving2Ton(
                diameterMm = selectedDiameter,
                lengthM = length,
                fullBundles = fullB,
                looseBundles = looseB,
                loosePieces = looseP,
                isEpoxy = selectedFinish == "EPOXY_GREEN",
                actualWeightKg = actualWeight
            )
        }
    }

    val pcsPerBundle = RebarCalculator.getPiecesPer2TonBundle(selectedDiameter)
    val unitWeight = RebarCalculator.getUnitWeight(selectedDiameter, selectedFinish == "EPOXY_GREEN")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("receiving_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            IndustrialCard(borderColor = IndustrialAmber.copy(alpha = 0.5f)) {
                SectionHeader(
                    title = "Raw Material Receiving (2-TON Engine)",
                    subtitle = "Factory standard: Calculator Issued 2 TON QTY rule",
                    icon = Icons.Default.Calculate
                )
            }
        }

        // Diameter & Rebar Spec Selection
        item {
            IndustrialCard {
                Text(
                    text = "REBAR DIAMETER SPECIFICATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Standard 2T Bundle: $pcsPerBundle PCS",
                        fontSize = 11.sp,
                        color = IndustrialAmber,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Unit Weight: $unitWeight kg/m",
                        fontSize = 11.sp,
                        color = SteelBlue,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Finish & Grade Selection
        item {
            IndustrialCard {
                Text(text = "SURFACE FINISH & STEEL GRADE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = selectedFinish == "BLACK",
                        onClick = { selectedFinish = "BLACK" },
                        label = { Text("Black Steel (Normal)") },
                        modifier = Modifier.weight(1f).testTag("chip_finish_black"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BlackSteel.copy(alpha = 0.25f),
                            selectedLabelColor = TextPrimary
                        )
                    )
                    FilterChip(
                        selected = selectedFinish == "EPOXY_GREEN",
                        onClick = { selectedFinish = "EPOXY_GREEN" },
                        label = { Text("Epoxy Green (+1.5%)") },
                        modifier = Modifier.weight(1f).testTag("chip_finish_epoxy"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EpoxyGreen.copy(alpha = 0.25f),
                            selectedLabelColor = EpoxyGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Grade 60", "B500B", "ASTM A615").forEach { grade ->
                        FilterChip(
                            selected = selectedGrade == grade,
                            onClick = { selectedGrade = grade },
                            label = { Text(grade, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Bundle & Quantity Inputs
        item {
            IndustrialCard {
                Text(text = "QUANTITY & BUNDLE INPUTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fullBundlesInput,
                        onValueChange = { fullBundlesInput = it },
                        label = { Text("Full 2T Bundles") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_full_bundles"),
                        colors = outlinedTextFieldColors()
                    )
                    OutlinedTextField(
                        value = loosePiecesInput,
                        onValueChange = { loosePiecesInput = it },
                        label = { Text("Loose PCS") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_loose_pcs"),
                        colors = outlinedTextFieldColors()
                    )
                    OutlinedTextField(
                        value = lengthInput,
                        onValueChange = { lengthInput = it },
                        label = { Text("Bar Length (m)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("input_bar_length"),
                        colors = outlinedTextFieldColors()
                    )
                }
            }
        }

        // Live Theoretical Calculation Card
        item {
            IndustrialCard(
                borderColor = IndustrialAmber,
                containerColor = IndustrialAmber.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CALCULATED RECEIVING SUMMARY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialAmber
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${calcResult.totalPieces} Pieces (${fullBundlesInput} Bundles × $pcsPerBundle + $loosePiecesInput loose)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${calcResult.theoreticalWeightMt} MT",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = IndustrialAmber,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${calcResult.theoreticalWeightKg} KG",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Weighbridge Actual Scale Weight & Variance Check
        item {
            IndustrialCard {
                SectionHeader(
                    title = "Weighbridge Scale Ticket (Optional)",
                    subtitle = "Compare theoretical formula vs physical weighbridge",
                    icon = Icons.Default.Scale
                )
                OutlinedTextField(
                    value = actualWeightInput,
                    onValueChange = { actualWeightInput = it },
                    label = { Text("Actual Scale Measured Weight (KG)") },
                    placeholder = { Text("e.g. 10020.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("input_actual_weight"),
                    colors = outlinedTextFieldColors()
                )

                if (calcResult.weightVarianceKg != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val vKg = calcResult.weightVarianceKg ?: 0.0
                    val vPct = calcResult.weightVariancePercent ?: 0.0
                    val isTolerable = kotlin.math.abs(vPct) <= 1.0

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isTolerable) StatusMatched.copy(alpha = 0.15f) else ScrapRed.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isTolerable) StatusMatched else ScrapRed
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isTolerable) "SCALE WEIGHT WITHIN NORMAL TOLERANCE" else "SCALE VARIANCE WARNING",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTolerable) StatusMatched else ScrapRed
                                )
                                Text(
                                    text = "Scale: ${calcResult.actualWeightKg} KG vs Theoretical: ${calcResult.theoreticalWeightKg} KG",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "${if (vKg >= 0) "+$vKg" else "$vKg"} KG (${vPct}%)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTolerable) StatusMatched else ScrapRed,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Ownership, Supplier & Warehouse Location
        item {
            IndustrialCard {
                Text(text = "OWNERSHIP, SUPPLIER & LOCATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                // Company Ownership & Supplier Origin (Separate fields as mandated!)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = selectedCompany,
                        onValueChange = { selectedCompany = it },
                        label = { Text("Ownership") },
                        modifier = Modifier.weight(1f).testTag("input_company_owner"),
                        colors = outlinedTextFieldColors()
                    )
                    OutlinedTextField(
                        value = selectedSupplier,
                        onValueChange = { selectedSupplier = it },
                        label = { Text("Supplier Origin") },
                        modifier = Modifier.weight(1f).testTag("input_supplier_origin"),
                        colors = outlinedTextFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = selectedLocation,
                    onValueChange = { selectedLocation = it },
                    label = { Text("Destination Location (e.g. RM-BAY-01)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_location"),
                    colors = outlinedTextFieldColors()
                )
            }
        }

        // PO, Delivery Note, Batch & Heat Numbers
        item {
            IndustrialCard {
                Text(text = "DOCUMENT REFERENCE & TRACEABILITY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = poNumber,
                        onValueChange = { poNumber = it },
                        label = { Text("Purchase Order (PO)") },
                        modifier = Modifier.weight(1f).testTag("input_po_number"),
                        colors = outlinedTextFieldColors()
                    )
                    OutlinedTextField(
                        value = dnNumber,
                        onValueChange = { dnNumber = it },
                        label = { Text("Supplier Delivery Note (DN)") },
                        modifier = Modifier.weight(1f).testTag("input_dn_number"),
                        colors = outlinedTextFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = batchNumber,
                        onValueChange = { batchNumber = it },
                        label = { Text("Batch Number") },
                        modifier = Modifier.weight(1f).testTag("input_batch_number"),
                        colors = outlinedTextFieldColors()
                    )
                    OutlinedTextField(
                        value = heatNumber,
                        onValueChange = { heatNumber = it },
                        label = { Text("Heat Number (Mill)") },
                        modifier = Modifier.weight(1f).testTag("input_heat_number"),
                        colors = outlinedTextFieldColors()
                    )
                }
            }
        }

        // Action Button: Post Receiving
        item {
            Button(
                onClick = {
                    viewModel.postReceiving(
                        companyCode = selectedCompany,
                        supplierOrigin = selectedSupplier,
                        destinationLocation = selectedLocation,
                        diameterMm = selectedDiameter,
                        lengthM = lengthInput.toDoubleOrNull() ?: 12.0,
                        grade = selectedGrade,
                        finish = selectedFinish,
                        fullBundles = fullBundlesInput.toIntOrNull() ?: 0,
                        looseBundles = looseBundlesInput.toIntOrNull() ?: 0,
                        loosePieces = loosePiecesInput.toIntOrNull() ?: 0,
                        batchNumber = batchNumber,
                        heatNumber = heatNumber,
                        poNumber = poNumber,
                        dnNumber = dnNumber,
                        actualWeightKg = actualWeightInput.toDoubleOrNull(),
                        notes = notes,
                        onSuccess = { onNavigateBack() }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_post_receiving"),
                colors = ButtonDefaults.buttonColors(containerColor = IndustrialAmber),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Done, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "POST 2-TON RECEIVING VOUCHER",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun DiameterChip(
    diameter: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) IndustrialAmber else SteelCardBg,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) IndustrialAmber else SteelCardBorder
        ),
        modifier = modifier
            .testTag("diameter_chip_$diameter")
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = "${diameter}mm",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.Black else TextPrimary
            )
        }
    }
}

@Composable
fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = IndustrialAmber,
    unfocusedBorderColor = SteelCardBorder,
    focusedLabelColor = IndustrialAmber,
    unfocusedLabelColor = TextSecondary,
    cursorColor = IndustrialAmber,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)
