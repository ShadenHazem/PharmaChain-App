package com.pharmachain.ai.data

import com.pharmachain.ai.core.network.NetworkClient
import com.pharmachain.ai.core.network.dto.OverrideOrderStatusRequestDto
import com.pharmachain.ai.core.result.Result
import com.pharmachain.ai.model.AdminAuditTimelineEvent
import com.pharmachain.ai.model.AdminOrder
import com.pharmachain.ai.model.AdminOrderDetail
import com.pharmachain.ai.model.AdminOrderItem
import com.pharmachain.ai.model.AdminOrderStatus
import com.pharmachain.ai.model.PlatformAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.UUID

interface AdminRepository {
    fun getPlatformAnalytics(): Flow<Result<PlatformAnalytics>>
    fun getAdminOrders(statusFilter: String? = null, searchQuery: String? = null): Flow<Result<List<AdminOrder>>>
    fun getAdminOrderDetail(orderId: String): Flow<Result<AdminOrderDetail>>
    suspend fun overrideOrderStatus(
        orderId: String,
        newStatus: String,
        adminNote: String,
        rollbackStock: Boolean
    ): Result<AdminOrderDetail>
}

class AdminRepositoryImpl(
    private val networkClient: NetworkClient
) : AdminRepository {

    // Thread-safe in-memory cache for live state during the app run
    private val ordersLock = Any()
    private val ordersList = mutableListOf<AdminOrderDetail>()

    init {
        seedInitialAdminData()
    }

    private fun seedInitialAdminData() {
        val now = System.currentTimeMillis()
        val oneHour = 3600000L
        val oneDay = 86400000L

        ordersList.addAll(
            listOf(
                AdminOrderDetail(
                    order = AdminOrder(
                        id = "ORD-2026-9041",
                        orderNumber = "#EGY-9041",
                        buyerPharmacyName = "El-Ezaby Pharmacy (Nasr City)",
                        sellerDistributorName = "Ibnsina Pharma",
                        totalAmount = 14250.0,
                        status = AdminOrderStatus.DISPUTED,
                        createdAt = now - (oneDay * 2 + oneHour * 3),
                        itemCount = 3
                    ),
                    items = listOf(
                        AdminOrderItem("item_1", "Augmentin 1g Tablets (14s)", 50, 145.0, 7250.0),
                        AdminOrderItem("item_2", "Panadol Extra Tablets (24s)", 100, 45.0, 4500.0),
                        AdminOrderItem("item_3", "Cataflam 50mg Tablets (20s)", 50, 50.0, 2500.0)
                    ),
                    buyerPharmacyPhone = "+20 100 123 4567",
                    buyerPharmacyAddress = "24 Abbas El Akkad St, Nasr City, Cairo",
                    buyerPharmacyLicense = "EGY-PHARM-2024-8841",
                    sellerDistributorContact = "Eng. Tamer Hegazy (Key Account Mgr)",
                    sellerDistributorPhone = "+20 2 2414 0000",
                    disputeReason = "Cold-chain violation & box tamper seal damage",
                    disputeNotes = "Buyer reported secondary carton seal compromised upon delivery driver arrival. 12 boxes showed moisture exposure.",
                    disputeRaisedAt = now - (oneDay + oneHour * 5),
                    auditTimeline = listOf(
                        AdminAuditTimelineEvent(
                            id = "aud_1",
                            timestamp = now - (oneDay * 2 + oneHour * 3),
                            action = "ORDER_CREATED",
                            actor = "Buyer: El-Ezaby Pharmacy",
                            note = "Smart batch order submitted via PharmaChain B2B portal."
                        ),
                        AdminAuditTimelineEvent(
                            id = "aud_2",
                            timestamp = now - (oneDay * 2 + oneHour * 1),
                            action = "ORDER_ACCEPTED",
                            actor = "Seller: Ibnsina Pharma",
                            note = "Inventory allocated at 10th of Ramadan Distribution Center."
                        ),
                        AdminAuditTimelineEvent(
                            id = "aud_3",
                            timestamp = now - (oneDay * 1 + oneHour * 6),
                            action = "DISPATCHED",
                            actor = "Logistics Carrier (Fleet #14B)",
                            note = "Consignment loaded onto delivery van #402."
                        ),
                        AdminAuditTimelineEvent(
                            id = "aud_4",
                            timestamp = now - (oneDay + oneHour * 5),
                            action = "DISPUTE_OPENED",
                            actor = "Buyer: Dr. Ahmed El-Sayed (Pharmacist)",
                            note = "Flagged cold-chain sensor alert and crushed cartons."
                        )
                    )
                ),
                AdminOrderDetail(
                    order = AdminOrder(
                        id = "ORD-2026-9042",
                        orderNumber = "#EGY-9042",
                        buyerPharmacyName = "19011 Pharmacy (Maadi Branch)",
                        sellerDistributorName = "United Co. Pharmacists (UCP)",
                        totalAmount = 28900.0,
                        status = AdminOrderStatus.PENDING,
                        createdAt = now - (oneHour * 2),
                        itemCount = 4
                    ),
                    items = listOf(
                        AdminOrderItem("item_4", "Concor 5mg Tablets (30s)", 80, 85.0, 6800.0),
                        AdminOrderItem("item_5", "Lipitor 20mg Tablets (28s)", 60, 210.0, 12600.0),
                        AdminOrderItem("item_6", "Glucophage 1000mg (30s)", 70, 70.0, 4900.0),
                        AdminOrderItem("item_7", "Nexium 40mg Pellets (28s)", 20, 230.0, 4600.0)
                    ),
                    buyerPharmacyPhone = "+20 111 901 1000",
                    buyerPharmacyAddress = "Road 9, Maadi, Cairo",
                    buyerPharmacyLicense = "EGY-PHARM-2023-1901",
                    sellerDistributorContact = "Mr. Karim Fahmy (B2B Lead)",
                    sellerDistributorPhone = "+20 2 2773 0000",
                    disputeNotes = null,
                    disputeReason = null,
                    disputeRaisedAt = null,
                    auditTimeline = listOf(
                        AdminAuditTimelineEvent(
                            id = "aud_5",
                            timestamp = now - (oneHour * 2),
                            action = "ORDER_CREATED",
                            actor = "Buyer: 19011 Pharmacy",
                            note = "Awaiting distributor warehouse review."
                        )
                    )
                ),
                AdminOrderDetail(
                    order = AdminOrder(
                        id = "ORD-2026-9043",
                        orderNumber = "#EGY-9043",
                        buyerPharmacyName = "Seif Pharmacy (Dokki)",
                        sellerDistributorName = "Ramco Pharm Distribution",
                        totalAmount = 8400.0,
                        status = AdminOrderStatus.ACCEPTED,
                        createdAt = now - (oneDay + oneHour * 2),
                        itemCount = 2
                    ),
                    items = listOf(
                        AdminOrderItem("item_8", "Amoclan 1g Tablets (14s)", 60, 110.0, 6600.0),
                        AdminOrderItem("item_9", "Antinal 200mg Capsules (24s)", 60, 30.0, 1800.0)
                    ),
                    buyerPharmacyPhone = "+20 122 345 6789",
                    buyerPharmacyAddress = "Mossadak St, Dokki, Giza",
                    buyerPharmacyLicense = "EGY-PHARM-2022-7721",
                    sellerDistributorContact = "Dr. Mona Rashed (Order Desk)",
                    sellerDistributorPhone = "+20 2 3345 0000",
                    disputeNotes = null,
                    disputeReason = null,
                    disputeRaisedAt = null,
                    auditTimeline = listOf(
                        AdminAuditTimelineEvent(
                            id = "aud_6",
                            timestamp = now - (oneDay + oneHour * 2),
                            action = "ORDER_CREATED",
                            actor = "Buyer: Seif Pharmacy",
                            note = "Routine weekly replenishment."
                        ),
                        AdminAuditTimelineEvent(
                            id = "aud_7",
                            timestamp = now - (oneDay),
                            action = "ORDER_ACCEPTED",
                            actor = "Seller: Ramco Pharm",
                            note = "Picking order generated."
                        )
                    )
                ),
                AdminOrderDetail(
                    order = AdminOrder(
                        id = "ORD-2026-9044",
                        orderNumber = "#EGY-9044",
                        buyerPharmacyName = "Kareem Pharmacy (Heliopolis)",
                        sellerDistributorName = "Soficopharm Trade",
                        totalAmount = 37600.0,
                        status = AdminOrderStatus.SHIPPED,
                        createdAt = now - (oneDay * 3),
                        itemCount = 5
                    ),
                    items = listOf(
                        AdminOrderItem("item_10", "Clexane 4000 IU Syringes (2s)", 40, 320.0, 12800.0),
                        AdminOrderItem("item_11", "Brufen 600mg Tablets (30s)", 100, 58.0, 5800.0),
                        AdminOrderItem("item_12", "Controloc 40mg Tablets (14s)", 50, 120.0, 6000.0),
                        AdminOrderItem("item_13", "Telfast 180mg Tablets (20s)", 60, 110.0, 6600.0),
                        AdminOrderItem("item_14", "Zithromax 500mg (3s)", 80, 80.0, 6400.0)
                    ),
                    buyerPharmacyPhone = "+20 100 889 9001",
                    buyerPharmacyAddress = "Al Ahram St, Heliopolis, Cairo",
                    buyerPharmacyLicense = "EGY-PHARM-2021-3320",
                    sellerDistributorContact = "Eng. Hossam Sabry (Dispatch)",
                    sellerDistributorPhone = "+20 2 2590 0000",
                    disputeNotes = null,
                    disputeReason = null,
                    disputeRaisedAt = null,
                    auditTimeline = listOf(
                        AdminAuditTimelineEvent(
                            id = "aud_8",
                            timestamp = now - (oneDay * 3),
                            action = "ORDER_CREATED",
                            actor = "Buyer: Kareem Pharmacy",
                            note = "High-volume hospital corridor request."
                        ),
                        AdminAuditTimelineEvent(
                            id = "aud_9",
                            timestamp = now - (oneDay * 2),
                            action = "ORDER_ACCEPTED",
                            actor = "Seller: Soficopharm",
                            note = "Payment escrow locked."
                        ),
                        AdminAuditTimelineEvent(
                            id = "aud_10",
                            timestamp = now - (oneDay),
                            action = "SHIPPED",
                            actor = "Soficopharm Logistics",
                            note = "Transit tracking #EGY-EXP-9044 active."
                        )
                    )
                ),
                AdminOrderDetail(
                    order = AdminOrder(
                        id = "ORD-2026-9045",
                        orderNumber = "#EGY-9045",
                        buyerPharmacyName = "Al-Razi Community Pharmacy",
                        sellerDistributorName = "Ibnsina Pharma",
                        totalAmount = 19400.0,
                        status = AdminOrderStatus.DELIVERED,
                        createdAt = now - (oneDay * 5),
                        itemCount = 3
                    ),
                    items = listOf(
                        AdminOrderItem("item_15", "Ketofan 200mg SR (20s)", 90, 40.0, 3600.0),
                        AdminOrderItem("item_16", "Voltaren 100mg Supp (5s)", 80, 75.0, 6000.0),
                        AdminOrderItem("item_17", "Cipralex 10mg Tablets (28s)", 40, 245.0, 9800.0)
                    ),
                    buyerPharmacyPhone = "+20 109 443 2211",
                    buyerPharmacyAddress = "Talaat Harb St, Downtown Cairo",
                    buyerPharmacyLicense = "EGY-PHARM-2020-5512",
                    sellerDistributorContact = "Ibnsina Cairo Hub",
                    sellerDistributorPhone = "+20 2 2414 0000",
                    disputeNotes = null,
                    disputeReason = null,
                    disputeRaisedAt = null,
                    auditTimeline = listOf(
                        AdminAuditTimelineEvent(
                            id = "aud_11",
                            timestamp = now - (oneDay * 5),
                            action = "ORDER_CREATED",
                            actor = "Buyer: Al-Razi Pharmacy",
                            note = "Direct order placed."
                        ),
                        AdminAuditTimelineEvent(
                            id = "aud_12",
                            timestamp = now - (oneDay * 4),
                            action = "DELIVERED",
                            actor = "Driver: Mahmoud Adel",
                            note = "Signed by pharmacist on duty with digital stamp."
                        )
                    )
                ),
                AdminOrderDetail(
                    order = AdminOrder(
                        id = "ORD-2026-9046",
                        orderNumber = "#EGY-9046",
                        buyerPharmacyName = "Misr Pharmacy (Zamalek)",
                        sellerDistributorName = "United Co. Pharmacists (UCP)",
                        totalAmount = 6200.0,
                        status = AdminOrderStatus.CANCELLED,
                        createdAt = now - (oneDay * 6),
                        itemCount = 1
                    ),
                    items = listOf(
                        AdminOrderItem("item_18", "Claritin 10mg Tablets (20s)", 100, 62.0, 6200.0)
                    ),
                    buyerPharmacyPhone = "+20 114 990 0022",
                    buyerPharmacyAddress = "26th of July St, Zamalek, Cairo",
                    buyerPharmacyLicense = "EGY-PHARM-2019-1100",
                    sellerDistributorContact = "UCP Zamalek Liaison",
                    sellerDistributorPhone = "+20 2 2773 0000",
                    disputeNotes = null,
                    disputeReason = null,
                    disputeRaisedAt = null,
                    auditTimeline = listOf(
                        AdminAuditTimelineEvent(
                            id = "aud_13",
                            timestamp = now - (oneDay * 6),
                            action = "ORDER_CREATED",
                            actor = "Buyer: Misr Pharmacy",
                            note = "Submitted order."
                        ),
                        AdminAuditTimelineEvent(
                            id = "aud_14",
                            timestamp = now - (oneDay * 6 + 1800000L),
                            action = "ORDER_CANCELLED",
                            actor = "Buyer: Misr Pharmacy",
                            note = "Cancelled within 30-minute grace window."
                        )
                    )
                )
            )
        )
    }

    override fun getPlatformAnalytics(): Flow<Result<PlatformAnalytics>> = flow {
        emit(Result.Loading)
        try {
            // Check API if configured, otherwise synthesize from live database
            val response = try {
                networkClient.adminApi.getPlatformAnalytics()
            } catch (e: Exception) {
                null
            }

            if (response != null && response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                emit(
                    Result.Success(
                        PlatformAnalytics(
                            totalGmv = dto.totalGmv,
                            activeOrdersCount = dto.activeOrdersCount,
                            completedCount = dto.completedCount,
                            criticalDisputedCount = dto.criticalDisputedCount,
                            statusDistribution = dto.statusDistribution
                        )
                    )
                )
            } else {
                // Compute from local orders database
                val analytics = synchronized(ordersLock) {
                    val totalGmv = ordersList.sumOf { it.order.totalAmount }
                    val activeCount = ordersList.count {
                        it.order.status in listOf(
                            AdminOrderStatus.PENDING,
                            AdminOrderStatus.ACCEPTED,
                            AdminOrderStatus.SHIPPED
                        )
                    }
                    val completedCount = ordersList.count { it.order.status == AdminOrderStatus.DELIVERED }
                    val disputedCount = ordersList.count { it.order.status == AdminOrderStatus.DISPUTED }
                    val distribution = mutableMapOf<String, Int>()
                    AdminOrderStatus.entries.forEach { st ->
                        distribution[st.name] = ordersList.count { it.order.status == st }
                    }

                    PlatformAnalytics(
                        totalGmv = totalGmv,
                        activeOrdersCount = activeCount,
                        completedCount = completedCount,
                        criticalDisputedCount = disputedCount,
                        statusDistribution = distribution
                    )
                }
                emit(Result.Success(analytics))
            }
        } catch (t: Throwable) {
            emit(Result.Error(t.message ?: "Failed to calculate platform analytics", t))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAdminOrders(
        statusFilter: String?,
        searchQuery: String?
    ): Flow<Result<List<AdminOrder>>> = flow {
        emit(Result.Loading)
        try {
            val response = try {
                networkClient.adminApi.getAdminOrders(status = statusFilter, query = searchQuery)
            } catch (e: Exception) {
                null
            }

            if (response != null && response.isSuccessful && response.body() != null) {
                val dtoList = response.body()!!
                val list = dtoList.map { dto ->
                    AdminOrder(
                        id = dto.id,
                        orderNumber = dto.orderNumber,
                        buyerPharmacyName = dto.buyerPharmacyName,
                        sellerDistributorName = dto.sellerDistributorName,
                        totalAmount = dto.totalAmount,
                        status = AdminOrderStatus.fromString(dto.status),
                        createdAt = dto.createdAt,
                        itemCount = dto.itemCount
                    )
                }
                emit(Result.Success(list))
            } else {
                val filtered = synchronized(ordersLock) {
                    var list = ordersList.map { it.order }

                    if (!statusFilter.isNullOrBlank() && !statusFilter.equals("ALL", ignoreCase = true)) {
                        list = list.filter { it.status.name.equals(statusFilter, ignoreCase = true) }
                    }

                    if (!searchQuery.isNullOrBlank()) {
                        val q = searchQuery.trim().lowercase()
                        list = list.filter {
                            it.orderNumber.lowercase().contains(q) ||
                                it.buyerPharmacyName.lowercase().contains(q) ||
                                it.sellerDistributorName.lowercase().contains(q)
                        }
                    }

                    list.sortedByDescending { it.createdAt }
                }
                emit(Result.Success(filtered))
            }
        } catch (t: Throwable) {
            emit(Result.Error(t.message ?: "Failed to load admin orders", t))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAdminOrderDetail(orderId: String): Flow<Result<AdminOrderDetail>> = flow {
        emit(Result.Loading)
        try {
            val response = try {
                networkClient.adminApi.getAdminOrderDetail(orderId)
            } catch (e: Exception) {
                null
            }

            if (response != null && response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                emit(
                    Result.Success(
                        AdminOrderDetail(
                            order = AdminOrder(
                                id = dto.order.id,
                                orderNumber = dto.order.orderNumber,
                                buyerPharmacyName = dto.order.buyerPharmacyName,
                                sellerDistributorName = dto.order.sellerDistributorName,
                                totalAmount = dto.order.totalAmount,
                                status = AdminOrderStatus.fromString(dto.order.status),
                                createdAt = dto.order.createdAt,
                                itemCount = dto.order.itemCount
                            ),
                            items = dto.items.map {
                                AdminOrderItem(it.id, it.name, it.quantity, it.unitPrice, it.subtotal)
                            },
                            buyerPharmacyPhone = dto.buyerPharmacyPhone,
                            buyerPharmacyAddress = dto.buyerPharmacyAddress,
                            buyerPharmacyLicense = dto.buyerPharmacyLicense,
                            sellerDistributorContact = dto.sellerDistributorContact,
                            sellerDistributorPhone = dto.sellerDistributorPhone,
                            disputeNotes = dto.disputeNotes,
                            disputeReason = dto.disputeReason,
                            disputeRaisedAt = dto.disputeRaisedAt,
                            auditTimeline = dto.auditTimeline.map {
                                AdminAuditTimelineEvent(it.id, it.timestamp, it.action, it.actor, it.note)
                            }
                        )
                    )
                )
            } else {
                val detail = synchronized(ordersLock) {
                    ordersList.firstOrNull { it.order.id == orderId }
                }
                if (detail != null) {
                    emit(Result.Success(detail))
                } else {
                    emit(Result.Error("Order not found: $orderId"))
                }
            }
        } catch (t: Throwable) {
            emit(Result.Error(t.message ?: "Failed to retrieve order detail", t))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun overrideOrderStatus(
        orderId: String,
        newStatus: String,
        adminNote: String,
        rollbackStock: Boolean
    ): Result<AdminOrderDetail> = withContext(Dispatchers.IO) {
        try {
            // Attempt remote API post
            try {
                networkClient.adminApi.overrideOrderStatus(
                    id = orderId,
                    request = OverrideOrderStatusRequestDto(
                        newStatus = newStatus,
                        adminNote = adminNote,
                        rollbackStock = rollbackStock
                    )
                )
            } catch (ignored: Exception) {
                // Network unavailable or simulated backend - proceed with local state update
            }

            synchronized(ordersLock) {
                val index = ordersList.indexOfFirst { it.order.id == orderId }
                if (index < 0) {
                    return@withContext Result.Error("Order not found: $orderId")
                }

                val current = ordersList[index]
                val targetStatus = AdminOrderStatus.fromString(newStatus)
                val rollbackNotice = if (rollbackStock) " [Inventory returned to distributor allocation]" else ""

                val newAudit = AdminAuditTimelineEvent(
                    id = "aud_${UUID.randomUUID().toString().take(8)}",
                    timestamp = System.currentTimeMillis(),
                    action = "ADMIN_OVERRIDE_${targetStatus.name}",
                    actor = "Platform Admin (Authority)",
                    note = "$adminNote$rollbackNotice"
                )

                val updatedOrder = current.order.copy(status = targetStatus)
                val updatedDetail = current.copy(
                    order = updatedOrder,
                    auditTimeline = current.auditTimeline + newAudit,
                    disputeNotes = if (targetStatus != AdminOrderStatus.DISPUTED) {
                        "Resolved by Admin: $adminNote"
                    } else current.disputeNotes
                )

                ordersList[index] = updatedDetail
                Result.Success(updatedDetail)
            }
        } catch (t: Throwable) {
            Result.Error(t.message ?: "Failed to override order status", t)
        }
    }
}
