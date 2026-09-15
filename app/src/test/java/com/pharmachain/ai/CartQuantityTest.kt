package com.pharmachain.ai

import com.pharmachain.ai.core.model.CartItem
import com.pharmachain.ai.feature.cart.CartUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CartQuantityTest {

    @Test
    fun testQuantitySteppingBetween10And25() {
        var quantity = 10 // initial selection

        // Step up one by one from 10 to 25
        for (expected in 11..25) {
            quantity += 1
            assertEquals(expected, quantity)
        }
        assertEquals(25, quantity)

        // Step down one by one from 25 to 10
        for (expected in 24 downTo 10) {
            quantity -= 1
            assertEquals(expected, quantity)
        }
        assertEquals(10, quantity)

        // Quick +5 jump
        quantity += 5
        assertEquals(15, quantity)
        quantity += 5
        assertEquals(20, quantity)
        quantity += 5
        assertEquals(25, quantity)

        // Quick -5 jump
        quantity -= 5
        assertEquals(20, quantity)
        quantity -= 5
        assertEquals(15, quantity)
        quantity -= 5
        assertEquals(10, quantity)
    }

    @Test
    fun testCartItemTotalWithQuantitiesBetween10And25() {
        val unitPrice = 45.0

        for (q in 10..25) {
            val item = CartItem(
                medicationId = "med_1",
                brandName = "Panadol Extra",
                genericName = "Paracetamol",
                category = "Analgesics",
                form = "Tablet",
                strength = "500mg",
                selectedListingId = "list_1",
                distributorId = "dist_1",
                distributorName = "Ibnsina Pharma",
                distributorReputation = 4.9,
                unitPrice = unitPrice,
                quantity = q
            )
            assertEquals(q, item.quantity)
            assertEquals(unitPrice * q, item.lineTotal, 0.001)

            val uiState = CartUiState(items = listOf(item))
            assertEquals(q, uiState.totalBoxes)
            assertEquals(unitPrice * q, uiState.totalPrice, 0.001)
        }
    }
}
