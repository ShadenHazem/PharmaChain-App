package com.pharmachain.ai.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlatformAnalyticsDto(
    @Json(name = "totalGmv") val totalGmv: Double,
    @Json(name = "activeOrdersCount") val activeOrdersCount: Int,
    @Json(name = "completedCount") val completedCount: Int,
    @Json(name = "criticalDisputedCount") val criticalDisputedCount: Int,
    @Json(name = "statusDistribution") val statusDistribution: Map<String, Int>
)

@JsonClass(generateAdapter = true)
data class AdminOrderDto(
    @Json(name = "id") val id: String,
    @Json(name = "orderNumber") val orderNumber: String,
    @Json(name = "buyerPharmacyName") val buyerPharmacyName: String,
    @Json(name = "sellerDistributorName") val sellerDistributorName: String,
    @Json(name = "totalAmount") val totalAmount: Double,
    @Json(name = "status") val status: String,
    @Json(name = "createdAt") val createdAt: Long,
    @Json(name = "itemCount") val itemCount: Int
)

@JsonClass(generateAdapter = true)
data class AdminOrderItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "unitPrice") val unitPrice: Double,
    @Json(name = "subtotal") val subtotal: Double
)

@JsonClass(generateAdapter = true)
data class AdminAuditTimelineEventDto(
    @Json(name = "id") val id: String,
    @Json(name = "timestamp") val timestamp: Long,
    @Json(name = "action") val action: String,
    @Json(name = "actor") val actor: String,
    @Json(name = "note") val note: String
)

@JsonClass(generateAdapter = true)
data class AdminOrderDetailDto(
    @Json(name = "order") val order: AdminOrderDto,
    @Json(name = "items") val items: List<AdminOrderItemDto>,
    @Json(name = "buyerPharmacyPhone") val buyerPharmacyPhone: String,
    @Json(name = "buyerPharmacyAddress") val buyerPharmacyAddress: String,
    @Json(name = "buyerPharmacyLicense") val buyerPharmacyLicense: String,
    @Json(name = "sellerDistributorContact") val sellerDistributorContact: String,
    @Json(name = "sellerDistributorPhone") val sellerDistributorPhone: String,
    @Json(name = "disputeNotes") val disputeNotes: String? = null,
    @Json(name = "disputeReason") val disputeReason: String? = null,
    @Json(name = "disputeRaisedAt") val disputeRaisedAt: Long? = null,
    @Json(name = "auditTimeline") val auditTimeline: List<AdminAuditTimelineEventDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class OverrideOrderStatusRequestDto(
    @Json(name = "newStatus") val newStatus: String,
    @Json(name = "adminNote") val adminNote: String,
    @Json(name = "rollbackStock") val rollbackStock: Boolean
)
