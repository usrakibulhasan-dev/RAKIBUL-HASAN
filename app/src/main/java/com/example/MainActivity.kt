package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AdminSettingsScreen
import com.example.ui.screens.BbsDeliveryScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExcelImportExportScreen
import com.example.ui.screens.ProductionScreen
import com.example.ui.screens.ReceivingScreen
import com.example.ui.screens.ReconciliationScreen
import com.example.ui.screens.StockLedgerScreen
import com.example.ui.screens.StockSummaryScreen
import com.example.ui.screens.TransferAdjustmentScreen
import com.example.ui.theme.EpoxyGreen
import com.example.ui.theme.IndustrialAmber
import com.example.ui.theme.SteelBlue
import com.example.ui.theme.SteelCardBg
import com.example.ui.theme.SteelCardBorder
import com.example.ui.theme.SteelDarkBg
import com.example.ui.theme.SteelDarkSurface
import com.example.ui.theme.TalaiSteelTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.InventoryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TalaiSteelTheme(darkTheme = true) {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val viewModel: InventoryViewModel = viewModel()
    var currentScreen by remember { mutableStateOf("dashboard") }
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val userRole by viewModel.currentUserRole.collectAsStateWithLifecycle()

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Handle back button when navigating into sub-screens
    if (currentScreen != "dashboard") {
        BackHandler {
            currentScreen = "dashboard"
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = SteelDarkSurface
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(IndustrialAmber, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TALAI STEEL ERP",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = IndustrialAmber,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                navigationIcon = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SteelBlue.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SteelBlue.copy(alpha = 0.5f)),
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text(
                            text = userRole.name.replace("_", " "),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SteelBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { currentScreen = "admin" },
                        modifier = Modifier.testTag("top_btn_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings & RBAC",
                            tint = if (currentScreen == "admin") IndustrialAmber else TextSecondary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SteelDarkSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == "dashboard",
                    onClick = { currentScreen = "dashboard" },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Home", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_home"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IndustrialAmber,
                        selectedTextColor = IndustrialAmber,
                        indicatorColor = IndustrialAmber.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentScreen == "receiving",
                    onClick = { currentScreen = "receiving" },
                    icon = { Icon(Icons.Default.AddShoppingCart, contentDescription = "Receiving") },
                    label = { Text("Receive 2T", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_receiving"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IndustrialAmber,
                        selectedTextColor = IndustrialAmber,
                        indicatorColor = IndustrialAmber.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentScreen == "delivery",
                    onClick = { currentScreen = "delivery" },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Delivery BBS") },
                    label = { Text("Delivery BBS", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_delivery"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SteelBlue,
                        selectedTextColor = SteelBlue,
                        indicatorColor = SteelBlue.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentScreen == "production",
                    onClick = { currentScreen = "production" },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Cut & Bend") },
                    label = { Text("Mfg", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_production"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EpoxyGreen,
                        selectedTextColor = EpoxyGreen,
                        indicatorColor = EpoxyGreen.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentScreen == "stock_summary",
                    onClick = { currentScreen = "stock_summary" },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Balances") },
                    label = { Text("Stock", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_stock_summary"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IndustrialAmber,
                        selectedTextColor = IndustrialAmber,
                        indicatorColor = IndustrialAmber.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentScreen == "admin",
                    onClick = { currentScreen = "admin" },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                    label = { Text("Admin", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_admin"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IndustrialAmber,
                        selectedTextColor = IndustrialAmber,
                        indicatorColor = IndustrialAmber.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SteelDarkBg)
        ) {
            when (currentScreen) {
                "dashboard" -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> currentScreen = route }
                )
                "receiving" -> ReceivingScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = "dashboard" }
                )
                "delivery" -> BbsDeliveryScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = "dashboard" }
                )
                "production" -> ProductionScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = "dashboard" }
                )
                "transfer" -> TransferAdjustmentScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = "dashboard" }
                )
                "stock_summary" -> StockSummaryScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = "dashboard" }
                )
                "ledger" -> StockLedgerScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = "dashboard" }
                )
                "reconciliation" -> ReconciliationScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = "dashboard" }
                )
                "excel" -> ExcelImportExportScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = "dashboard" }
                )
                "admin" -> AdminSettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = "dashboard" }
                )
                else -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> currentScreen = route }
                )
            }
        }
    }
}
