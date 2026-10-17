package com.example

import com.example.data.calculator.RebarCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Automated Verification of Steel Rebar Calculations & Rules
 * Covers:
 * 1. 'Calculator Issued 2 TON QTY' standard 2-Ton bundle piece counts and weights
 * 2. 'Calculator Issued BBS QTY' customer delivery calculations
 * 3. Actual weighbridge vs theoretical weight variance
 * 4. Production cutting & bending yield and scrap calculations
 */
class RebarCalculationAndInventoryTest {

    @Test
    fun testStandard2TonBundlePiecesPerDiameter() {
        // Test standard piece counts for all diameters according to factory specification
        assertEquals(422, RebarCalculator.getPiecesPer2TonBundle(8))
        assertEquals(270, RebarCalculator.getPiecesPer2TonBundle(10))
        assertEquals(188, RebarCalculator.getPiecesPer2TonBundle(12))
        assertEquals(138, RebarCalculator.getPiecesPer2TonBundle(14))
        assertEquals(106, RebarCalculator.getPiecesPer2TonBundle(16))
        assertEquals(83, RebarCalculator.getPiecesPer2TonBundle(18))
        assertEquals(68, RebarCalculator.getPiecesPer2TonBundle(20))
        assertEquals(56, RebarCalculator.getPiecesPer2TonBundle(22))
        assertEquals(43, RebarCalculator.getPiecesPer2TonBundle(25))
        assertEquals(26, RebarCalculator.getPiecesPer2TonBundle(32))
    }

    @Test
    fun testStandard2TonBundleWeightsNominal2Tons() {
        // Check that 1 standard bundle of each diameter is approximately 2.000 MT (+/- 1.5%)
        val diameters = listOf(8, 10, 12, 14, 16, 18, 20, 22, 25, 32)
        for (d in diameters) {
            val res = RebarCalculator.calculateReceiving2Ton(
                diameterMm = d,
                lengthM = 12.0,
                fullBundles = 1,
                looseBundles = 0,
                loosePieces = 0,
                isEpoxy = false
            )
            // Each standard bundle must be within 1.95 MT and 2.05 MT
            assertTrue(
                "Bundle for $d mm should be ~2.0 MT, was ${res.theoreticalWeightMt} MT",
                res.theoreticalWeightMt in 1.95..2.05
            )
        }
    }

    @Test
    fun testReceivingCalculation12mmFiveBundles() {
        // 5 bundles of 12mm (188 pcs per bundle) = 940 pcs
        // 940 pcs * 12.0m * 0.888 kg/m = 10,016.64 kg = 10.017 MT
        val res = RebarCalculator.calculateReceiving2Ton(
            diameterMm = 12,
            lengthM = 12.0,
            fullBundles = 5,
            looseBundles = 0,
            loosePieces = 0,
            isEpoxy = false,
            actualWeightKg = 10020.00
        )

        assertEquals(940, res.totalPieces)
        assertEquals(10016.64, res.theoreticalWeightKg, 0.01)
        assertEquals(10.017, res.theoreticalWeightMt, 0.001)

        // Weighbridge scale verification
        assertNotNull(res.actualWeightKg)
        assertEquals(10020.00, res.actualWeightKg!!, 0.01)
        assertEquals(3.36, res.weightVarianceKg!!, 0.01)
        assertFalse(res.isEstimated)
    }

    @Test
    fun testReceivingCalculationWithLoosePieces() {
        // 2 bundles of 16mm (106 pcs/bnd) + 10 loose pieces = 212 + 10 = 222 pieces
        // 222 * 12.0m * 1.578 kg/m = 4203.80 kg = 4.204 MT
        val res = RebarCalculator.calculateReceiving2Ton(
            diameterMm = 16,
            lengthM = 12.0,
            fullBundles = 2,
            looseBundles = 0,
            loosePieces = 10,
            isEpoxy = false
        )

        assertEquals(222, res.totalPieces)
        assertEquals(4203.80, res.theoreticalWeightKg, 0.05)
        assertEquals(4.204, res.theoreticalWeightMt, 0.001)
        assertTrue(res.isEstimated)
    }

    @Test
    fun testBbsCustomerDeliveryCalculation() {
        // 150 pieces of 16mm cut to 5.80m length
        // Total length = 150 * 5.80 = 870.00m
        // Unit weight 1.578 kg/m -> 870 * 1.578 = 1372.86 kg = 1.373 MT
        val bbs = RebarCalculator.calculateBbsDelivery(
            diameterMm = 16,
            cutLengthM = 5.80,
            requiredPieces = 150,
            isEpoxy = false
        )

        assertEquals(150, bbs.requiredPieces)
        assertEquals(5.80, bbs.cutLengthM, 0.001)
        assertEquals(870.00, bbs.totalLengthM, 0.01)
        assertEquals(1.578, bbs.unitWeightKgPerM, 0.001)
        assertEquals(1372.86, bbs.weightKg, 0.01)
        assertEquals(1.373, bbs.weightMt, 0.001)

        // 150 pcs with standard 16mm bundle size of 106 pcs -> 1 bundle + 44 loose pcs
        assertEquals(1, bbs.bundleCount)
        assertEquals(44, bbs.loosePieces)
    }

    @Test
    fun testEpoxyCoatingFactor() {
        // Black steel unit weight for 12mm is 0.888 kg/m
        val blackUnit = RebarCalculator.getUnitWeight(12, isEpoxy = false)
        assertEquals(0.888, blackUnit, 0.001)

        // Epoxy coated adds 1.5% factor -> 0.888 * 1.015 = 0.9013 kg/m
        val epoxyUnit = RebarCalculator.getUnitWeight(12, isEpoxy = true)
        assertEquals(0.9013, epoxyUnit, 0.001)
        assertTrue(epoxyUnit > blackUnit)
    }

    @Test
    fun testProductionYieldAndScrapCalculation() {
        // Input: 1000.0 kg raw bar consumed
        // Output: 900.0 kg Finished Goods + 60.0 kg usable off-cuts + 40.0 kg scrap
        // Total accounted: 900 + 60 + 40 = 1000.0 kg -> Variance = 0.0 kg
        // Scrap percentage: 40 / 1000 = 4.0%
        val yield = RebarCalculator.calculateProductionYield(
            inputRawKg = 1000.0,
            finishedGoodsKg = 900.0,
            offcutKg = 60.0,
            scrapKg = 40.0
        )

        assertEquals(1000.0, yield.inputRawKg, 0.01)
        assertEquals(900.0, yield.finishedGoodsKg, 0.01)
        assertEquals(60.0, yield.offcutKg, 0.01)
        assertEquals(40.0, yield.scrapKg, 0.01)
        assertEquals(0.0, yield.varianceKg, 0.01)
        assertEquals(4.0, yield.scrapPercentage, 0.01)
    }
}
