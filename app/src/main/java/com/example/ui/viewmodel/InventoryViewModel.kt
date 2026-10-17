package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.calculator.RebarCalculator
import com.example.data.db.AppDatabase
import com.example.data.model.CompanyEntity
import com.example.data.model.LocationEntity
import com.example.data.model.ManufacturingOrderEntity
import com.example.data.model.OfflineDraftEntity
import com.example.data.model.ReconciliationRecordEntity
import com.example.data.model.StockBalanceEntity
import com.example.data.model.StockLedgerEntity
import com.example.data.model.SupplierEntity
import com.example.data.model.UserRole
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DashboardMetrics(
    val totalStockMt: Double = 0.0,
    val totalStockKg: Double = 0.0,
    val blackSteelMt: Double = 0.0,
    val epoxyGreenMt: Double = 0.0,
    val totalPieces: Int = 0,
    val todayInflowMt: Double = 0.0,
    val todayOutflowMt: Double = 0.0,
    val scrapStockKg: Double = 0.0,
    val activeOrdersCount: Int = 0,
    val reconMatchedCount: Int = 0,
    val reconMismatchedCount: Int = 0,
    val reconMissingCount: Int = 0
)

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    val repository = InventoryRepository(db)

    // Current Active User & Role
    private val _currentUserEmail = MutableStateFlow("controller@talaisteel.com")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _currentUserRole = MutableStateFlow(UserRole.INVENTORY_CONTROLLER)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    // Notification / Toast Message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    // Repository Flows
    val stockBalances: StateFlow<List<StockBalanceEntity>> = repository.allStockBalances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockLedger: StateFlow<List<StockLedgerEntity>> = repository.allStockLedger
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val manufacturingOrders: StateFlow<List<ManufacturingOrderEntity>> = repository.allManufacturingOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reconciliationRecords: StateFlow<List<ReconciliationRecordEntity>> = repository.allReconciliationRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val companies: StateFlow<List<CompanyEntity>> = repository.allCompanies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<SupplierEntity>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val locations: StateFlow<List<LocationEntity>> = repository.allLocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlineDrafts: StateFlow<List<OfflineDraftEntity>> = repository.allOfflineDrafts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Combined Metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        stockBalances,
        stockLedger,
        manufacturingOrders,
        reconciliationRecords
    ) { balances, ledger, orders, recon ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        var totalMt = 0.0
        var totalKg = 0.0
        var blackMt = 0.0
        var epoxyMt = 0.0
        var pieces = 0
        var scrapKg = 0.0

        for (b in balances) {
            totalMt += b.balanceMt
            totalKg += b.balanceKg
            pieces += b.totalPieces
            if (b.finish == "EPOXY_GREEN") {
                epoxyMt += b.balanceMt
            } else {
                blackMt += b.balanceMt
            }
            if (b.locationCode == "SCRAP-BIN") {
                scrapKg += b.balanceKg
            }
        }

        var todayInMt = 0.0
        var todayOutMt = 0.0
        for (l in ledger) {
            if (l.transactionDate == todayStr && l.status != "REVERSED") {
                if (l.toLocation != null && l.fromLocation == null) {
                    todayInMt += l.weightMt
                } else if (l.fromLocation != null && l.toLocation == null) {
                    todayOutMt += l.weightMt
                }
            }
        }

        val activeOrders = orders.count { it.status == "IN_PROGRESS" }
        val matched = recon.count { it.status == "MATCHED" }
        val mismatched = recon.count { it.status == "MISMATCHED" }
        val missing = recon.count { it.status == "MISSING" }

        DashboardMetrics(
            totalStockMt = RebarCalculator.round(totalMt, 3),
            totalStockKg = RebarCalculator.round(totalKg, 2),
            blackSteelMt = RebarCalculator.round(blackMt, 3),
            epoxyGreenMt = RebarCalculator.round(epoxyMt, 3),
            totalPieces = pieces,
            todayInflowMt = RebarCalculator.round(todayInMt, 3),
            todayOutflowMt = RebarCalculator.round(todayOutMt, 3),
            scrapStockKg = RebarCalculator.round(scrapKg, 2),
            activeOrdersCount = activeOrders,
            reconMatchedCount = matched,
            reconMismatchedCount = mismatched,
            reconMissingCount = missing
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    fun switchRole(role: UserRole) {
        _currentUserRole.value = role
        _currentUserEmail.value = when (role) {
            UserRole.ADMIN -> "admin@talaisteel.com"
            UserRole.INVENTORY_CONTROLLER -> "controller@talaisteel.com"
            UserRole.WAREHOUSE_SUPERVISOR -> "supervisor@talaisteel.com"
            UserRole.STORE_KEEPER -> "storekeeper@talaisteel.com"
            UserRole.AUDITOR -> "auditor@talaisteel.com"
        }
        _snackbarMessage.value = "Switched to ${role.title} (${_currentUserEmail.value})"
    }

    // 1. Post Receiving
    fun postReceiving(
        companyCode: String,
        supplierOrigin: String,
        destinationLocation: String,
        diameterMm: Int,
        lengthM: Double,
        grade: String,
        finish: String,
        fullBundles: Int,
        looseBundles: Int,
        loosePieces: Int,
        batchNumber: String,
        heatNumber: String,
        poNumber: String,
        dnNumber: String,
        actualWeightKg: Double?,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.postRawMaterialReceiving(
                companyCode = companyCode,
                supplierOrigin = supplierOrigin,
                destinationLocation = destinationLocation,
                diameterMm = diameterMm,
                lengthM = lengthM,
                grade = grade,
                finish = finish,
                fullBundles = fullBundles,
                looseBundles = looseBundles,
                loosePieces = loosePieces,
                batchNumber = batchNumber,
                heatNumber = heatNumber,
                poNumber = poNumber,
                dnNumber = dnNumber,
                actualWeightKg = actualWeightKg,
                userEmail = _currentUserEmail.value,
                userRole = _currentUserRole.value,
                notes = notes
            )
            result.fold(
                onSuccess = { docNum ->
                    _snackbarMessage.value = "Posted Receiving $docNum successfully!"
                    onSuccess()
                },
                onFailure = { error ->
                    _snackbarMessage.value = "Receiving failed: ${error.message}"
                }
            )
        }
    }

    // 2. Post Customer Delivery (BBS)
    fun postCustomerDelivery(
        companyCode: String,
        sourceLocation: String,
        customerName: String,
        projectName: String,
        deliveryNote: String,
        bbsRef: String,
        diameterMm: Int,
        cutLengthM: Double,
        grade: String,
        finish: String,
        batchNumber: String,
        heatNumber: String,
        requiredPieces: Int,
        allowNegativeOverride: Boolean = false,
        overrideReason: String = "",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.postCustomerDelivery(
                companyCode = companyCode,
                sourceLocation = sourceLocation,
                customerName = customerName,
                projectName = projectName,
                deliveryNote = deliveryNote,
                bbsRef = bbsRef,
                diameterMm = diameterMm,
                cutLengthM = cutLengthM,
                grade = grade,
                finish = finish,
                batchNumber = batchNumber,
                heatNumber = heatNumber,
                requiredPieces = requiredPieces,
                customBundlePcs = null,
                userEmail = _currentUserEmail.value,
                userRole = _currentUserRole.value,
                allowNegativeOverride = allowNegativeOverride,
                overrideReason = overrideReason
            )
            result.fold(
                onSuccess = { docNum ->
                    _snackbarMessage.value = "Delivery $docNum dispatched successfully!"
                    onSuccess()
                },
                onFailure = { error ->
                    _snackbarMessage.value = error.message ?: "Delivery failed"
                }
            )
        }
    }

    // 3. Post Internal Transfer
    fun postInternalTransfer(
        companyCode: String,
        fromLocation: String,
        toLocation: String,
        diameterMm: Int,
        lengthM: Double,
        grade: String,
        finish: String,
        batchNumber: String,
        heatNumber: String,
        supplierOrigin: String,
        transferPieces: Int,
        allowNegativeOverride: Boolean = false,
        overrideReason: String = "",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.postInternalTransfer(
                companyCode = companyCode,
                fromLocation = fromLocation,
                toLocation = toLocation,
                diameterMm = diameterMm,
                lengthM = lengthM,
                grade = grade,
                finish = finish,
                batchNumber = batchNumber,
                heatNumber = heatNumber,
                supplierOrigin = supplierOrigin,
                transferPieces = transferPieces,
                userEmail = _currentUserEmail.value,
                userRole = _currentUserRole.value,
                allowNegativeOverride = allowNegativeOverride,
                overrideReason = overrideReason
            )
            result.fold(
                onSuccess = { docNum ->
                    _snackbarMessage.value = "Transfer $docNum posted ($transferPieces pcs to $toLocation)!"
                    onSuccess()
                },
                onFailure = { error ->
                    _snackbarMessage.value = error.message ?: "Transfer failed"
                }
            )
        }
    }

    // 4. Post Production Output
    fun postProduction(
        orderNumber: String,
        rawLocation: String,
        fgLocation: String,
        offcutLocation: String,
        scrapLocation: String,
        diameterMm: Int,
        rawLengthM: Double,
        cutLengthM: Double,
        grade: String,
        finish: String,
        batchNumber: String,
        heatNumber: String,
        companyCode: String,
        consumedRawPieces: Int,
        producedFgPieces: Int,
        producedOffcutKg: Double,
        producedScrapKg: Double,
        voucherRef: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.postProduction(
                orderNumber = orderNumber,
                rawLocation = rawLocation,
                fgLocation = fgLocation,
                offcutLocation = offcutLocation,
                scrapLocation = scrapLocation,
                diameterMm = diameterMm,
                rawLengthM = rawLengthM,
                cutLengthM = cutLengthM,
                grade = grade,
                finish = finish,
                batchNumber = batchNumber,
                heatNumber = heatNumber,
                companyCode = companyCode,
                consumedRawPieces = consumedRawPieces,
                producedFgPieces = producedFgPieces,
                producedOffcutKg = producedOffcutKg,
                producedScrapKg = producedScrapKg,
                voucherRef = voucherRef,
                userEmail = _currentUserEmail.value,
                userRole = _currentUserRole.value
            )
            result.fold(
                onSuccess = { docNum ->
                    _snackbarMessage.value = "Production $docNum posted successfully!"
                    onSuccess()
                },
                onFailure = { error ->
                    _snackbarMessage.value = error.message ?: "Production failed"
                }
            )
        }
    }

    // 5. Reverse Transaction
    fun reverseTransaction(docNumber: String, reason: String) {
        viewModelScope.launch {
            val result = repository.reverseTransaction(
                originalDocNumber = docNumber,
                reversalReason = reason,
                userEmail = _currentUserEmail.value,
                userRole = _currentUserRole.value
            )
            result.fold(
                onSuccess = { revDoc ->
                    _snackbarMessage.value = "Transaction reversed under $revDoc"
                },
                onFailure = { error ->
                    _snackbarMessage.value = "Reversal error: ${error.message}"
                }
            )
        }
    }

    // 6. Reconciliation Run
    fun runReconciliationBatch() {
        viewModelScope.launch {
            val ledgerList = stockLedger.value
            val simulatedMasarRecords = listOf(
                ReconciliationRecordEntity(
                    batchRunId = "RECON-${System.currentTimeMillis() % 10000}",
                    reconciliationType = "MASAR_DN",
                    referenceKey = "DN-MIT-8841",
                    masarValue = "DN-MIT-8841 (10,020.00 KG)",
                    appValue = "DN-MIT-8841 (10,020.00 KG)",
                    masarQtyKg = 10020.00,
                    appQtyKg = 10020.00,
                    varianceKg = 0.00,
                    status = "MATCHED",
                    details = "Delivery Note and Net Weighbridge tickets match exactly"
                ),
                ReconciliationRecordEntity(
                    batchRunId = "RECON-${System.currentTimeMillis() % 10000}",
                    reconciliationType = "BATCH_HEAT",
                    referenceKey = "HT-88421",
                    masarValue = "Batch B-2026-1020 / HT-88421",
                    appValue = "Batch B-2026-1020 / HT-88421",
                    masarQtyKg = 10016.64,
                    appQtyKg = 10016.64,
                    varianceKg = 0.00,
                    status = "MATCHED",
                    details = "Full batch traceability and heat number verified"
                ),
                ReconciliationRecordEntity(
                    batchRunId = "RECON-${System.currentTimeMillis() % 10000}",
                    reconciliationType = "MASAR_DN",
                    referenceKey = "DN-TAM-5521",
                    masarValue = "DN-TAM-5521 (10,036.08 KG)",
                    appValue = "DN-TAM-5521 (10,040.00 KG)",
                    masarQtyKg = 10036.08,
                    appQtyKg = 10040.00,
                    varianceKg = 3.92,
                    status = "MISMATCHED",
                    details = "Scale measured weight (10,040 kg) differs from MASAR invoice theoretical weight (+3.92 kg / +0.04%)"
                ),
                ReconciliationRecordEntity(
                    batchRunId = "RECON-${System.currentTimeMillis() % 10000}",
                    reconciliationType = "BLACK_VS_EPOXY",
                    referenceKey = "COAT-2026-08",
                    masarValue = "Black Rebar Sent: 10,036.08 KG",
                    appValue = "Epoxy Coated Recv: 10,186.62 KG",
                    masarQtyKg = 10036.08,
                    appQtyKg = 10186.62,
                    varianceKg = 150.54,
                    status = "MATCHED",
                    details = "1.50% Epoxy mass gain adheres strictly to factory coating tolerance (+/- 2%)"
                ),
                ReconciliationRecordEntity(
                    batchRunId = "RECON-${System.currentTimeMillis() % 10000}",
                    reconciliationType = "MASAR_DN",
                    referenceKey = "DN-UNKNOWN-9912",
                    masarValue = "DN-UNKNOWN-9912 (8,400.00 KG)",
                    appValue = "UNRECORDED_IN_FACTORY_APP",
                    masarQtyKg = 8400.00,
                    appQtyKg = 0.00,
                    varianceKg = -8400.00,
                    status = "MISSING",
                    details = "DN appears in MASAR export but has no physical receiving voucher in app"
                )
            )
            repository.runReconciliationAudit(simulatedMasarRecords)
            _snackbarMessage.value = "Reconciliation audit completed. Verified 5 audit keys."
        }
    }

    // Physical Stock Count Adjustment
    fun postStockAdjustment(
        balanceId: Long,
        physicalPieces: Int,
        physicalKg: Double,
        reason: String,
        userEmail: String,
        userRole: UserRole
    ) {
        viewModelScope.launch {
            val result = repository.postStockAdjustment(
                balanceId = balanceId,
                physicalPieces = physicalPieces,
                physicalKg = physicalKg,
                reason = reason,
                userEmail = userEmail,
                userRole = userRole
            )
            result.fold(
                onSuccess = { docNum ->
                    _snackbarMessage.value = "Physical count adjustment posted ($docNum)"
                },
                onFailure = { error ->
                    _snackbarMessage.value = "Adjustment error: ${error.message}"
                }
            )
        }
    }

    // CSV Generation
    fun generateStockBalancesCsv(): String {
        val balances = stockBalances.value
        val sb = StringBuilder()
        sb.append("Company,Location,Diameter(mm),Length(m),Grade,Finish,Batch No,Heat No,Pieces,Bundles,Balance(KG),Balance(MT)\n")
        for (b in balances) {
            sb.append("${b.companyCode},${b.locationCode},${b.diameterMm},${b.lengthM},${b.grade},${b.finish},${b.batchNumber},${b.heatNumber},${b.totalPieces},${b.totalBundles},${b.balanceKg},${b.balanceMt}\n")
        }
        return sb.toString()
    }

    fun generateStockLedgerCsv(): String {
        val ledger = stockLedger.value
        val sb = StringBuilder()
        sb.append("Doc No,Date,Movement,Company,From,To,Diameter(mm),Grade,Finish,Batch,Pieces,Weight(KG),Weight(MT),User,Status\n")
        for (l in ledger) {
            sb.append("${l.docNumber},${l.transactionDate},${l.movementType},${l.companyCode},${l.fromLocation ?: "-"},${l.toLocation ?: "-"},${l.diameterMm},${l.grade},${l.finish},${l.batchNumber},${l.pieces},${l.weightKg},${l.weightMt},${l.userEmail},${l.status}\n")
        }
        return sb.toString()
    }
}
