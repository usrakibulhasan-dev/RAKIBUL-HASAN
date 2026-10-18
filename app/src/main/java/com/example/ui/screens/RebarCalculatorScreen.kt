package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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

@Composable
fun RebarCalculatorScreen(
    onNavigateToReceiving: () -> Unit,
    onNavigateToDelivery: () -> Unit
) {
    val diameterOptions = listOf(8, 10, 12, 14, 16, 18, 20, 22, 25, 32)
    var selectedDiameter by remember { mutableIntStateOf(12) }
    var selectedFinish by remember { mutableStateOf("BLACK") }
    var activeTab by remember { mutableIntStateOf(0) }

    // Mode 1: Weight -> Bundles & Pieces
    var weightInput by remember { mutableStateOf("10020") }
    var weightUnit by remember { mutableStateOf("KG") } // KG or MT
    var barLengthInput by remember { mutableStateOf("12.0") }

    // Mode 2: Pieces -> Bundles (2-Ton & BBS)
    var piecesInput by remember { mutableStateOf("940") }
    var bbsCutLengthInput by remember { mutableStateOf("5.80") }

    // Mode 3: Bundles -> Weight
    var fullBundlesInput by remember { mutableStateOf("5") }
    var loosePiecesInput by remember { mutableStateOf("0") }

    val pcsPer2Ton = RebarCalculator.getPiecesPer2TonBundle(selectedDiameter)
    val unitWeight = RebarCalculator.getUnitWeight(selectedDiameter, selectedFinish == "EPOXY_GREEN")
    val singleBarWeight = RebarCalculator.round((barLengthInput.toDoubleOrNull() ?: 12.0) * unitWeight, 3)

    // Live Result for Mode 1 (Weight -> Bundles)
    val weightToBundleResult by remember(weightInput, weightUnit, selectedDiameter, barLengthInput, selectedFinish) {
        derivedStateOf {
            val rawWeight = weightInput.toDoubleOrNull() ?: 0.0
            val weightKg = if (weightUnit == "MT") rawWeight * 1000.0 else rawWeight
            val len = barLengthInput.toDoubleOrNull() ?: 12.0
            RebarCalculator.calculateWeightTo2TonBundles(
                diameterMm = selectedDiameter,
                weightKg = weightKg,
                lengthM = len,
                isEpoxy = selectedFinish == "EPOXY_GREEN"
            )
        }
    }

    // Live Result for Mode 2 (Pieces -> 2-Ton & BBS)
    val piecesConversionResult by remember(piecesInput, selectedDiameter, barLengthInput, bbsCutLengthInput, selectedFinish) {
        derivedStateOf {
            val totalPcs = piecesInput.toIntOrNull() ?: 0
            val stdLen = barLengthInput.toDoubleOrNull() ?: 12.0
            val bbsCutLen = bbsCutLengthInput.toDoubleOrNull() ?: 5.80
            RebarCalculator.calculatePiecesTo2TonAndBbs(
                diameterMm = selectedDiameter,
                totalPieces = totalPcs,
                standardLengthM = stdLen,
                bbsCutLengthM = bbsCutLen,
                isEpoxy = selectedFinish == "EPOXY_GREEN"
            )
        }
    }

    // Live Result for Mode 3 (Bundles & Loose -> Weight)
    val bundlesToWeightResult by remember(fullBundlesInput, loosePiecesInput, selectedDiameter, barLengthInput, selectedFinish) {
        derivedStateOf {
            val b = fullBundlesInput.toIntOrNull() ?: 0
            val p = loosePiecesInput.toIntOrNull() ?: 0
            val len = barLengthInput.toDoubleOrNull() ?: 12.0
            RebarCalculator.calculateReceiving2Ton(
                diameterMm = selectedDiameter,
                lengthM = len,
                fullBundles = b,
                looseBundles = 0,
                loosePieces = p,
                isEpoxy = selectedFinish == "EPOXY_GREEN"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("rebar_calculator_screen")
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
                text = { Text("Weight ➔ Bundles", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Pieces ➔ Bundles & BBS", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = { Text("Bundles ➔ Weight", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Banner
            item {
                IndustrialCard(borderColor = IndustrialAmber.copy(alpha = 0.5f)) {
                    SectionHeader(
                        title = "Talai Al-Folath Rebar Calculator",
                        subtitle = "Receiving Standard: All as per 2 TON | Delivery Standard: Only as per BBS",
                        icon = Icons.Default.Calculate,
                        accentColor = IndustrialAmber
                    )
                }
            }

            // Diameter Selector
            item {
                IndustrialCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SELECT DIAMETER (MM)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Text(
                            text = "2-TON BUNDLE = $pcsPer2Ton PCS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialAmber,
                            fontFamily = FontFamily.Monospace
                        )
                    }
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
                            text = "Unit Weight: $unitWeight kg/m",
                            fontSize = 11.sp,
                            color = SteelBlue,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "12m Bar: $singleBarWeight kg",
                            fontSize = 11.sp,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Steel Finish (Black Steel vs Epoxy Green)
            item {
                IndustrialCard {
                    Text(text = "STEEL SURFACE FINISH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = selectedFinish == "BLACK",
                            onClick = { selectedFinish = "BLACK" },
                            label = { Text("Black Steel (Normal)") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BlackSteel.copy(alpha = 0.25f),
                                selectedLabelColor = TextPrimary
                            )
                        )
                        FilterChip(
                            selected = selectedFinish == "EPOXY_GREEN",
                            onClick = { selectedFinish = "EPOXY_GREEN" },
                            label = { Text("Epoxy Green (+1.5% Coated)") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EpoxyGreen.copy(alpha = 0.25f),
                                selectedLabelColor = EpoxyGreen
                            )
                        )
                    }
                }
            }

            // Tab 0: Input Weight -> Calculate Bundles & Pieces
            if (activeTab == 0) {
                item {
                    IndustrialCard {
                        Text(text = "INPUT WEIGHT TO CONVERT TO 2-TON BUNDLES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { weightInput = it },
                                label = { Text("Total Weight ($weightUnit)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(2f).testTag("calc_input_weight"),
                                colors = outlinedTextFieldColors()
                            )
                            Row(modifier = Modifier.weight(1.2f), verticalAlignment = Alignment.CenterVertically) {
                                FilterChip(
                                    selected = weightUnit == "KG",
                                    onClick = {
                                        if (weightUnit != "KG") {
                                            val current = weightInput.toDoubleOrNull() ?: 0.0
                                            weightInput = (current * 1000.0).toString()
                                            weightUnit = "KG"
                                        }
                                    },
                                    label = { Text("KG", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                FilterChip(
                                    selected = weightUnit == "MT",
                                    onClick = {
                                        if (weightUnit != "MT") {
                                            val current = weightInput.toDoubleOrNull() ?: 0.0
                                            weightInput = (current / 1000.0).toString()
                                            weightUnit = "MT"
                                        }
                                    },
                                    label = { Text("MT", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = barLengthInput,
                            onValueChange = { barLengthInput = it },
                            label = { Text("Standard Stock Bar Length (Meters)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = outlinedTextFieldColors()
                        )
                    }
                }

                // Output Result Card for Mode 1
                item {
                    IndustrialCard(
                        borderColor = IndustrialAmber,
                        containerColor = IndustrialAmber.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "CONVERTED 2-TON BUNDLE RECEIVING BREAKDOWN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialAmber
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMetric(
                                label = "FULL 2-TON BUNDLES",
                                value = "${weightToBundleResult.full2TonBundles}",
                                unit = "Bundles",
                                color = IndustrialAmber,
                                testTag = "calc_result_full_bundles"
                            )
                            StatMetric(
                                label = "LOOSE PIECES",
                                value = "${weightToBundleResult.loosePieces}",
                                unit = "PCS",
                                color = SteelBlue,
                                testTag = "calc_result_loose_pcs"
                            )
                            StatMetric(
                                label = "TOTAL PIECES",
                                value = "${weightToBundleResult.totalPieces}",
                                unit = "PCS",
                                color = TextPrimary,
                                testTag = "calc_result_total_pcs"
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F151F),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Standard 2T Bundle: $pcsPer2Ton pieces @ $singleBarWeight kg/bar",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Exact Calculated Weight: ${weightToBundleResult.exactCalculatedWeightKg} KG (${weightToBundleResult.exactCalculatedWeightMt} MT)",
                                    fontSize = 11.sp,
                                    color = EpoxyGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Difference from Input Scale Weight: ${weightToBundleResult.weightVarianceKg} KG",
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToReceiving,
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialAmber),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PUSH TO 2-TON RECEIVING VOUCHER", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Tab 1: Input Pieces -> Calculate 2-Ton & BBS
            else if (activeTab == 1) {
                item {
                    IndustrialCard {
                        Text(text = "INPUT PIECES TO CONVERT TO 2-TON & BBS DELIVERY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = piecesInput,
                                onValueChange = { piecesInput = it },
                                label = { Text("Total Pieces") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("calc_input_pieces"),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = bbsCutLengthInput,
                                onValueChange = { bbsCutLengthInput = it },
                                label = { Text("BBS Cut Length (m)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("calc_input_cut_length"),
                                colors = outlinedTextFieldColors()
                            )
                        }
                    }
                }

                // As per 2-Ton Standard Receiving Card
                item {
                    IndustrialCard(
                        borderColor = IndustrialAmber,
                        containerColor = IndustrialAmber.copy(alpha = 0.08f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(IndustrialAmber, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1. AS PER 2-TON STANDARD (RAW MATERIAL RECEIVING)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndustrialAmber
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMetric(
                                label = "TOTAL BUNDLES",
                                value = "${piecesConversionResult.receivingFractionalBundles}",
                                unit = "Bundles",
                                color = IndustrialAmber
                            )
                            StatMetric(
                                label = "BREAKDOWN",
                                value = "${piecesConversionResult.receivingFullBundles}B + ${piecesConversionResult.receivingLoosePieces}P",
                                unit = "Full+Loose",
                                color = TextPrimary
                            )
                            StatMetric(
                                label = "12M STOCK MT",
                                value = "${piecesConversionResult.receivingWeightMt}",
                                unit = "MT",
                                color = EpoxyGreen
                            )
                        }
                        Text(
                            text = "Standard 12m: ${piecesConversionResult.receivingWeightKg} KG | Exact: ${piecesConversionResult.receivingFractionalBundles} x 2-Ton Bundle (${piecesConversionResult.pcsPer2TonBundle} pcs/bundle)",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // As per BBS Delivery Card
                item {
                    IndustrialCard(
                        borderColor = SteelBlue,
                        containerColor = SteelBlue.copy(alpha = 0.08f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(SteelBlue, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "2. AS PER BBS DELIVERY (CUSTOMER ORDER SCHEDULE)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SteelBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMetric(
                                label = "BBS BUNDLES",
                                value = "${piecesConversionResult.bbsFractionalBundles}",
                                unit = "Bundles",
                                color = SteelBlue
                            )
                            StatMetric(
                                label = "TOTAL METERS",
                                value = "${piecesConversionResult.bbsTotalLengthM}",
                                unit = "M",
                                color = TextPrimary
                            )
                            StatMetric(
                                label = "DISPATCH WEIGHT",
                                value = "${piecesConversionResult.bbsWeightMt}",
                                unit = "MT",
                                color = SteelBlue
                            )
                        }
                        Text(
                            text = "BBS Delivery: ${piecesConversionResult.bbsWeightKg} KG (${piecesConversionResult.bbsBundleCount} bundles + ${piecesConversionResult.bbsLoosePieces} loose pcs @ ${piecesConversionResult.bbsCutLengthM}m cut)",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onNavigateToReceiving,
                                modifier = Modifier.weight(1f).height(42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IndustrialAmber),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Use in 2T Receiving", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onNavigateToDelivery,
                                modifier = Modifier.weight(1f).height(42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SteelBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Use in BBS Delivery", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Tab 2: Input Bundles & Loose -> Weight
            else {
                item {
                    IndustrialCard {
                        Text(text = "INPUT 2-TON BUNDLES & LOOSE PCS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = fullBundlesInput,
                                onValueChange = { fullBundlesInput = it },
                                label = { Text("Full 2T Bundles") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = loosePiecesInput,
                                onValueChange = { loosePiecesInput = it },
                                label = { Text("Loose Pieces") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                        }
                    }
                }

                item {
                    IndustrialCard(
                        borderColor = IndustrialAmber,
                        containerColor = IndustrialAmber.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "CALCULATED WEIGHT AS PER 2-TON STANDARD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialAmber
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMetric(
                                label = "TOTAL PIECES",
                                value = "${bundlesToWeightResult.totalPieces}",
                                unit = "PCS",
                                color = TextPrimary
                            )
                            StatMetric(
                                label = "WEIGHT (KG)",
                                value = "${bundlesToWeightResult.theoreticalWeightKg}",
                                unit = "KG",
                                color = SteelBlue
                            )
                            StatMetric(
                                label = "WEIGHT (MT)",
                                value = "${bundlesToWeightResult.theoreticalWeightMt}",
                                unit = "MT",
                                color = IndustrialAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onNavigateToReceiving,
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialAmber),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PUSH TO 2-TON RECEIVING VOUCHER", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }

            // Standard Reference Table
            item {
                IndustrialCard {
                    SectionHeader(
                        title = "Factory Rebar 2-Ton Reference Matrix",
                        subtitle = "Standard piece count per 2-ton bundle & unit weights (12m bar)",
                        icon = Icons.Default.Scale
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFF0F151F), RoundedCornerShape(4.dp)).padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dia", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text("Unit Wt", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text("2T Bundle", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text("12m Bar", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text("Nominal MT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    }

                    RebarCalculator.STANDARD_UNIT_WEIGHTS.forEach { (dia, uWt) ->
                        val pcs = RebarCalculator.getPiecesPer2TonBundle(dia)
                        val barWt = RebarCalculator.round(12.0 * uWt, 2)
                        val nomMt = RebarCalculator.round((pcs * barWt) / 1000.0, 3)
                        val isSelected = dia == selectedDiameter

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) IndustrialAmber.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { selectedDiameter = dia }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${dia}mm", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium, color = if (isSelected) IndustrialAmber else TextPrimary)
                            Text("$uWt kg/m", fontSize = 11.sp, color = SteelBlue, fontFamily = FontFamily.Monospace)
                            Text("$pcs pcs", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) IndustrialAmber else TextPrimary, fontFamily = FontFamily.Monospace)
                            Text("$barWt kg", fontSize = 11.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                            Text("$nomMt MT", fontSize = 11.sp, color = EpoxyGreen, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}
