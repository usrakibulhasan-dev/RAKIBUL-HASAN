package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.CompanyEntity
import com.example.data.model.LocationCategory
import com.example.data.model.LocationEntity
import com.example.data.model.ManufacturingOrderEntity
import com.example.data.model.OfflineDraftEntity
import com.example.data.model.ReconciliationRecordEntity
import com.example.data.model.StockBalanceEntity
import com.example.data.model.StockLedgerEntity
import com.example.data.model.SupplierEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CompanyEntity::class,
        SupplierEntity::class,
        LocationEntity::class,
        StockBalanceEntity::class,
        StockLedgerEntity::class,
        ManufacturingOrderEntity::class,
        ReconciliationRecordEntity::class,
        AuditLogEntity::class,
        OfflineDraftEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun companyDao(): CompanyDao
    abstract fun supplierDao(): SupplierDao
    abstract fun locationDao(): LocationDao
    abstract fun stockBalanceDao(): StockBalanceDao
    abstract fun stockLedgerDao(): StockLedgerDao
    abstract fun manufacturingOrderDao(): ManufacturingOrderDao
    abstract fun reconciliationDao(): ReconciliationDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun offlineDraftDao(): OfflineDraftDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "talai_folath_steel_erp.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            val companies = listOf(
                CompanyEntity(code = "TALAI", name = "TALAI AL-FOLATH FOR MANUFACTURING CO.", isDefaultOwner = true),
                CompanyEntity(code = "MITWALLI", name = "MITWALLI STEEL", isDefaultOwner = false),
                CompanyEntity(code = "TAMEER", name = "TAMEER STEEL INDUSTRIES", isDefaultOwner = false),
                CompanyEntity(code = "SEA", name = "SEA STEEL TRADING", isDefaultOwner = false),
                CompanyEntity(code = "AL_BAWAN", name = "AL BAWAN CO.", isDefaultOwner = false),
                CompanyEntity(code = "BATCO", name = "BATCO GROUP", isDefaultOwner = false),
                CompanyEntity(code = "BBIAGC", name = "BBIAGC INDUSTRIAL", isDefaultOwner = false),
                CompanyEntity(code = "OTHER", name = "OTHER / THIRD PARTY CONSIGNMENT", isDefaultOwner = false)
            )
            db.companyDao().insertCompanies(companies)

            val suppliers = listOf(
                SupplierEntity(code = "MITWALLI", name = "Mitwalli Steel Factory", originCountry = "Saudi Arabia"),
                SupplierEntity(code = "TAMEER", name = "Tameer Steel Industries", originCountry = "Saudi Arabia"),
                SupplierEntity(code = "HADEED", name = "SABIC Hadeed Jubail", originCountry = "Saudi Arabia"),
                SupplierEntity(code = "ISPC", name = "Al-Ittefaq Steel Products", originCountry = "Saudi Arabia"),
                SupplierEntity(code = "RAJHI", name = "Al-Rajhi Steel", originCountry = "Saudi Arabia"),
                SupplierEntity(code = "SEA", name = "Sea Steel Logistics", originCountry = "UAE/KSA"),
                SupplierEntity(code = "BATCO", name = "Batco Metal Trading", originCountry = "Saudi Arabia"),
                SupplierEntity(code = "BBIAGC", name = "BBIAGC Steel Supply", originCountry = "Saudi Arabia")
            )
            db.supplierDao().insertSuppliers(suppliers)

            val locations = listOf(
                LocationEntity(code = "RM-BAY-01", name = "Raw Material Bay 01 (Black)", category = LocationCategory.RAW_MATERIAL.name),
                LocationEntity(code = "RM-BAY-02", name = "Raw Material Bay 02 (Heavy Bars)", category = LocationCategory.RAW_MATERIAL.name),
                LocationEntity(code = "RM-BAY-03", name = "Raw Material Bay 03 (Special/Consigned)", category = LocationCategory.RAW_MATERIAL.name),
                LocationEntity(code = "LINE-01-CUT", name = "Shear Cutting Line 01", category = LocationCategory.PRODUCTION_FLOOR.name),
                LocationEntity(code = "LINE-02-BEND", name = "Automatic Bending Line 02", category = LocationCategory.PRODUCTION_FLOOR.name),
                LocationEntity(code = "FG-YARD-A", name = "Finished Goods Ready Yard A", category = LocationCategory.FINISHED_GOODS.name),
                LocationEntity(code = "FG-DISPATCH", name = "Dispatch Delivery Staging", category = LocationCategory.FINISHED_GOODS.name),
                LocationEntity(code = "OFFCUT-RACK", name = "Off-cut Usable Lengths Rack", category = LocationCategory.OFFCUT_RACK.name),
                LocationEntity(code = "SCRAP-BIN", name = "Scrap & Trimming Bin 01", category = LocationCategory.SCRAP_YARD.name),
                LocationEntity(code = "EPOXY-YARD", name = "Epoxy Coated Green Yard", category = LocationCategory.EPOXY_YARD.name),
                LocationEntity(code = "QUARANTINE", name = "Customer Return / Quarantine", category = LocationCategory.QUARANTINE.name)
            )
            db.locationDao().insertLocations(locations)

            // Seed initial verified inventory balances
            val initialBalances = listOf(
                StockBalanceEntity(
                    companyCode = "TALAI",
                    supplierOrigin = "MITWALLI",
                    locationCode = "RM-BAY-01",
                    diameterMm = 12,
                    lengthM = 12.0,
                    grade = "Grade 60",
                    finish = "BLACK",
                    batchNumber = "B-2026-1020",
                    heatNumber = "HT-88421",
                    totalPieces = 940, // 5 bundles of 188 pcs
                    totalBundles = 5.0,
                    balanceKg = 10016.64,
                    balanceMt = 10.017
                ),
                StockBalanceEntity(
                    companyCode = "TALAI",
                    supplierOrigin = "TAMEER",
                    locationCode = "RM-BAY-01",
                    diameterMm = 16,
                    lengthM = 12.0,
                    grade = "Grade 60",
                    finish = "BLACK",
                    batchNumber = "B-2026-1025",
                    heatNumber = "HT-99312",
                    totalPieces = 530, // 5 bundles of 106 pcs
                    totalBundles = 5.0,
                    balanceKg = 10036.08,
                    balanceMt = 10.036
                ),
                StockBalanceEntity(
                    companyCode = "TALAI",
                    supplierOrigin = "HADEED",
                    locationCode = "RM-BAY-02",
                    diameterMm = 20,
                    lengthM = 12.0,
                    grade = "B500B",
                    finish = "BLACK",
                    batchNumber = "B-2026-1031",
                    heatNumber = "HT-77610",
                    totalPieces = 340, // 5 bundles of 68 pcs
                    totalBundles = 5.0,
                    balanceKg = 10061.28,
                    balanceMt = 10.061
                ),
                StockBalanceEntity(
                    companyCode = "TALAI",
                    supplierOrigin = "ISPC",
                    locationCode = "EPOXY-YARD",
                    diameterMm = 16,
                    lengthM = 12.0,
                    grade = "ASTM A615",
                    finish = "EPOXY_GREEN",
                    batchNumber = "B-2026-EP04",
                    heatNumber = "HT-55419",
                    totalPieces = 318, // 3 bundles of 106 pcs
                    totalBundles = 3.0,
                    balanceKg = 6111.96,
                    balanceMt = 6.112
                ),
                StockBalanceEntity(
                    companyCode = "TALAI",
                    supplierOrigin = "MITWALLI",
                    locationCode = "FG-YARD-A",
                    diameterMm = 12,
                    lengthM = 3.8,
                    grade = "Grade 60",
                    finish = "BLACK",
                    batchNumber = "B-2026-1020",
                    heatNumber = "HT-88421",
                    totalPieces = 250,
                    totalBundles = 1.0,
                    balanceKg = 843.60,
                    balanceMt = 0.844
                ),
                StockBalanceEntity(
                    companyCode = "TALAI",
                    supplierOrigin = "INTERNAL",
                    locationCode = "SCRAP-BIN",
                    diameterMm = 12,
                    lengthM = 0.0,
                    grade = "Mixed",
                    finish = "BLACK",
                    batchNumber = "SCRAP-OCT-26",
                    heatNumber = "SCRAP",
                    totalPieces = 0,
                    totalBundles = 0.0,
                    balanceKg = 342.50,
                    balanceMt = 0.343
                )
            )

            for (bal in initialBalances) {
                db.stockBalanceDao().insertOrUpdate(bal)
            }

            // Seed initial Stock Ledger entries
            val initialLedger = listOf(
                StockLedgerEntity(
                    docNumber = "RCV-2026-0001",
                    transactionDate = "2026-10-15",
                    movementType = "RECEIVING_2TON",
                    companyCode = "TALAI",
                    supplierOrigin = "MITWALLI",
                    fromLocation = null,
                    toLocation = "RM-BAY-01",
                    diameterMm = 12,
                    lengthM = 12.0,
                    grade = "Grade 60",
                    finish = "BLACK",
                    batchNumber = "B-2026-1020",
                    heatNumber = "HT-88421",
                    poNumber = "PO-9910",
                    dnNumber = "DN-MIT-8841",
                    pieces = 940,
                    bundles = 5.0,
                    weightKg = 10016.64,
                    weightMt = 10.017,
                    actualWeightKg = 10020.0,
                    isEstimated = false,
                    userEmail = "storekeeper@talaisteel.com",
                    userRole = "STORE_KEEPER",
                    notes = "Initial 2-Ton bundle receiving verified by weighbridge ticket #WB-4412"
                ),
                StockLedgerEntity(
                    docNumber = "RCV-2026-0002",
                    transactionDate = "2026-10-16",
                    movementType = "RECEIVING_2TON",
                    companyCode = "TALAI",
                    supplierOrigin = "TAMEER",
                    fromLocation = null,
                    toLocation = "RM-BAY-01",
                    diameterMm = 16,
                    lengthM = 12.0,
                    grade = "Grade 60",
                    finish = "BLACK",
                    batchNumber = "B-2026-1025",
                    heatNumber = "HT-99312",
                    poNumber = "PO-9915",
                    dnNumber = "DN-TAM-5521",
                    pieces = 530,
                    bundles = 5.0,
                    weightKg = 10036.08,
                    weightMt = 10.036,
                    actualWeightKg = 10040.0,
                    isEstimated = false,
                    userEmail = "storekeeper@talaisteel.com",
                    userRole = "STORE_KEEPER",
                    notes = "Tameer 16mm rebar received into Bay 01"
                )
            )

            for (entry in initialLedger) {
                db.stockLedgerDao().insertLedger(entry)
            }

            // Seed sample manufacturing orders
            val sampleOrder = ManufacturingOrderEntity(
                orderNumber = "MO-2026-0042",
                project = "Jeddah Metro Station Phase 2",
                customer = "Nesma & Partners",
                bbsRef = "BBS-METRO-PIER-C3",
                diameterMm = 16,
                finish = "BLACK",
                grade = "Grade 60",
                requiredPieces = 300,
                cutLengthM = 5.80,
                barMark = "B-01-CRANK",
                plannedRawKg = 2745.72,
                status = "IN_PROGRESS",
                voucherRef = "MFG-VOUCHER-088",
                operatorUser = "supervisor@talaisteel.com"
            )
            db.manufacturingOrderDao().insert(sampleOrder)

            // Seed initial reconciliation sample entries
            val reconSamples = listOf(
                ReconciliationRecordEntity(
                    batchRunId = "RUN-OCT17-01",
                    reconciliationType = "MASAR_DN",
                    referenceKey = "DN-MIT-8841",
                    masarValue = "DN-MIT-8841 (10,020.00 KG)",
                    appValue = "DN-MIT-8841 (10,020.00 KG)",
                    masarQtyKg = 10020.00,
                    appQtyKg = 10020.00,
                    varianceKg = 0.00,
                    status = "MATCHED",
                    details = "Exact match on Mitwalli Delivery Note and weighbridge actual weight"
                ),
                ReconciliationRecordEntity(
                    batchRunId = "RUN-OCT17-01",
                    reconciliationType = "BATCH_HEAT",
                    referenceKey = "HT-99312",
                    masarValue = "Tameer 16mm / B-2026-1025",
                    appValue = "Tameer 16mm / B-2026-1025",
                    masarQtyKg = 10036.08,
                    appQtyKg = 10040.00,
                    varianceKg = 3.92,
                    status = "MISMATCHED",
                    details = "Variance between MASAR theoretical (10,036.08 kg) and app scale weight (10,040.00 kg)"
                ),
                ReconciliationRecordEntity(
                    batchRunId = "RUN-OCT17-01",
                    reconciliationType = "BLACK_VS_EPOXY",
                    referenceKey = "COAT-REQ-003",
                    masarValue = "Black Rebar Dispatched: 6,022.00 KG",
                    appValue = "Epoxy Green Received: 6,111.96 KG",
                    masarQtyKg = 6022.00,
                    appQtyKg = 6111.96,
                    varianceKg = 89.96,
                    status = "MATCHED",
                    details = "Weight gain of 1.49% consistent with ASTM A775 epoxy coating thickness standard"
                ),
                ReconciliationRecordEntity(
                    batchRunId = "RUN-OCT17-01",
                    reconciliationType = "MASAR_DN",
                    referenceKey = "DN-MASAR-PENDING-99",
                    masarValue = "DN-MASAR-PENDING-99 (5,000.00 KG)",
                    appValue = "NOT_FOUND_IN_APP",
                    masarQtyKg = 5000.00,
                    appQtyKg = 0.00,
                    varianceKg = -5000.00,
                    status = "MISSING",
                    details = "Document listed in MASAR export but not yet received or posted in factory app"
                )
            )
            db.reconciliationDao().insertAll(reconSamples)

            // Seed initial audit log
            db.auditLogDao().insert(
                AuditLogEntity(
                    action = "SYSTEM_INITIALIZATION",
                    entityType = "DATABASE",
                    entityId = "INIT-001",
                    summary = "Talai Al-Folath ERP database initialized with standard locations, rebar matrices, and companies",
                    userEmail = "admin@talaisteel.com",
                    userRole = "ADMIN"
                )
            )
        }
    }
}
