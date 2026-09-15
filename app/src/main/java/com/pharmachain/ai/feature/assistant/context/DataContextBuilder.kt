package com.pharmachain.ai.feature.assistant.context

import com.pharmachain.ai.core.model.ForecastResult
import com.pharmachain.ai.core.model.MedicationWithListings
import com.pharmachain.ai.core.model.Order
import com.pharmachain.ai.core.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * DataContextBuilder formats fetched Room entity data into compact, labeled,
 * minimal-token text blocks for the Gemini model.
 */
object DataContextBuilder {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    fun buildUserHeader(user: User?, pharmacyName: String? = null): String {
        val name = user?.fullName ?: "Licensed Pharmacist"
        val pharmacy = pharmacyName ?: "Community Pharmacy"
        return "CURRENT USER: Pharmacist $name ($pharmacy)\nROLE: Licensed Pharmacy Buyer (100% Free Tier)"
    }

    fun buildOrdersContext(orders: List<Order>, maxOrders: Int = 5): String {
        if (orders.isEmpty()) {
            return "ORDERS CONTEXT: No orders found for this pharmacy account yet."
        }
        val sb = StringBuilder()
        sb.appendLine("RECENT ORDERS (last ${minOf(orders.size, maxOrders)}):")
        orders.take(maxOrders).forEach { order ->
            val dateStr = try {
                dateFormat.format(Date(order.placedAt))
            } catch (e: Exception) {
                "Recent"
            }
            sb.appendLine("- Order #${order.id} | Placed: $dateStr | Status: ${order.status.name} | Distributor: ${order.distributorName} | Total: ${"%.2f".format(order.totalAmount)} EGP")
            if (order.items.isNotEmpty()) {
                val itemsSummary = order.items.joinToString(", ") { "${it.medicationName} (x${it.quantity} @ ${"%.2f".format(it.unitPriceAtOrder)} EGP)" }
                sb.appendLine("  Items: $itemsSummary")
            }
            if (!order.deliveryAddress.isNullOrBlank()) {
                sb.appendLine("  Delivery Address: ${order.deliveryAddress}")
            }
        }
        return sb.toString().trimEnd()
    }

    fun buildSpendSummaryContext(orders: List<Order>): String {
        if (orders.isEmpty()) {
            return "SPENDING SUMMARY: Total 0.00 EGP across 0 orders."
        }
        val totalSpend = orders.sumOf { it.totalAmount }
        val orderCount = orders.size
        val byDistributor = orders.groupBy { it.distributorName }
            .map { (distName, list) -> "$distName: ${"%.2f".format(list.sumOf { it.totalAmount })} EGP (${list.size} orders)" }
            .joinToString("; ")

        val completedSpend = orders.filter { it.status.name == "DELIVERED" }.sumOf { it.totalAmount }
        val pendingSpend = orders.filter { it.status.name == "PENDING_APPROVAL" || it.status.name == "CONFIRMED" || it.status.name == "SHIPPED" }.sumOf { it.totalAmount }

        return """SPENDING SUMMARY:
- Total Lifetime/Recorded Spend: ${"%.2f".format(totalSpend)} EGP across $orderCount orders
- Delivered/Completed Spend: ${"%.2f".format(completedSpend)} EGP
- Active/In-Flight Spend: ${"%.2f".format(pendingSpend)} EGP
- Distributor Breakdown: $byDistributor""".trimIndent()
    }

