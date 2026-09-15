package com.pharmachain.ai.feature.forecasting.engine

import com.pharmachain.ai.core.model.ForecastResult
import com.pharmachain.ai.core.model.MedicationWithListings
import com.pharmachain.ai.core.model.ProductListing
import com.pharmachain.ai.core.model.SmartCartItem
import java.util.UUID

object SmartCartScoringEngine {
    // Pure function adhering to Section 8.4:
    // cartItemScore = (0.6 × priceScore) + (0.4 × reputationScore)
    fun calculateCartScore(
        listing: ProductListing,
        minPriceInSet: Double,
        maxPriceInSet: Double,
        epsilon: Double = 0.0001
    ): Double {
        val priceDiff = maxPriceInSet - minPriceInSet
        val priceScore = if (priceDiff <= epsilon) {
            1.0
        } else {
            (1.0 - ((listing.price - minPriceInSet) / (priceDiff + epsilon))).coerceIn(0.0, 1.0)
        }
        val reputationScore = (listing.distributorReputation / 5.0).coerceIn(0.0, 1.0)
        return (0.6 * priceScore) + (0.4 * reputationScore)
    }

    fun generateSmartCart(
        jobId: String,
        forecastResults: List<ForecastResult>,
        medicationsWithListings: List<MedicationWithListings>,
        maxBudgetEGP: Double
    ): List<SmartCartItem> {
        val items = mutableListOf<SmartCartItem>()

        for (forecast in forecastResults) {
            val medData = medicationsWithListings.find { it.medication.id == forecast.medicationId }
            val candidateListings = medData?.listings ?: emptyList()
            if (candidateListings.isEmpty()) continue

            // Hard filter: stockQuantity >= predictedQuantity
            val eligibleListings = candidateListings.filter { it.stockQuantity >= forecast.predictedQuantity }
            val listingsToScore = if (eligibleListings.isNotEmpty()) eligibleListings else candidateListings

            val minPrice = listingsToScore.minOf { it.price }
            val maxPrice = listingsToScore.maxOf { it.price }

            val topListing = listingsToScore.maxByOrNull { listing ->
                calculateCartScore(listing, minPrice, maxPrice)
            } ?: candidateListings.first()

            val lineTotal = topListing.price * forecast.predictedQuantity
            val rationale = "Best offer (%.2f EGP), %.1f★ rating from %s".format(
                topListing.price,
                topListing.distributorReputation,
                topListing.distributorName
            )

            items.add(
                SmartCartItem(
                    id = "cart_item_${UUID.randomUUID().toString().take(8)}",
                    jobId = jobId,
                    medicationId = forecast.medicationId,
                    medicationName = forecast.medicationName,
                    selectedProductListingId = topListing.id,
                    distributorId = topListing.distributorId,
                    distributorName = topListing.distributorName,
                    quantity = forecast.predictedQuantity,
                    unitPrice = topListing.price,
                    lineTotal = lineTotal,
                    distributorReputationAtSelection = topListing.distributorReputation,
                    scoringRationale = rationale,
                    isSelected = true
                )
            )
        }

        // ARCHITECT DECISION: If total exceeds maxBudgetEGP, sort items by priority (forecast confidence * quantity)
        // and deselect lowest-priority items until total is under budget, leaving manual toggle affordances for the pharmacist
        var runningTotal = items.sumOf { it.lineTotal }
        if (runningTotal > maxBudgetEGP) {
            val forecastMap = forecastResults.associateBy { it.medicationId }
            // Sort ascending by priority so lowest priority is trimmed first
            val sortedByAscendingPriority = items.sortedBy { item ->
                val f = forecastMap[item.medicationId]
                val confidence = f?.confidenceScore ?: 0.5
                confidence * item.quantity
            }

            val finalItems = items.toMutableList()
            for (lowPriorityItem in sortedByAscendingPriority) {
                if (runningTotal <= maxBudgetEGP) break
                val index = finalItems.indexOfFirst { it.id == lowPriorityItem.id }
                if (index != -1) {
                    finalItems[index] = finalItems[index].copy(isSelected = false)
                    runningTotal -= lowPriorityItem.lineTotal
                }
            }
            return finalItems
        }

        return items
    }
}
