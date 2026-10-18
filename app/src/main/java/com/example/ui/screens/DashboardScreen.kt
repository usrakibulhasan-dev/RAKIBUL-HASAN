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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserRole
import com.example.ui.theme.AlertWarning
import com.example.ui.theme.BlackSteel
import com.example.ui.theme.EpoxyGreen
import com.example.ui.theme.IndustrialAmber
import com.example.ui.theme.ScrapRed
import com.example.ui.theme.StatusMatched
import com.example.ui.theme.SteelBlue
import com.example.ui.theme.SteelBlueDark
import com.example.ui.theme.SteelCardBg
import com.example.ui.theme.SteelCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.InventoryViewModel

@Composable
fun DashboardScreen(
    viewModel: InventoryViewModel,
    onNavigate: (String) -> Unit
) {
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val ledger by viewModel.stockLedger.collectAsStateWithLifecycle()
    val userRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val userEmail by viewModel.currentUserEmail.collectAsStateWithLifecycle()
    val balances by viewModel.stockBalances.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Factory Plant Header Banner
        item {
            IndustrialCard(
                borderColor = IndustrialAmber.copy(alpha = 0.5f),
                containerColor = SteelCardBg
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(IndustrialAmber, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TALAI AL-FOLATH FOR MANUFACTURING CO.",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = IndustrialAmber,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "Steel Cut & Bend Factory ERP • Jeddah Central Plant",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SteelBlue.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SteelBlue.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = userRole.title,
                            color = SteelBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Primary KPI Metric Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Stock MT
                IndustrialCard(
                    modifier = Modifier.weight(1f),
                    borderColor = IndustrialAmber.copy(alpha = 0.3f)
                ) {
                    StatMetric(
                        label = "TOTAL STEEL STOCK",
                        value = "${metrics.totalStockMt}",
                        unit = "MT",
                        color = IndustrialAmber,
                        testTag = "kpi_total_stock"
                    )
                    Text(
                        text = "${metrics.totalPieces} Bars (${metrics.totalStockKg} KG)",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                // Black vs Epoxy breakdown
                IndustrialCard(
                    modifier = Modifier.weight(1f),
                    borderColor = EpoxyGreen.copy(alpha = 0.3f)
                ) {
                    Text(text = "STEEL GRADES & FINISH", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(BlackSteel, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Black: ${metrics.blackSteelMt} MT", fontSize = 12.sp, color = TextPrimary, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(EpoxyGreen, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Epoxy: ${metrics.epoxyGreenMt} MT", fontSize = 12.sp, color = EpoxyGreen, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Daily In / Out & Scrap KPIs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IndustrialCard(modifier = Modifier.weight(1f)) {
                    StatMetric(
                        label = "TODAY'S INFLOW",
                        value = "+${metrics.todayInflowMt}",
                        unit = "MT",
                        color = EpoxyGreen,
                        testTag = "kpi_today_in"
                    )
                    Text(text = "Raw Material Receiving", fontSize = 10.sp, color = TextMuted)
                }
                IndustrialCard(modifier = Modifier.weight(1f)) {
                    StatMetric(
                        label = "TODAY'S OUTFLOW",
                        value = "-${metrics.todayOutflowMt}",
                        unit = "MT",
                        color = SteelBlue,
                        testTag = "kpi_today_out"
                    )
                    Text(text = "Dispatched Delivery", fontSize = 10.sp, color = TextMuted)
                }
                IndustrialCard(modifier = Modifier.weight(1f)) {
                    StatMetric(
                        label = "SCRAP IN BIN",
                        value = "${metrics.scrapStockKg}",
                        unit = "KG",
                        color = ScrapRed,
                        testTag = "kpi_scrap"
                    )
                    Text(text = "Factory trimmings", fontSize = 10.sp, color = TextMuted)
                }
            }
        }

        // Rebar Calculator Hero Feature Card
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
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(IndustrialAmber, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "2-TON & BBS REBAR CALCULATOR",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = IndustrialAmber
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Input Weight (KG/MT) ➔ Get Full 2T Bundles & Loose Pieces\n• Input Pieces ➔ Get 2-Ton Receiving & BBS Delivery MT",
                            fontSize = 11.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                    }

                    Button(
                        onClick = { onNavigate("calculator") },
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialAmber),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_hero_calculator")
                    ) {
                        Text("Calculate", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 11.sp)
                    }
                }
            }
        }

        // Quick Operations Action Row
        item {
            SectionHeader(title = "Factory Operations & Actions", icon = Icons.Default.Build)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Calculate,
                    label = "Calculator",
                    accent = IndustrialAmber,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_nav_calculator"
                ) { onNavigate("calculator") }

                QuickActionButton(
                    icon = Icons.Default.AddShoppingCart,
                    label = "Receive 2T",
                    accent = IndustrialAmber,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_nav_receive"
                ) { onNavigate("receiving") }

                QuickActionButton(
                    icon = Icons.Default.LocalShipping,
                    label = "BBS Delivery",
                    accent = SteelBlue,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_nav_delivery"
                ) { onNavigate("delivery") }

                QuickActionButton(
                    icon = Icons.Default.Assessment,
                    label = "Raw Reports",
                    accent = EpoxyGreen,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_nav_raw_reports"
                ) { onNavigate("reports") }
            }
        }

        // Secondary Action Row (Recon, Import/Export, Summary, Ledger)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.FactCheck,
                    label = "Reconcile",
                    accent = Color(0xFFA855F7),
                    modifier = Modifier.weight(1f),
                    testTag = "btn_nav_reconcile"
                ) { onNavigate("reconciliation") }

                QuickActionButton(
                    icon = Icons.Default.UploadFile,
                    label = "MASAR Excel",
                    accent = Color(0xFF06B6D4),
                    modifier = Modifier.weight(1f),
                    testTag = "btn_nav_excel"
                ) { onNavigate("excel") }

                QuickActionButton(
                    icon = Icons.Default.Dashboard,
                    label = "Stock Balances",
                    accent = Color(0xFFF97316),
                    modifier = Modifier.weight(1f),
                    testTag = "btn_nav_stock"
                ) { onNavigate("stock_summary") }

                QuickActionButton(
                    icon = Icons.Default.CompareArrows,
                    label = "Ledger Audit",
                    accent = Color(0xFF64748B),
                    modifier = Modifier.weight(1f),
                    testTag = "btn_nav_ledger"
                ) { onNavigate("ledger") }
            }
        }

        // Reconciliation & Verification Status Banner
        item {
            IndustrialCard(
                borderColor = if (metrics.reconMismatchedCount > 0 || metrics.reconMissingCount > 0)
                    AlertWarning.copy(alpha = 0.5f) else StatusMatched.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MASAR INVENTORY RECONCILIATION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${metrics.reconMatchedCount} Matched • ${metrics.reconMismatchedCount} Mismatched • ${metrics.reconMissingCount} Missing",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Button(
                        onClick = { onNavigate("reconciliation") },
                        colors = ButtonDefaults.buttonColors(containerColor = SteelBlueDark),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("View Audit", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }

        // Recent Posted Movements Preview
        item {
            SectionHeader(
                title = "Recent Material Movements",
                subtitle = "Double-entry immutable stock ledger entries"
            )
        }

        items(ledger.take(4)) { entry ->
            IndustrialCard(
                modifier = Modifier.testTag("ledger_card_${entry.docNumber}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = entry.docNumber,
                                fontWeight = FontWeight.Bold,
                                color = IndustrialAmber,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FinishBadge(finish = entry.finish)
                        }
                        Text(
                            text = "${entry.diameterMm}mm • ${entry.grade} • ${entry.pieces} pcs • Batch ${entry.batchNumber}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = "From: ${entry.fromLocation ?: "External"} -> To: ${entry.toLocation ?: "Customer"}",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${entry.weightMt} MT",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = entry.transactionDate,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        StatusBadge(status = entry.status, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SteelCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, SteelCardBorder),
        modifier = modifier
            .testTag(testTag)
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(accent.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}
