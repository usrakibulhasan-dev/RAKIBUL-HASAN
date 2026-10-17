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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.calculator.RebarCalculator
import com.example.data.model.UserRole
import com.example.ui.theme.AlertWarning
import com.example.ui.theme.IndustrialAmber
import com.example.ui.theme.SteelBlue
import com.example.ui.theme.SteelCardBg
import com.example.ui.theme.SteelCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminSettingsScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit
) {
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val currentUserEmail by viewModel.currentUserEmail.collectAsStateWithLifecycle()
    val companies by viewModel.companies.collectAsStateWithLifecycle()
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().testTag("admin_settings_screen")) {
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
                text = { Text("User & Roles", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Factory Masters", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Audit Trail", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (selectedTab == 0) {
                // Roles & Access Control
                item {
                    IndustrialCard(borderColor = IndustrialAmber.copy(alpha = 0.5f)) {
                        SectionHeader(
                            title = "Role-Based Access Control (RBAC)",
                            subtitle = "Switch system role to test permissions and operational authorities",
                            icon = Icons.Default.AdminPanelSettings
                        )
                    }
                }

                item {
                    IndustrialCard {
                        Text(text = "CURRENT ACTIVE USER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(IndustrialAmber, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$currentUserEmail (${currentUserRole.title})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                item {
                    Text(text = "SELECT USER ROLE PROFILE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                }

                items(UserRole.values()) { role ->
                    val isSelected = role == currentUserRole
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) IndustrialAmber.copy(alpha = 0.15f) else SteelCardBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) IndustrialAmber else SteelCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("role_option_${role.name}")
                            .clickable { viewModel.switchRole(role) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = role.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) IndustrialAmber else TextPrimary
                                )
                                Text(
                                    text = when (role) {
                                        UserRole.ADMIN -> "Full system control, masters, user accounts & overrides"
                                        UserRole.INVENTORY_CONTROLLER -> "Stock management, approvals, reconciliation & reports"
                                        UserRole.WAREHOUSE_SUPERVISOR -> "Supervises bay transfers, cutting lines & overrides"
                                        UserRole.STORE_KEEPER -> "Data entry for receiving, delivery & physical count"
                                        UserRole.AUDITOR -> "Read-only access to ledger, reconciliation & reports"
                                    },
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = IndustrialAmber
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == 1) {
                // Factory Masters
                item {
                    IndustrialCard {
                        SectionHeader(title = "Rebar 2-Ton Standard Specification Matrix", icon = Icons.Default.Settings)
                        Spacer(modifier = Modifier.height(6.dp))
                        RebarCalculator.STANDARD_UNIT_WEIGHTS.forEach { (dia, uWt) ->
                            val pcs = RebarCalculator.getPiecesPer2TonBundle(dia)
                            val barWt = RebarCalculator.round(12.0 * uWt, 2)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "${dia}mm Rebar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(text = "$uWt kg/m", fontSize = 12.sp, color = SteelBlue, fontFamily = FontFamily.Monospace)
                                Text(text = "$pcs pcs/2T bnd", fontSize = 12.sp, color = IndustrialAmber, fontFamily = FontFamily.Monospace)
                                Text(text = "12m: $barWt kg", fontSize = 12.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                item {
                    IndustrialCard {
                        SectionHeader(title = "Companies & Ownership (${companies.size})")
                        Spacer(modifier = Modifier.height(4.dp))
                        companies.forEach { c ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = c.code, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IndustrialAmber)
                                Text(text = c.name, fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                item {
                    IndustrialCard {
                        SectionHeader(title = "Warehouse Locations (${locations.size})")
                        Spacer(modifier = Modifier.height(4.dp))
                        locations.forEach { loc ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = loc.code, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(text = "${loc.name} (${loc.category})", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            } else {
                // Audit Trail
                item {
                    IndustrialCard(borderColor = SteelBlue.copy(alpha = 0.5f)) {
                        SectionHeader(
                            title = "Immutable System Audit Trail",
                            subtitle = "Tracks every inventory modification, posting, and override",
                            icon = Icons.Default.Security,
                            accentColor = SteelBlue
                        )
                    }
                }

                items(auditLogs) { log ->
                    IndustrialCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.action,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndustrialAmber,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = log.summary,
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = "By: ${log.userEmail} (${log.userRole})",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                            Text(
                                text = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(log.timestamp)),
                                fontSize = 10.sp,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
