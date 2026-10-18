package com.example

import com.example.data.calculator.RebarCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testWeightTo2TonBundlesCalculation() {
        // Diameter 12mm: 188 pcs/2T bundle, 0.888 kg/m, 12m bar = 10.656 kg
        // Input: 10020 kg
        val result = RebarCalculator.calculateWeightTo2TonBundles(
            diameterMm = 12,
            weightKg = 10020.0,
            lengthM = 12.0
        )
        // Expected total pieces ~ 940 pcs
        assertEquals(940, result.totalPieces)
        assertEquals(5, result.full2TonBundles)
        assertEquals(0, result.loosePieces)
        assertEquals(188, result.standardBundlePcs)
        assertEquals(10016.64, result.exactCalculatedWeightKg, 0.5)
    }

    @Test
    fun testPiecesTo2TonAndBbsConversion() {
        // Diameter 16mm: 106 pcs/2T bundle, 1.578 kg/m, 12m bar = 18.936 kg
        // 250 pieces
        val result = RebarCalculator.calculatePiecesTo2TonAndBbs(
            diameterMm = 16,
            totalPieces = 250,
            standardLengthM = 12.0,
            bbsCutLengthM = 6.0
        )
        assertEquals(2, result.receivingFullBundles)
        assertEquals(38, result.receivingLoosePieces)
        assertEquals(2.358, result.receivingFractionalBundles, 0.01)
        assertEquals(4734.0, result.receivingWeightKg, 1.0)

        // BBS delivery check
        assertEquals(1500.0, result.bbsTotalLengthM, 0.1)
        assertEquals(2367.0, result.bbsWeightKg, 1.0)
        assertEquals(2, result.bbsBundleCount)
        assertEquals(38, result.bbsLoosePieces)
    }

    @Test
    fun testReceiving2TonCalculation() {
        // 5 full bundles of 12mm + 10 loose pieces
        val result = RebarCalculator.calculateReceiving2Ton(
            diameterMm = 12,
            lengthM = 12.0,
            fullBundles = 5,
            loosePieces = 10
        )
        // 5 * 188 + 10 = 950 pieces
        assertEquals(950, result.totalPieces)
        assertTrue(result.theoreticalWeightKg > 10000.0)
    }

    @Test
    fun testBbsDeliveryCalculation() {
        // Diameter 20mm, 4.5m cut length, 100 required pieces
        val result = RebarCalculator.calculateBbsDelivery(
            diameterMm = 20,
            cutLengthM = 4.5,
            requiredPieces = 100
        )
        assertEquals(100, result.requiredPieces)
        assertEquals(450.0, result.totalLengthM, 0.1)
        assertEquals(1109.7, result.weightKg, 1.0)
        assertEquals(1.11, result.weightMt, 0.01)
    }
}