    fun buildCatalogContext(catalogItems: List<MedicationWithListings>, queryFilter: String? = null, maxItems: Int = 8): String {
        val filtered = if (!queryFilter.isNullOrBlank()) {
            catalogItems.filter {
                it.medication.brandName.contains(queryFilter, ignoreCase = true) ||
                it.medication.genericName.contains(queryFilter, ignoreCase = true) ||
                it.medication.category.contains(queryFilter, ignoreCase = true)
            }
        } else {
            catalogItems
        }

        if (filtered.isEmpty()) {
            return "CATALOG CONTEXT: No matching pharmaceutical products found."
        }

        val sb = StringBuilder()
        sb.appendLine("CATALOG & DISTRIBUTOR PRICING SNAPSHOT (Showing top ${minOf(filtered.size, maxItems)} items):")
        filtered.take(maxItems).forEach { item ->
            val topOffer = item.topOffer
            val offersSummary = if (item.listings.isNotEmpty()) {
                item.listings.take(3).joinToString(", ") { l ->
                    "${l.distributorName}: ${"%.2f".format(l.price)} EGP (Stock: ${l.stockQuantity}, Rating: ${l.distributorReputation}★)"
                }
            } else {
                "No active distributor listings"
            }
            sb.appendLine("- ${item.medication.brandName} (${item.medication.genericName}) [${item.medication.category}]")
            sb.appendLine("  Best Offer: ${topOffer?.distributorName ?: "N/A"} @ ${topOffer?.let { "%.2f".format(it.price) + " EGP" } ?: "N/A"} (Stock: ${topOffer?.stockQuantity ?: 0})")
            sb.appendLine("  All Offers: $offersSummary")
        }
        return sb.toString().trimEnd()
    }

    fun buildForecastContext(forecastResults: List<ForecastResult>, maxResults: Int = 6): String {
        if (forecastResults.isEmpty()) {
            return "DEMAND FORECAST CONTEXT: No active forecast calculation found in cache. The pharmacist can run an AI forecast by uploading a POS sales Excel/CSV or entering monthly needs manually."
        }
        val sb = StringBuilder()
        sb.appendLine("LATEST AI DEMAND PROJECTION RESULTS:")
        forecastResults.take(maxResults).forEach { res ->
            val surgeNote = if (res.seasonalTrendDetected) " [SEASONAL SURGE DETECTED]" else ""
            sb.appendLine("- ${res.medicationName} (${res.category}): Projected Demand = ${res.predictedQuantity} units | Confidence = ${"%.0f".format(res.confidenceScore * 100)}%$surgeNote")
        }
        return sb.toString().trimEnd()
    }

    fun buildAppHowToContext(): String {
        return """PLATFORM FEATURES & HOW-TO GUIDE:
1. Ordering: Search catalog by brand name or generic INN, compare distributor prices/ratings, click 'Direct Order' or use AI Smart Cart.
2. Demand Forecasting: Go to Forecasting tab -> Upload POS/ERP sales report (Excel .xlsx or CSV) or enter manual needs -> AI validates headers, projects monthly demand, and builds an optimized multi-distributor Smart Cart.
3. Pricing & Economics: The platform is 100% free for pharmacists. No subscription fees or buyer markups.
4. Order Tracking: View order status (Pending Approval -> Confirmed -> Shipped -> Delivered) with live line items and delivery addresses.
5. Distributors: Verified Egyptian licensed pharmaceutical distributors (e.g. Ibnsina Pharma, United Pharmacists UCP, Ramco, Soficopharm)."""
    }

    fun buildGeneralSnapshot(
        user: User?,
        pharmacyName: String?,
        orders: List<Order>,
        catalogSample: List<MedicationWithListings>,
        forecastResults: List<ForecastResult>
    ): String {
        val userHeader = buildUserHeader(user, pharmacyName)
        val ordersBlock = buildOrdersContext(orders, maxOrders = 3)
        val spendBlock = buildSpendSummaryContext(orders)
        val catalogBlock = buildCatalogContext(catalogSample, maxItems = 4)
        val forecastBlock = buildForecastContext(forecastResults, maxResults = 3)
        val howToBlock = buildAppHowToContext()

        return """$userHeader

---
DATA CONTEXT (REAL ROOM DATABASE SNAPSHOT):

$ordersBlock

$spendBlock

$catalogBlock

$forecastBlock

$howToBlock""".trimIndent()
    }
}
