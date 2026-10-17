package com.example.data.calculator

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt

/**
 * Standard Rebar Specification and Calculation Engine
 * Rebar Weight Calculator for TALAI AL-FOLATH FOR MANUFACTURING CO.
 * Implements:
 * 1. 2-TON BUNDLE Calculation (Raw Material Receiving)
 * 2. BBS Calculation (Customer Delivery / Bar Bending Schedule)
 */
object RebarCalculator {

    // Standard theoretical weights per meter according to ASTM A615 / BS 4449
    // Formula: d^2 / 162 (approx) or standard manufacturer catalog values:
    val STANDARD_UNIT_WEIGHTS: Map<Int, Double> = mapOf(
        8 to 0.395,
        10 to 0.617,
        12 to 0.888,
        14 to 1.208,
        16 to 1.578,
        18 to 1.998,
        20 to 2.466,
        22 to 2.984,
        25 to 3.853,
        32 to 6.313
    )

    // Standard 2-Ton Bundle pieces specification for 12.00 meter standard bars
    // Based on factory reference 'Calculator Issued 2 TON QTY'
    val STANDARD_2TON_BUNDLE_PCS: Map<Int, Int> = mapOf(
        8 to 422,
        10 to 270,
        12 to 188,
        14 to 138,
        16 to 106,
        18 to 83,
        20 to 68,
        22 to 56,
        25 to 43,
        32 to 26
    )

    const val EPOXY_COATING_WEIGHT_FACTOR = 1.015 // +1.5% for epoxy protective layer

    fun getUnitWeight(diameterMm: Int, isEpoxy: Boolean = false): Double {
        val base = STANDARD_UNIT_WEIGHTS[diameterMm] ?: (diameterMm * diameterMm / 162.0)
        return if (isEpoxy) round(base * EPOXY_COATING_WEIGHT_FACTOR, 4) else base
    }

    fun getPiecesPer2TonBundle(diameterMm: Int): Int {
        return STANDARD_2TON_BUNDLE_PCS[diameterMm] ?: 100
    }

    /**
     * Raw Material Receiving Calculation (Calculator Issued 2 TON QTY)
     */
    data class ReceivingCalculationResult(
        val totalPieces: Int,
        val theoreticalWeightKg: Double,
        val theoreticalWeightMt: Double,
        val actualWeightKg: Double?,
        val actualWeightMt: Double?,
        val weightVarianceKg: Double?,
        val weightVariancePercent: Double?,
        val isEstimated: Boolean
    )

    fun calculateReceiving2Ton(
        diameterMm: Int,
        lengthM: Double = 12.0,
        fullBundles: Int,
        looseBundles: Int = 0,
        loosePieces: Int = 0,
        isEpoxy: Boolean = false,
        actualWeightKg: Double? = null
    ): ReceivingCalculationResult {
        val pcsPerBundle = getPiecesPer2TonBundle(diameterMm)
        val totalPieces = (fullBundles * pcsPerBundle) + (looseBundles * pcsPerBundle) + loosePieces
        val unitWeight = getUnitWeight(diameterMm, isEpoxy)

        val theoreticalWeightKg = round(totalPieces * lengthM * unitWeight, 2)
        val theoreticalWeightMt = round(theoreticalWeightKg / 1000.0, 3)

        val (actMt, varianceKg, variancePct) = if (actualWeightKg != null && actualWeightKg > 0.0) {
            val aKg = round(actualWeightKg, 2)
            val aMt = round(aKg / 1000.0, 3)
            val vKg = round(aKg - theoreticalWeightKg, 2)
            val vPct = if (theoreticalWeightKg > 0) round((vKg / theoreticalWeightKg) * 100.0, 2) else 0.0
            Triple(aMt, vKg, vPct)
        } else {
            Triple(null, null, null)
        }

        return ReceivingCalculationResult(
            totalPieces = totalPieces,
            theoreticalWeightKg = theoreticalWeightKg,
            theoreticalWeightMt = theoreticalWeightMt,
            actualWeightKg = actualWeightKg?.let { round(it, 2) },
            actualWeightMt = actMt,
            weightVarianceKg = varianceKg,
            weightVariancePercent = variancePct,
            isEstimated = actualWeightKg == null || actualWeightKg <= 0.0
        )
    }

    /**
     * Customer Delivery / BBS Calculation (Calculator Issued BBS QTY)
     */
    data class BbsDeliveryCalculationResult(
        val requiredPieces: Int,
        val cutLengthM: Double,
        val totalLengthM: Double,
        val unitWeightKgPerM: Double,
        val weightKg: Double,
        val weightMt: Double,
        val bundleCount: Int,
        val loosePieces: Int
    )

    fun calculateBbsDelivery(
        diameterMm: Int,
        cutLengthM: Double,
        requiredPieces: Int,
        isEpoxy: Boolean = false,
        customBundlePcs: Int? = null
    ): BbsDeliveryCalculationResult {
        val unitWeight = getUnitWeight(diameterMm, isEpoxy)
        val totalLengthM = round(requiredPieces * cutLengthM, 2)
        val weightKg = round(totalLengthM * unitWeight, 2)
        val weightMt = round(weightKg / 1000.0, 3)

        val bundleSize = customBundlePcs ?: getPiecesPer2TonBundle(diameterMm)
        val bundleCount = if (bundleSize > 0) requiredPieces / bundleSize else 0
        val loosePcs = if (bundleSize > 0) requiredPieces % bundleSize else requiredPieces

        return BbsDeliveryCalculationResult(
            requiredPieces = requiredPieces,
            cutLengthM = cutLengthM,
            totalLengthM = totalLengthM,
            unitWeightKgPerM = unitWeight,
            weightKg = weightKg,
            weightMt = weightMt,
            bundleCount = bundleCount,
            loosePieces = loosePcs
        )
    }

    /**
     * Production Cutting & Bending Yield / Scrap Calculation
     */
    data class ProductionYieldResult(
        val inputRawKg: Double,
        val inputRawMt: Double,
        val finishedGoodsKg: Double,
        val finishedGoodsMt: Double,
        val offcutKg: Double,
        val offcutMt: Double,
        val scrapKg: Double,
        val scrapMt: Double,
        val varianceKg: Double,
        val scrapPercentage: Double
    )

    fun calculateProductionYield(
        inputRawKg: Double,
        finishedGoodsKg: Double,
        offcutKg: Double,
        scrapKg: Double
    ): ProductionYieldResult {
        val accountedKg = finishedGoodsKg + offcutKg + scrapKg
        val varianceKg = round(inputRawKg - accountedKg, 2)
        val scrapPct = if (inputRawKg > 0.0) round((scrapKg / inputRawKg) * 100.0, 2) else 0.0

        return ProductionYieldResult(
            inputRawKg = round(inputRawKg, 2),
            inputRawMt = round(inputRawKg / 1000.0, 3),
            finishedGoodsKg = round(finishedGoodsKg, 2),
            finishedGoodsMt = round(finishedGoodsKg / 1000.0, 3),
            offcutKg = round(offcutKg, 2),
            offcutMt = round(offcutKg / 1000.0, 3),
            scrapKg = round(scrapKg, 2),
            scrapMt = round(scrapKg / 1000.0, 3),
            varianceKg = varianceKg,
            scrapPercentage = scrapPct
        )
    }

    fun round(value: Double, places: Int): Double {
        if (value.isNaN() || value.isInfinite()) return 0.0
        return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).toDouble()
    }
}
