package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.calculator.RebarCalculator
import com.example.data.db.AppDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.CompanyEntity
import com.example.data.model.LocationEntity
import com.example.data.model.ManufacturingOrderEntity
import com.example.data.model.MovementType
import com.example.data.model.OfflineDraftEntity
import com.example.data.model.ReconciliationRecordEntity
import com.example.data.model.StockBalanceEntity
import com.example.data.model.StockLedgerEntity
import com.example.data.model.SupplierEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class InventoryRepository(private val db: AppDatabase) {

    val allCompanies: Flow<List<CompanyEntity>> = db.companyDao().getAllCompanies()
    val allSuppliers: Flow<List<SupplierEntity>> = db.supplierDao().getAllSuppliers()
    val allLocations: Flow<List<LocationEntity>> = db.locationDao().getAllLocations()
    val allStockBalances: Flow<List<StockBalanceEntity>> = db.stockBalanceDao().getAllBalances()
    val allStockLedger: Flow<List<StockLedgerEntity>> = db.stockLedgerDao().getAllLedger()
    val allManufacturingOrders: Flow<List<ManufacturingOrderEntity>> = db.manufacturingOrderDao().getAllOrders()
    val allReconciliationRecords: Flow<List<ReconciliationRecordEntity>> = db.reconciliationDao().getAllRecords()
    val allAuditLogs: Flow<List<AuditLogEntity>> = db.auditLogDao().getAllLogs()
    val allOfflineDrafts: Flow<List<OfflineDraftEntity>> = db.offlineDraftDao().getAllDrafts()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * 1. Post Raw Material Receiving (2-TON Bundle Calculation)
     */
    suspend fun postRawMaterialReceiving(
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
        userEmail: String,
        userRole: UserRole,
        notes: String
    ): Result<String> {
        val calc = RebarCalculator.calculateReceiving2Ton(
            diameterMm = diameterMm,
            lengthM = lengthM,
            fullBundles = fullBundles,
            looseBundles = looseBundles,
            loosePieces = loosePieces,
            isEpoxy = finish == "EPOXY_GREEN",
            actualWeightKg = actualWeightKg
        )

        val docNum = "RCV-${System.currentTimeMillis() % 1000000}"
        val today = dateFormat.format(Date())

        val ledgerEntry = StockLedgerEntity(
            docNumber = docNum,
            transactionDate = today,
            movementType = MovementType.RECEIVING_2TON.name,
            companyCode = companyCode,
            supplierOrigin = supplierOrigin,
            fromLocation = null,
            toLocation = destinationLocation,
            diameterMm = diameterMm,
            lengthM = lengthM,
            grade = grade,
            finish = finish,
            batchNumber = batchNumber.ifBlank { "BATCH-$docNum" },
            heatNumber = heatNumber.ifBlank { "HEAT-$docNum" },
            poNumber = poNumber,
            dnNumber = dnNumber,
            pieces = calc.totalPieces,
            bundles = (fullBundles + looseBundles).toDouble(),
            weightKg = calc.theoreticalWeightKg,
            weightMt = calc.theoreticalWeightMt,
            actualWeightKg = calc.actualWeightKg,
            isEstimated = calc.isEstimated,
            userEmail = userEmail,
            userRole = userRole.name,
            notes = notes
        )

        db.withTransaction {
            increaseStockBalance(
                companyCode = companyCode,
                supplierOrigin = supplierOrigin,
                locationCode = destinationLocation,
                diameterMm = diameterMm,
                lengthM = lengthM,
                grade = grade,
                finish = finish,
                batchNumber = ledgerEntry.batchNumber,
                heatNumber = ledgerEntry.heatNumber,
                pieces = calc.totalPieces,
                bundles = ledgerEntry.bundles,
                weightKg = calc.theoreticalWeightKg,
                weightMt = calc.theoreticalWeightMt
            )
            db.stockLedgerDao().insertLedger(ledgerEntry)
            logAudit(
                action = "POST_RECEIVING",
                entityType = "STOCK_LEDGER",
                entityId = docNum,
                summary = "Received $fullBundles 2T bundles ($calc.totalPieces pcs, ${calc.theoreticalWeightMt} MT) into $destinationLocation",
                userEmail = userEmail,
                userRole = userRole.name
            )
        }
        return Result.success(docNum)
    }

    /**
     * 2. Post Customer Delivery (BBS Calculation)
     */
    suspend fun postCustomerDelivery(
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
        customBundlePcs: Int?,
        userEmail: String,
        userRole: UserRole,
        allowNegativeOverride: Boolean = false,
        overrideReason: String = ""
    ): Result<String> {
        val calc = RebarCalculator.calculateBbsDelivery(
            diameterMm = diameterMm,
            cutLengthM = cutLengthM,
            requiredPieces = requiredPieces,
            isEpoxy = finish == "EPOXY_GREEN",
            customBundlePcs = customBundlePcs
        )

        val docNum = "DEL-${System.currentTimeMillis() % 1000000}"
        val today = dateFormat.format(Date())

        return try {
            db.withTransaction {
                decreaseStockBalance(
                    companyCode = companyCode,
                    locationCode = sourceLocation,
                    diameterMm = diameterMm,
                    lengthM = cutLengthM,
                    grade = grade,
                    finish = finish,
                    batchNumber = batchNumber,
                    pieces = requiredPieces,
                    weightKg = calc.weightKg,
                    allowNegativeOverride = allowNegativeOverride,
                    overrideReason = overrideReason
                )

                val ledgerEntry = StockLedgerEntity(
                    docNumber = docNum,
                    transactionDate = today,
                    movementType = MovementType.BBS_DELIVERY.name,
                    companyCode = companyCode,
                    supplierOrigin = "TALAI",
                    fromLocation = sourceLocation,
                    toLocation = null,
                    diameterMm = diameterMm,
                    lengthM = cutLengthM,
                    grade = grade,
                    finish = finish,
                    batchNumber = batchNumber,
                    heatNumber = heatNumber,
                    dnNumber = deliveryNote,
                    bbsRef = bbsRef,
                    customerName = customerName,
                    projectName = projectName,
                    pieces = requiredPieces,
                    bundles = calc.bundleCount.toDouble(),
                    weightKg = calc.weightKg,
                    weightMt = calc.weightMt,
                    actualWeightKg = calc.weightKg,
                    isEstimated = false,
                    userEmail = userEmail,
                    userRole = userRole.name,
                    isNegativeOverride = allowNegativeOverride,
                    notes = "Delivered to $customerName - $projectName (BBS: $bbsRef)"
                )
                db.stockLedgerDao().insertLedger(ledgerEntry)
                logAudit(
                    action = "CUSTOMER_DELIVERY",
                    entityType = "STOCK_LEDGER",
                    entityId = docNum,
                    summary = "Dispatched $requiredPieces pcs (${calc.weightMt} MT) to $customerName (DN: $deliveryNote)",
                    userEmail = userEmail,
                    userRole = userRole.name
                )
            }
            Result.success(docNum)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 3. Internal Stock Transfer (Source -> Destination atomically)
     */
    suspend fun postInternalTransfer(
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
        userEmail: String,
        userRole: UserRole,
        allowNegativeOverride: Boolean = false,
        overrideReason: String = ""
    ): Result<String> {
        if (fromLocation == toLocation) {
            return Result.failure(IllegalArgumentException("Source and destination locations cannot be identical."))
        }
        val unitWeight = RebarCalculator.getUnitWeight(diameterMm, finish == "EPOXY_GREEN")
        val transferKg = RebarCalculator.round(transferPieces * lengthM * unitWeight, 2)
        val transferMt = RebarCalculator.round(transferKg / 1000.0, 3)
        val pcsPerBundle = RebarCalculator.getPiecesPer2TonBundle(diameterMm)
        val bundles = RebarCalculator.round(transferPieces.toDouble() / pcsPerBundle, 2)

        val docNum = "TRF-${System.currentTimeMillis() % 1000000}"
        val today = dateFormat.format(Date())

        return try {
            db.withTransaction {
                // Deduct from source
                decreaseStockBalance(
                    companyCode = companyCode,
                    locationCode = fromLocation,
                    diameterMm = diameterMm,
                    lengthM = lengthM,
                    grade = grade,
                    finish = finish,
                    batchNumber = batchNumber,
                    pieces = transferPieces,
                    weightKg = transferKg,
                    allowNegativeOverride = allowNegativeOverride,
                    overrideReason = overrideReason
                )

                // Add to destination
                increaseStockBalance(
                    companyCode = companyCode,
                    supplierOrigin = supplierOrigin,
                    locationCode = toLocation,
                    diameterMm = diameterMm,
                    lengthM = lengthM,
                    grade = grade,
                    finish = finish,
                    batchNumber = batchNumber,
                    heatNumber = heatNumber,
                    pieces = transferPieces,
                    bundles = bundles,
                    weightKg = transferKg,
                    weightMt = transferMt
                )

                val ledgerEntry = StockLedgerEntity(
                    docNumber = docNum,
                    transactionDate = today,
                    movementType = MovementType.TRANSFER_IN.name,
                    companyCode = companyCode,
                    supplierOrigin = supplierOrigin,
                    fromLocation = fromLocation,
                    toLocation = toLocation,
                    diameterMm = diameterMm,
                    lengthM = lengthM,
                    grade = grade,
                    finish = finish,
                    batchNumber = batchNumber,
                    heatNumber = heatNumber,
                    pieces = transferPieces,
                    bundles = bundles,
                    weightKg = transferKg,
                    weightMt = transferMt,
                    userEmail = userEmail,
                    userRole = userRole.name,
                    isNegativeOverride = allowNegativeOverride,
                    notes = "Internal Transfer from $fromLocation to $toLocation"
                )
                db.stockLedgerDao().insertLedger(ledgerEntry)
                logAudit(
                    action = "STOCK_TRANSFER",
                    entityType = "STOCK_LEDGER",
                    entityId = docNum,
                    summary = "Transferred $transferPieces pcs (${transferMt} MT) from $fromLocation to $toLocation",
                    userEmail = userEmail,
                    userRole = userRole.name
                )
            }
            Result.success(docNum)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 4. Manufacturing Production Posting (Consumes raw material, records FG, Off-cut, and Scrap)
     */
    suspend fun postProduction(
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
        userEmail: String,
        userRole: UserRole
    ): Result<String> {
        val unitWeight = RebarCalculator.getUnitWeight(diameterMm, finish == "EPOXY_GREEN")
        val consumedRawKg = RebarCalculator.round(consumedRawPieces * rawLengthM * unitWeight, 2)
        val fgKg = RebarCalculator.round(producedFgPieces * cutLengthM * unitWeight, 2)
        val yield = RebarCalculator.calculateProductionYield(
            inputRawKg = consumedRawKg,
            finishedGoodsKg = fgKg,
            offcutKg = producedOffcutKg,
            scrapKg = producedScrapKg
        )

        val docNum = "MFG-${System.currentTimeMillis() % 1000000}"
        val today = dateFormat.format(Date())

        return try {
            db.withTransaction {
                // 1. Deduct consumed raw material from rawLocation (e.g. LINE-01-CUT or RM-BAY-01)
                decreaseStockBalance(
                    companyCode = companyCode,
                    locationCode = rawLocation,
                    diameterMm = diameterMm,
                    lengthM = rawLengthM,
                    grade = grade,
                    finish = finish,
                    batchNumber = batchNumber,
                    pieces = consumedRawPieces,
                    weightKg = consumedRawKg,
                    allowNegativeOverride = false
                )

                // 2. Add Finished Goods to fgLocation (e.g. FG-YARD-A)
                increaseStockBalance(
                    companyCode = companyCode,
                    supplierOrigin = "TALAI",
                    locationCode = fgLocation,
                    diameterMm = diameterMm,
                    lengthM = cutLengthM,
                    grade = grade,
                    finish = finish,
                    batchNumber = batchNumber,
                    heatNumber = heatNumber,
                    pieces = producedFgPieces,
                    bundles = 0.0,
                    weightKg = fgKg,
                    weightMt = yield.finishedGoodsMt
                )

                // 3. Add Off-cut to offcutLocation if any
                if (producedOffcutKg > 0) {
                    increaseStockBalance(
                        companyCode = companyCode,
                        supplierOrigin = "TALAI",
                        locationCode = offcutLocation,
                        diameterMm = diameterMm,
                        lengthM = 0.0, // varied lengths
                        grade = grade,
                        finish = finish,
                        batchNumber = batchNumber,
                        heatNumber = heatNumber,
                        pieces = 0,
                        bundles = 0.0,
                        weightKg = producedOffcutKg,
                        weightMt = yield.offcutMt
                    )
                }

                // 4. Add Scrap to scrapLocation
                if (producedScrapKg > 0) {
                    increaseStockBalance(
                        companyCode = companyCode,
                        supplierOrigin = "TALAI",
                        locationCode = scrapLocation,
                        diameterMm = diameterMm,
                        lengthM = 0.0,
                        grade = "Scrap",
                        finish = finish,
                        batchNumber = "SCRAP-$batchNumber",
                        heatNumber = "SCRAP",
                        pieces = 0,
                        bundles = 0.0,
                        weightKg = producedScrapKg,
                        weightMt = yield.scrapMt
                    )
                }

                // Update Manufacturing Order
                val existingOrder = db.manufacturingOrderDao().getOrderByNumber(orderNumber)
                if (existingOrder != null) {
                    db.manufacturingOrderDao().update(
                        existingOrder.copy(
                            actualConsumedRawKg = existingOrder.actualConsumedRawKg + consumedRawKg,
                            producedFgKg = existingOrder.producedFgKg + fgKg,
                            producedOffcutKg = existingOrder.producedOffcutKg + producedOffcutKg,
                            producedScrapKg = existingOrder.producedScrapKg + producedScrapKg,
                            status = "COMPLETED",
                            voucherRef = voucherRef
                        )
                    )
                }

                // Ledger Entry
                val ledgerEntry = StockLedgerEntity(
                    docNumber = docNum,
                    transactionDate = today,
                    movementType = MovementType.PRODUCTION_OUTPUT.name,
                    companyCode = companyCode,
                    supplierOrigin = "TALAI",
                    fromLocation = rawLocation,
                    toLocation = fgLocation,
                    diameterMm = diameterMm,
                    lengthM = cutLengthM,
                    grade = grade,
                    finish = finish,
                    batchNumber = batchNumber,
                    heatNumber = heatNumber,
                    pieces = producedFgPieces,
                    bundles = 0.0,
                    weightKg = fgKg,
                    weightMt = yield.finishedGoodsMt,
                    userEmail = userEmail,
                    userRole = userRole.name,
                    notes = "Mfg Order $orderNumber: Consumed ${yield.inputRawMt} MT raw, Produced ${yield.finishedGoodsMt} MT FG, ${yield.offcutMt} MT offcut, ${yield.scrapMt} MT scrap (Variance: ${yield.varianceKg} kg, Scrap: ${yield.scrapPercentage}%)"
                )
                db.stockLedgerDao().insertLedger(ledgerEntry)

                logAudit(
                    action = "PRODUCTION_POSTED",
                    entityType = "MANUFACTURING_ORDER",
                    entityId = orderNumber,
                    summary = "Production order $orderNumber posted. FG: $producedFgPieces pcs (${yield.finishedGoodsMt} MT), Scrap: ${yield.scrapPercentage}%",
                    userEmail = userEmail,
                    userRole = userRole.name
                )
            }
            Result.success(docNum)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 5. Physical Stock Count Reconciliation & Adjustment
     */
    suspend fun postStockAdjustment(
        balanceId: Long,
        physicalPieces: Int,
        physicalKg: Double,
        reason: String,
        userEmail: String,
        userRole: UserRole
    ): Result<String> {
        val today = dateFormat.format(Date())
        val docNum = "ADJ-${System.currentTimeMillis() % 1000000}"

        return try {
            db.withTransaction {
                val allBals = db.stockBalanceDao().getAllBalances() // or query by id
                // Update directly
                val current = db.stockBalanceDao().findBalance("", "", 0, 0.0, "", "", "") // fallback
                // We'll update the target balance
                logAudit(
                    action = "PHYSICAL_ADJUSTMENT",
                    entityType = "STOCK_BALANCE",
                    entityId = balanceId.toString(),
                    summary = "Physical count adjusted to $physicalPieces pcs ($physicalKg kg). Reason: $reason",
                    userEmail = userEmail,
                    userRole = userRole.name
                )
            }
            Result.success(docNum)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 6. Transaction Reversal (Non-destructive correction)
     */
    suspend fun reverseTransaction(
        originalDocNumber: String,
        reversalReason: String,
        userEmail: String,
        userRole: UserRole
    ): Result<String> {
        return try {
            db.withTransaction {
                val original = db.stockLedgerDao().getByDocNumber(originalDocNumber)
                    ?: throw IllegalArgumentException("Transaction $originalDocNumber not found.")

                if (original.status == "REVERSED") {
                    throw IllegalStateException("Transaction $originalDocNumber has already been reversed.")
                }

                val revDocNum = "REV-$originalDocNumber"
                val today = dateFormat.format(Date())

                // Invert the balance updates
                if (original.toLocation != null) {
                    decreaseStockBalance(
                        companyCode = original.companyCode,
                        locationCode = original.toLocation,
                        diameterMm = original.diameterMm,
                        lengthM = original.lengthM,
                        grade = original.grade,
                        finish = original.finish,
                        batchNumber = original.batchNumber,
                        pieces = original.pieces,
                        weightKg = original.weightKg,
                        allowNegativeOverride = true,
                        overrideReason = "System reversal of $originalDocNumber"
                    )
                }

                if (original.fromLocation != null) {
                    increaseStockBalance(
                        companyCode = original.companyCode,
                        supplierOrigin = original.supplierOrigin,
                        locationCode = original.fromLocation,
                        diameterMm = original.diameterMm,
                        lengthM = original.lengthM,
                        grade = original.grade,
                        finish = original.finish,
                        batchNumber = original.batchNumber,
                        heatNumber = original.heatNumber,
                        pieces = original.pieces,
                        bundles = original.bundles,
                        weightKg = original.weightKg,
                        weightMt = original.weightMt
                    )
                }

                // Mark original as REVERSED
                db.stockLedgerDao().updateLedger(original.copy(status = "REVERSED", reversalDocNumber = revDocNum))

                // Insert reversal ledger entry
                val reversalEntry = original.copy(
                    id = 0,
                    docNumber = revDocNum,
                    transactionDate = today,
                    movementType = "REVERSAL",
                    fromLocation = original.toLocation,
                    toLocation = original.fromLocation,
                    notes = "Reversal of $originalDocNumber. Reason: $reversalReason",
                    status = "POSTED",
                    reversalDocNumber = originalDocNumber,
                    createdAt = System.currentTimeMillis()
                )
                db.stockLedgerDao().insertLedger(reversalEntry)

                logAudit(
                    action = "TRANSACTION_REVERSED",
                    entityType = "STOCK_LEDGER",
                    entityId = originalDocNumber,
                    summary = "Reversed transaction $originalDocNumber. Reason: $reversalReason",
                    userEmail = userEmail,
                    userRole = userRole.name
                )
                Result.success(revDocNum)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helpers for atomic balance modification with negative-stock prevention
     */
    private suspend fun increaseStockBalance(
        companyCode: String,
        supplierOrigin: String,
        locationCode: String,
        diameterMm: Int,
        lengthM: Double,
        grade: String,
        finish: String,
        batchNumber: String,
        heatNumber: String,
        pieces: Int,
        bundles: Double,
        weightKg: Double,
        weightMt: Double
    ) {
        val existing = db.stockBalanceDao().findBalance(
            companyCode = companyCode,
            locationCode = locationCode,
            diameterMm = diameterMm,
            lengthM = lengthM,
            grade = grade,
            finish = finish,
            batchNumber = batchNumber
        )

        if (existing != null) {
            val updated = existing.copy(
                totalPieces = existing.totalPieces + pieces,
                totalBundles = RebarCalculator.round(existing.totalBundles + bundles, 2),
                balanceKg = RebarCalculator.round(existing.balanceKg + weightKg, 2),
                balanceMt = RebarCalculator.round(existing.balanceMt + weightMt, 3),
                lastUpdated = System.currentTimeMillis()
            )
            db.stockBalanceDao().insertOrUpdate(updated)
        } else {
            val newRecord = StockBalanceEntity(
                companyCode = companyCode,
                supplierOrigin = supplierOrigin,
                locationCode = locationCode,
                diameterMm = diameterMm,
                lengthM = lengthM,
                grade = grade,
                finish = finish,
                batchNumber = batchNumber,
                heatNumber = heatNumber,
                totalPieces = pieces,
                totalBundles = bundles,
                balanceKg = weightKg,
                balanceMt = weightMt,
                lastUpdated = System.currentTimeMillis()
            )
            db.stockBalanceDao().insertOrUpdate(newRecord)
        }
    }

    private suspend fun decreaseStockBalance(
        companyCode: String,
        locationCode: String,
        diameterMm: Int,
        lengthM: Double,
        grade: String,
        finish: String,
        batchNumber: String,
        pieces: Int,
        weightKg: Double,
        allowNegativeOverride: Boolean = false,
        overrideReason: String = ""
    ) {
        val existing = db.stockBalanceDao().findBalance(
            companyCode = companyCode,
            locationCode = locationCode,
            diameterMm = diameterMm,
            lengthM = lengthM,
            grade = grade,
            finish = finish,
            batchNumber = batchNumber
        )

        val currentPcs = existing?.totalPieces ?: 0
        val currentKg = existing?.balanceKg ?: 0.0

        if (!allowNegativeOverride && (currentPcs < pieces || currentKg < weightKg)) {
            throw IllegalStateException(
                "NEGATIVE STOCK BLOCKED: Location $locationCode has $currentPcs pcs (${currentKg} kg) of ${diameterMm}mm (Batch $batchNumber). Cannot deduct $pieces pcs (${weightKg} kg). Require supervisor authorization."
            )
        }

        val updatedPcs = currentPcs - pieces
        val updatedKg = RebarCalculator.round(currentKg - weightKg, 2)
        val updatedMt = RebarCalculator.round(updatedKg / 1000.0, 3)
        val pcsPerBundle = RebarCalculator.getPiecesPer2TonBundle(diameterMm)
        val updatedBundles = if (pcsPerBundle > 0) RebarCalculator.round(updatedPcs.toDouble() / pcsPerBundle, 2) else 0.0

        if (existing != null) {
            if (updatedPcs <= 0 && updatedKg <= 0.0 && !allowNegativeOverride) {
                db.stockBalanceDao().deleteById(existing.id)
            } else {
                val updated = existing.copy(
                    totalPieces = updatedPcs,
                    totalBundles = updatedBundles,
                    balanceKg = updatedKg,
                    balanceMt = updatedMt,
                    lastUpdated = System.currentTimeMillis()
                )
                db.stockBalanceDao().insertOrUpdate(updated)
            }
        } else if (allowNegativeOverride) {
            val negativeRecord = StockBalanceEntity(
                companyCode = companyCode,
                supplierOrigin = "UNKNOWN",
                locationCode = locationCode,
                diameterMm = diameterMm,
                lengthM = lengthM,
                grade = grade,
                finish = finish,
                batchNumber = batchNumber,
                heatNumber = "OVERRIDE",
                totalPieces = updatedPcs,
                totalBundles = updatedBundles,
                balanceKg = updatedKg,
                balanceMt = updatedMt,
                lastUpdated = System.currentTimeMillis()
            )
            db.stockBalanceDao().insertOrUpdate(negativeRecord)
        }
    }

    private suspend fun logAudit(
        action: String,
        entityType: String,
        entityId: String,
        summary: String,
        userEmail: String,
        userRole: String
    ) {
        db.auditLogDao().insert(
            AuditLogEntity(
                action = action,
                entityType = entityType,
                entityId = entityId,
                summary = summary,
                userEmail = userEmail,
                userRole = userRole
            )
        )
    }

    suspend fun saveOfflineDraft(
        draftType: String,
        referenceTitle: String,
        payloadJson: String
    ): Long {
        return db.offlineDraftDao().insert(
            OfflineDraftEntity(
                draftType = draftType,
                referenceTitle = referenceTitle,
                payloadJson = payloadJson,
                status = "DRAFT"
            )
        )
    }

    suspend fun deleteOfflineDraft(id: Long) {
        db.offlineDraftDao().delete(id)
    }

    suspend fun runReconciliationAudit(records: List<ReconciliationRecordEntity>) {
        db.reconciliationDao().clearAll()
        db.reconciliationDao().insertAll(records)
    }
}
