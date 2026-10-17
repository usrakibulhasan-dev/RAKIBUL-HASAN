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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.ui.theme.BlackSteel
import com.example.ui.theme.EpoxyGreen
import com.example.ui.theme.IndustrialAmber
import com.example.ui.theme.SteelBlue
import com.example.ui.theme.SteelCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.InventoryViewModel

@Composable
fun StockSummaryScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit
) {
    val balances by viewModel.stockBalances.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFinishFilter by remember { mutableStateOf("ALL") }
    var selectedDiameterFilter by remember { mutableStateOf<Int?>(null) }

    val filteredBalances = remember(balances, searchQuery, selectedFinishFilter, selectedDiameterFilter) {
        balances.filter { b ->
            val matchesQuery = searchQuery.isBlank() ||
                b.batchNumber.contains(searchQuery, ignoreCase = true) ||
                b.heatNumber.contains(searchQuery, ignoreCase = true) ||
                b.locationCode.contains(searchQuery, ignoreCase = true) ||
                b.companyCode.contains(searchQuery, ignoreCase = true)

            val matchesFinish = selectedFinishFilter == "ALL" || b.finish == selectedFinishFilter
            val matchesDia = selectedDiameterFilter == null || b.diameterMm == selectedDiameterFilter

            matchesQuery && matchesFinish && matchesDia
        }
    }

    val totalFilteredMt = remember(filteredBalances) {
        filteredBalances.sumOf { it.balanceMt }
    }
    val totalFilteredPcs = remember(filteredBalances) {
        filteredBalances.sumOf { it.totalPieces }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("stock_summary_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            IndustrialCard(borderColor = IndustrialAmber.copy(alpha = 0.5f)) {
                SectionHeader(
                    title = "Factory Stock Summary & Balances",
                    subtitle = "Real-time stock by warehouse bay, diameter, grade, finish & batch",
                    icon = Icons.Default.Dashboard
                )
            }
        }

        // Search Bar & Filter Chips
        item {
            IndustrialCard {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by Batch, Heat, Location, or Company") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IndustrialAmber) },
                    modifier = Modifier.fillMaxWidth().testTag("input_stock_search"),
                    colors = outlinedTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFinishFilter == "ALL",
                        onClick = { selectedFinishFilter = "ALL" },
                        label = { Text("All Steel") }
                    )
                    FilterChip(
                        selected = selectedFinishFilter == "BLACK",
                        onClick = { selectedFinishFilter = "BLACK" },
                        label = { Text("Black Only") }
                    )
                    FilterChip(
                        selected = selectedFinishFilter == "EPOXY_GREEN",
                        onClick = { selectedFinishFilter = "EPOXY_GREEN" },
                        label = { Text("Epoxy Green") }
                    )
                }
            }
        }

        // Summary Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredBalances.size} Stock Records Found",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Total: ${String.format("%.3f", totalFilteredMt)} MT ($totalFilteredPcs PCS)",
                    fontSize = 13.sp,
                    color = IndustrialAmber,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Balances List
        items(filteredBalances) { bal ->
            IndustrialCard(
                modifier = Modifier.testTag("stock_item_${bal.id}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${bal.diameterMm}mm Rebar",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FinishBadge(finish = bal.finish)
                        }
                        Text(
                            text = "Loc: ${bal.locationCode} • Owner: ${bal.companyCode} (Origin: ${bal.supplierOrigin})",
                            fontSize = 11.sp,
                            color = IndustrialAmber,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = "Batch: ${bal.batchNumber} • Heat: ${bal.heatNumber} • ${bal.grade} • Length: ${bal.lengthM}m",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${bal.balanceMt} MT",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (bal.finish == "EPOXY_GREEN") EpoxyGreen else IndustrialAmber,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${bal.balanceKg} KG",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${bal.totalPieces} PCS (${bal.totalBundles} 2T Bnd)",
                            fontSize = 10.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
