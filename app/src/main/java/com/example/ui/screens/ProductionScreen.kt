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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.calculator.RebarCalculator
import com.example.ui.theme.AlertWarning
import com.example.ui.theme.EpoxyGreen
import com.example.ui.theme.IndustrialAmber
import com.example.ui.theme.ScrapRed
import com.example.ui.theme.SteelBlue
import com.example.ui.theme.SteelCardBg
import com.example.ui.theme.SteelCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.InventoryViewModel

@Composable
fun ProductionScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit
) {
    val orders by viewModel.manufacturingOrders.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // New Production Posting State
    var orderNumber by remember { mutableStateOf("MO-2026-0042") }
    var voucherRef by remember { mutableStateOf("MFG-VOUCHER-088") }
    var rawLocation by remember { mutableStateOf("RM-BAY-01") }
    var fgLocation by remember { mutableStateOf("FG-YARD-A") }
    var offcutLocation by remember { mutableStateOf("OFFCUT-RACK") }
    var scrapLocation by remember { mutableStateOf("SCRAP-BIN") }

    var selectedDiameter by remember { mutableIntStateOf(16) }
    var rawLengthInput by remember { mutableStateOf("12.0") }
    var cutLengthInput by remember { mutableStateOf("5.80") }
    var consumedRawPcsInput by remember { mutableStateOf("50") }
    var producedFgPcsInput by remember { mutableStateOf("100") }
    var offcutKgInput by remember { mutableStateOf("25.0") }
    var scrapKgInput by remember { mutableStateOf("18.5") }

    var selectedGrade by remember { mutableStateOf("Grade 60") }
    var selectedFinish by remember { mutableStateOf("BLACK") }
    var companyCode by remember { mutableStateOf("TALAI") }
    var batchNumber by remember { mutableStateOf("B-2026-1025") }
    var heatNumber by remember { mutableStateOf("HT-99312") }

    // Live Yield Calculation
    val yieldCalc by remember(selectedDiameter, rawLengthInput, cutLengthInput, consumedRawPcsInput, producedFgPcsInput, offcutKgInput, scrapKgInput, selectedFinish) {
        derivedStateOf {
            val unitWeight = RebarCalculator.getUnitWeight(selectedDiameter, selectedFinish == "EPOXY_GREEN")
            val rawLen = rawLengthInput.toDoubleOrNull() ?: 12.0
            val cutLen = cutLengthInput.toDoubleOrNull() ?: 1.0
            val rawPcs = consumedRawPcsInput.toIntOrNull() ?: 0
            val fgPcs = producedFgPcsInput.toIntOrNull() ?: 0
            val offKg = offcutKgInput.toDoubleOrNull() ?: 0.0
            val scrKg = scrapKgInput.toDoubleOrNull() ?: 0.0

            val inputKg = rawPcs * rawLen * unitWeight
            val fgKg = fgPcs * cutLen * unitWeight

            RebarCalculator.calculateProductionYield(
                inputRawKg = inputKg,
                finishedGoodsKg = fgKg,
                offcutKg = offKg,
                scrapKg = scrKg
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize().testTag("production_screen")) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = SteelCardBg,
            contentColor = IndustrialAmber,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = IndustrialAmber
                )
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Post Production Output", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Manufacturing Orders (${orders.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        if (selectedTabIndex == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    IndustrialCard(borderColor = EpoxyGreen.copy(alpha = 0.5f)) {
                        SectionHeader(
                            title = "Cut & Bend Production Posting",
                            subtitle = "Deducts consumed raw bars; posts FG, off-cuts, and scrap atomically",
                            icon = Icons.Default.Build,
                            accentColor = EpoxyGreen
                        )
                    }
                }

                // Order & Voucher Reference
                item {
                    IndustrialCard {
                        Text(text = "ORDER & MANUFACTURING VOUCHER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = orderNumber,
                                onValueChange = { orderNumber = it },
                                label = { Text("Mfg Order No.") },
                                modifier = Modifier.weight(1f).testTag("input_mfg_order_no"),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = voucherRef,
                                onValueChange = { voucherRef = it },
                                label = { Text("Voucher Ref") },
                                modifier = Modifier.weight(1f).testTag("input_mfg_voucher_ref"),
                                colors = outlinedTextFieldColors()
                            )
                        }
                    }
                }

                // Raw Material Consumption
                item {
                    IndustrialCard {
                        Text(text = "RAW MATERIAL CONSUMPTION (INPUT)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = rawLocation,
                                onValueChange = { rawLocation = it },
                                label = { Text("Raw Location") },
                                modifier = Modifier.weight(1f).testTag("input_raw_location"),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = consumedRawPcsInput,
                                onValueChange = { consumedRawPcsInput = it },
                                label = { Text("Consumed 12m Bars") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("input_consumed_raw_pcs"),
                                colors = outlinedTextFieldColors()
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Input Weight: ${yieldCalc.inputRawKg} KG (${yieldCalc.inputRawMt} MT) of ${selectedDiameter}mm",
                            fontSize = 11.sp,
                            color = IndustrialAmber,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Finished Goods Output
                item {
                    IndustrialCard {
                        Text(text = "FINISHED GOODS OUTPUT (CUT & BENT REBAR)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = fgLocation,
                                onValueChange = { fgLocation = it },
                                label = { Text("FG Ready Location") },
                                modifier = Modifier.weight(1f).testTag("input_fg_location"),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = cutLengthInput,
                                onValueChange = { cutLengthInput = it },
                                label = { Text("Cut Length (m)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = producedFgPcsInput,
                                onValueChange = { producedFgPcsInput = it },
                                label = { Text("Produced PCS") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("input_produced_fg_pcs"),
                                colors = outlinedTextFieldColors()
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "FG Output Weight: ${yieldCalc.finishedGoodsKg} KG (${yieldCalc.finishedGoodsMt} MT)",
                            fontSize = 11.sp,
                            color = EpoxyGreen,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Off-cuts & Scrap
                item {
                    IndustrialCard {
                        Text(text = "OFF-CUT RECOVERY & SCRAP TRIMMINGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = offcutKgInput,
                                onValueChange = { offcutKgInput = it },
                                label = { Text("Off-cut Usable (KG)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("input_offcut_kg"),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = scrapKgInput,
                                onValueChange = { scrapKgInput = it },
                                label = { Text("Scrap Trimming (KG)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("input_scrap_kg"),
                                colors = outlinedTextFieldColors()
                            )
                        }
                    }
                }

                // Yield & Scrap Performance Card
                item {
                    IndustrialCard(
                        borderColor = IndustrialAmber,
                        containerColor = IndustrialAmber.copy(alpha = 0.08f)
                    ) {
                        Text(text = "PRODUCTION YIELD & VARIANCE ANALYSIS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IndustrialAmber)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMetric(label = "INPUT RAW", value = "${yieldCalc.inputRawKg}", unit = "KG")
                            StatMetric(label = "OUTPUT FG", value = "${yieldCalc.finishedGoodsKg}", unit = "KG", color = EpoxyGreen)
                            StatMetric(label = "OFF-CUT", value = "${yieldCalc.offcutKg}", unit = "KG", color = SteelBlue)
                            StatMetric(label = "SCRAP RATE", value = "${yieldCalc.scrapPercentage}%", color = ScrapRed)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Unaccounted Production Variance: ${yieldCalc.varianceKg} KG",
                            fontSize = 11.sp,
                            color = if (yieldCalc.varianceKg == 0.0) EpoxyGreen else AlertWarning,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Post Button
                item {
                    Button(
                        onClick = {
                            viewModel.postProduction(
                                orderNumber = orderNumber,
                                rawLocation = rawLocation,
                                fgLocation = fgLocation,
                                offcutLocation = offcutLocation,
                                scrapLocation = scrapLocation,
                                diameterMm = selectedDiameter,
                                rawLengthM = rawLengthInput.toDoubleOrNull() ?: 12.0,
                                cutLengthM = cutLengthInput.toDoubleOrNull() ?: 1.0,
                                grade = selectedGrade,
                                finish = selectedFinish,
                                batchNumber = batchNumber,
                                heatNumber = heatNumber,
                                companyCode = companyCode,
                                consumedRawPieces = consumedRawPcsInput.toIntOrNull() ?: 0,
                                producedFgPieces = producedFgPcsInput.toIntOrNull() ?: 0,
                                producedOffcutKg = offcutKgInput.toDoubleOrNull() ?: 0.0,
                                producedScrapKg = scrapKgInput.toDoubleOrNull() ?: 0.0,
                                voucherRef = voucherRef,
                                onSuccess = { onNavigateBack() }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_post_production"),
                        colors = ButtonDefaults.buttonColors(containerColor = EpoxyGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "POST PRODUCTION & UPDATE INVENTORY",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        } else {
            // Tab 1: Orders List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(orders) { order ->
                    IndustrialCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = order.orderNumber,
                                        fontWeight = FontWeight.Bold,
                                        color = IndustrialAmber,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadge(status = order.status)
                                }
                                Text(
                                    text = "${order.customer} • ${order.project}",
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = "BBS: ${order.bbsRef} • ${order.diameterMm}mm • ${order.requiredPieces} pcs @ ${order.cutLengthM}m",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${order.plannedRawKg} KG",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary
                                )
                                Text(text = "Planned Raw", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}
