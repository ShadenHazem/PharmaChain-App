package com.pharmachain.ai.model

enum class AdminOrderStatus {
    PENDING,
    ACCEPTED,
    SHIPPED,
    DELIVERED,
    DISPUTED,
    CANCELLED;

    companion object {
        fun fromString(value: String): AdminOrderStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

data class AdminOrder(
    val id: String,
    val orderNumber: String,
    val buyerPharmacyName: String,
    val sellerDistributorName: String,
    val totalAmount: Double,
    val status: AdminOrderStatus,
    val createdAt: Long,
    val itemCount: Int
)

data class AdminOrderItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)

data class AdminAuditTimelineEvent(
    val id: String,
    val timestamp: Long,
    val action: String,
    val actor: String,
    val note: String
)

data class AdminOrderDetail(
    val order: AdminOrder,
    val items: List<AdminOrderItem>,
    val buyerPharmacyPhone: String,
    val buyerPharmacyAddress: String,
    val buyerPharmacyLicense: String,
    val sellerDistributorContact: String,
    val sellerDistributorPhone: String,
    val disputeNotes: String? = null,
    val disputeReason: String? = null,
    val disputeRaisedAt: Long? = null,
    val auditTimeline: List<AdminAuditTimelineEvent> = emptyList()
)

data class PlatformAnalytics(
    val totalGmv: Double,
    val activeOrdersCount: Int,
    val completedCount: Int,
    val criticalDisputedCount: Int,
    val statusDistribution: Map<String, Int>
)
