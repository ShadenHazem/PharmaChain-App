package com.pharmachain.ai.feature.catalog

import com.pharmachain.ai.core.model.ProductListing

object ProductSortEngine {
    // Pure function adhering to Section 8.2:
    // listingScore = (0.55 × priceScore) + (0.35 × reputationScore) + (0.10 × stockScore)
    // priceScore = 1 - ((price - minPriceInSet) / (maxPriceInSet - minPriceInSet + ε))
    // reputationScore = distributor.reputationScore / 5.0
    // stockScore = min(1.0, stockQuantity / referenceQty) (referenceQty = 50.0)
    fun calculateListingScore(
        listing: ProductListing,
        minPriceInSet: Double,
        maxPriceInSet: Double,
        referenceQty: Double = 50.0,
        epsilon: Double = 0.0001
    ): Double {
        val priceDiff = maxPriceInSet - minPriceInSet
        val priceScore = if (priceDiff <= epsilon) {
            1.0
        } else {
            (1.0 - ((listing.price - minPriceInSet) / (priceDiff + epsilon))).coerceIn(0.0, 1.0)
        }

        val reputationScore = (listing.distributorReputation / 5.0).coerceIn(0.0, 1.0)
        val stockScore = (listing.stockQuantity.toDouble() / referenceQty).coerceIn(0.0, 1.0)

        return (0.55 * priceScore) + (0.35 * reputationScore) + (0.10 * stockScore)
    }

    fun rankListings(
        listings: List<ProductListing>,
        referenceQty: Double = 50.0
    ): List<ProductListing> {
        if (listings.isEmpty()) return emptyList()
        if (listings.size == 1) return listings

        val minPrice = listings.minOf { it.price }
        val maxPrice = listings.maxOf { it.price }

        return listings.sortedByDescending { listing ->
            calculateListingScore(
                listing = listing,
                minPriceInSet = minPrice,
                maxPriceInSet = maxPrice,
                referenceQty = referenceQty
            )
        }
    }
}
