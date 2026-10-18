package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "companies",
    indices = [Index(value = ["code"], unique = true)]
)
data class CompanyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val isDefaultOwner: Boolean = false
)

@Entity(
    tableName = "suppliers",
    indices = [Index(value = ["code"], unique = true)]
)
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val originCountry: String = "Saudi Arabia"
)

@Entity(
    tableName = "locations",
    indices = [Index(value = ["code"], unique = true)]
)
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val category: String, // from LocationCategory
    val warehouseName: String = "Jeddah Central Plant"
)

/**
 * Material Specification & Stock Balance
 * Enforces separation by company, location, diameter, length, grade, finish, and batch.
 */
@Entity(
    tableName = "stock_balances",
    indices = [
        Index(
            value = ["companyCode", "locationCode", "diameterMm", "lengthM", "grade", "finish", "batchNumber"],
            unique = true
        )
    ]
)
data class StockBalanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyCode: String,
    val supplierOrigin: String,
    val locationCode: String,
    val diameterMm: Int,
    val lengthM: Double,
    val grade: String,
    val finish: String, // BLACK or EPOXY_GREEN
    val batchNumber: String,
    val heatNumber: String,
    val totalPieces: Int,
    val totalBundles: Double,
    val balanceKg: Double,
    val balanceMt: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Immutable Double-Entry Stock Movement Ledger
 * Every single material movement is recorded here with unique document reference.
 */
@Entity(
    tableName = "stock_ledger",
    indices = [
        Index(value = ["docNumber"], unique = true),
        Index(value = ["batchNumber"]),
        Index(value = ["createdAt"])
    ]
)
data class StockLedgerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val docNumber: String,
    val transactionDate: String,
    val movementType: String, // MovementType enum name
    val companyCode: String,
    val supplierOrigin: String,
    val fromLocation: String?,
    val toLocation: String?,
    val diameterMm: Int,
    val lengthM: Double,
    val grade: String,
    val finish: String,
    val batchNumber: String,
    val heatNumber: String,
    val poNumber: String = "",
    val dnNumber: String = "",
    val bbsRef: String = "",
    val customerName: String = "",
    val projectName: String = "",
    val pieces: Int,
    val bundles: Double,
    val weightKg: Double,
    val weightMt: Double,
    val actualWeightKg: Double? = null,
    val isEstimated: Boolean = true,
    val userEmail: String,
    val userRole: String,
    val notes: String = "",
    val status: String = "POSTED",
    val reversalDocNumber: String? = null,
    val isNegativeOverride: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Manufacturing / Cut & Bend Production Orders
 */
@Entity(
    tableName = "manufacturing_orders",
    indices = [Index(value = ["orderNumber"], unique = true)]
)
data class ManufacturingOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val project: String,
    val customer: String,
    val bbsRef: String,
    val diameterMm: Int,
    val finish: String,
    val grade: String,
    val requiredPieces: Int,
    val cutLengthM: Double,
    val barMark: String = "M-01",
    val plannedRawKg: Double,
    val actualConsumedRawKg: Double = 0.0,
    val producedFgKg: Double = 0.0,
    val producedOffcutKg: Double = 0.0,
    val producedScrapKg: Double = 0.0,
    val status: String = "IN_PROGRESS", // IN_PROGRESS, COMPLETED, CANCELLED
    val voucherRef: String = "",
    val operatorUser: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Reconciliation Record (MASAR vs App DN, Batch, Black vs Epoxy Green, Physical vs System)
 */
@Entity(
    tableName = "reconciliation_records"
)
data class ReconciliationRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchRunId: String,
    val reconciliationType: String, // MASAR_DN, BATCH_HEAT, BLACK_VS_EPOXY, PHYSICAL_COUNT
    val referenceKey: String, // e.g. DN-10293 or Batch-882
    val masarValue: String,
    val appValue: String,
    val masarQtyKg: Double,
    val appQtyKg: Double,
    val varianceKg: Double,
    val status: String, // MATCHED, MISMATCHED, MISSING, DUPLICATE
    val details: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Audit Trail Log
 */
@Entity(
    tableName = "audit_logs"
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val entityType: String,
    val entityId: String,
    val summary: String,
    val userEmail: String,
    val userRole: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Offline Drafts (for warehouse mobile entry when offline)
 */
@Entity(
    tableName = "offline_drafts"
)
data class OfflineDraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val draftType: String,
    val referenceTitle: String,
    val payloadJson: String,
    val status: String = "DRAFT", // DRAFT, SYNCED, ERROR
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Dedicated Raw Material Inventory Entity (2-TON Standard)
 * Specialized for steel cut-and-bend factory receiving and stock tracking.
 */
@Entity(
    tableName = "raw_material_inventory",
    indices = [
        Index(
            value = ["companyCode", "locationCode", "diameterMm", "standardLengthM", "grade", "finish", "batchNumber"],
            unique = true
        ),
        Index(value = ["batchNumber"]),
        Index(value = ["dnNumber"])
    ]
)
data class RawMaterialInventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyCode: String,
    val supplierOrigin: String,
    val locationCode: String,
    val diameterMm: Int,
    val standardLengthM: Double = 12.0,
    val grade: String,
    val finish: String, // BLACK or EPOXY_GREEN
    val batchNumber: String,
    val heatNumber: String,
    val full2TonBundles: Int,
    val loosePieces: Int,
    val totalPieces: Int,
    val unitWeightKgPerM: Double,
    val singleBarWeightKg: Double,
    val balanceKg: Double,
    val balanceMt: Double,
    val poNumber: String = "",
    val dnNumber: String = "",
    val receivingDate: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Dedicated BBS-Based Delivery Record Entity
 * Captures project-specific cut-and-bend rebar delivery schedules and dispatch notes.
 */
@Entity(
    tableName = "bbs_delivery_records",
    indices = [
        Index(value = ["deliveryNoteNumber"], unique = true),
        Index(value = ["bbsReference"]),
        Index(value = ["customerName"]),
        Index(value = ["batchNumber"]),
        Index(value = ["deliveryDate"])
    ]
)
data class BbsDeliveryRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deliveryNoteNumber: String,
    val customerName: String,
    val projectName: String,
    val bbsReference: String,
    val barMark: String = "M-01",
    val shapeCode: String = "00",
    val diameterMm: Int,
    val cutLengthM: Double,
    val requiredPieces: Int,
    val totalLengthM: Double,
    val grade: String,
    val finish: String, // BLACK or EPOXY_GREEN
    val companyCode: String,
    val sourceLocation: String,
    val batchNumber: String,
    val heatNumber: String,
    val bundleCount: Int,
    val loosePieces: Int,
    val weightKg: Double,
    val weightMt: Double,
    val actualScaleWeightKg: Double? = null,
    val deliveryDate: String,
    val driverName: String = "",
    val vehicleNumber: String = "",
    val status: String = "DELIVERED",
    val dispatchedByUser: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

