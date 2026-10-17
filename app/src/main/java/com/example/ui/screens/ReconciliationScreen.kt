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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import com.example.ui.theme.AlertWarning
import com.example.ui.theme.IndustrialAmber
import com.example.ui.theme.ScrapRed
import com.example.ui.theme.StatusMatched
import com.example.ui.theme.SteelBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.InventoryViewModel

@Composable
fun ReconciliationScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit
) {
    val records by viewModel.reconciliationRecords.collectAsStateWithLifecycle()
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    val filteredRecords = remember(records, selectedStatusFilter) {
        if (selectedStatusFilter == "ALL") records else records.filter { it.status == selectedStatusFilter }
    }

    val matchedCount = records.count { it.status == "MATCHED" }
    val mismatchedCount = records.count { it.status == "MISMATCHED" }
    val missingCount = records.count { it.status == "MISSING" }
    val duplicateCount = records.count { it.status == "DUPLICATE" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reconciliation_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            IndustrialCard(borderColor = Color(0xFFA855F7).copy(alpha = 0.5f)) {
                SectionHeader(
                    title = "MASAR Inventory Reconciliation Center",
                    subtitle = "Audits MASAR DNs, batches, black vs epoxy yield, and physical count variances",
                    icon = Icons.Default.FactCheck,
                    accentColor = Color(0xFFA855F7)
                )
            }
        }

        // Run Audit Button
        item {
            Button(
                onClick = { viewModel.runReconciliationBatch() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_run_recon_audit"),
                colors = ButtonDefaults.buttonColors(containerColor = SteelBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RUN LIVE MASAR RECONCILIATION AUDIT", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // Summary Metric Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Triple("ALL", "All (${records.size})", TextPrimary),
                    Triple("MATCHED", "Matched ($matchedCount)", StatusMatched),
                    Triple("MISMATCHED", "Mismatch ($mismatchedCount)", AlertWarning),
                    Triple("MISSING", "Missing ($missingCount)", ScrapRed)
                ).forEach { (status, label, color) ->
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { selectedStatusFilter = status },
                        label = { Text(label, fontSize = 11.sp, color = if (selectedStatusFilter == status) color else TextSecondary) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Reconciliation Cards
        items(filteredRecords) { r ->
            IndustrialCard(
                modifier = Modifier.testTag("recon_record_${r.id}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "[${r.reconciliationType}] ${r.referenceKey}",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadge(status = r.status)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "MASAR Source: ${r.masarValue}",
                            fontSize = 11.sp,
                            color = SteelBlue,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Factory App: ${r.appValue}",
                            fontSize = 11.sp,
                            color = IndustrialAmber,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = "Audit Note: ${r.details}",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (r.varianceKg >= 0) "+${r.varianceKg} KG" else "${r.varianceKg} KG",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (r.status == "MATCHED") StatusMatched else ScrapRed,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Variance",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}
