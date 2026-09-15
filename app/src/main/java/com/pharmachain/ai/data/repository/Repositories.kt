package com.pharmachain.ai.data.repository

import com.pharmachain.ai.core.common.result.NetworkResult
import com.pharmachain.ai.core.common.session.SessionManager
import com.pharmachain.ai.core.database.PharmaChainDatabase
import com.pharmachain.ai.core.database.entity.ApiIntegrationEntity
import com.pharmachain.ai.core.database.entity.ColumnMappingEntity
import com.pharmachain.ai.core.database.entity.CommissionRecordEntity
import com.pharmachain.ai.core.database.entity.DistributorAnalyticsSnapshotEntity
import com.pharmachain.ai.core.database.entity.DistributorProfileEntity
import com.pharmachain.ai.core.database.entity.ForecastJobEntity
import com.pharmachain.ai.core.database.entity.ForecastResultEntity
import com.pharmachain.ai.core.database.entity.InventoryColumnMappingEntity
import com.pharmachain.ai.core.database.entity.InventoryUploadEntity
import com.pharmachain.ai.core.database.entity.ListingSourceEntity
import com.pharmachain.ai.core.database.entity.LowStockAlertEntity
import com.pharmachain.ai.core.database.entity.MedicationEntity
import com.pharmachain.ai.core.database.entity.OrderEntity
import com.pharmachain.ai.core.database.entity.OrderItemEntity
import com.pharmachain.ai.core.database.entity.PharmacyProfileEntity
import com.pharmachain.ai.core.database.entity.ProductListingEntity
import com.pharmachain.ai.core.database.entity.SalesHistoryUploadEntity
import com.pharmachain.ai.core.database.entity.SmartCartItemEntity
import com.pharmachain.ai.core.database.entity.UserEntity
import com.pharmachain.ai.core.model.ApiAuthType
import com.pharmachain.ai.core.model.ApiIntegration
import com.pharmachain.ai.core.model.CartItem
import com.pharmachain.ai.core.model.ApiSyncFrequency
import com.pharmachain.ai.core.model.ApiSyncStatus
import com.pharmachain.ai.core.model.CommissionRecord
import com.pharmachain.ai.core.model.CommissionStatus
import com.pharmachain.ai.core.model.ConflictResolutionAction
import com.pharmachain.ai.core.model.DistributorAnalyticsSnapshot
import com.pharmachain.ai.core.model.DistributorProfile
import com.pharmachain.ai.core.model.ForecastDuration
import com.pharmachain.ai.core.model.ForecastJob
import com.pharmachain.ai.core.model.ForecastResult
import com.pharmachain.ai.core.model.InventoryColumnMapping
import com.pharmachain.ai.core.model.InventoryMappingField
import com.pharmachain.ai.core.model.InventoryUpload
import com.pharmachain.ai.core.model.InventoryUploadStatus
import com.pharmachain.ai.core.model.JobStatus
import com.pharmachain.ai.core.model.ListingSource
import com.pharmachain.ai.core.model.ListingSourceType
import com.pharmachain.ai.core.model.LowStockAlert
import com.pharmachain.ai.core.model.MappingField
import com.pharmachain.ai.core.model.Medication
import com.pharmachain.ai.core.model.MedicationWithListings
import com.pharmachain.ai.core.model.Order
import com.pharmachain.ai.core.model.OrderItem
import com.pharmachain.ai.core.model.OrderStatus
import com.pharmachain.ai.core.model.PharmacyProfile
import com.pharmachain.ai.core.model.ProductListing
import com.pharmachain.ai.core.model.Role
import com.pharmachain.ai.core.model.SalesHistoryUpload
import com.pharmachain.ai.core.model.SmartCartItem
import com.pharmachain.ai.core.model.SyncConflictItem
import com.pharmachain.ai.core.model.UploadStatus
import com.pharmachain.ai.core.model.User
import com.pharmachain.ai.core.network.NetworkClient
import com.pharmachain.ai.core.network.dto.CreateOrderRequestDto
import com.pharmachain.ai.core.network.dto.DailyRevenuePointDto
import com.pharmachain.ai.core.network.dto.DistributorAnalyticsDto
import com.pharmachain.ai.core.network.dto.InventoryValidationResponseDto
import com.pharmachain.ai.core.network.dto.InventoryValidationRowDto
import com.pharmachain.ai.core.network.dto.OrderItemCreateDto
import com.pharmachain.ai.core.network.dto.RegisterRequestDto
import com.pharmachain.ai.core.network.dto.TopSellingMedicationDto
import com.pharmachain.ai.feature.catalog.ProductSortEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class AuthRepository(
    private val database: PharmaChainDatabase,
    private val networkClient: NetworkClient,
    private val sessionManager: SessionManager
) {
    suspend fun login(email: String, role: Role): NetworkResult<User> = withContext(Dispatchers.IO) {
        try {
            // Check if user already in local DB or generate session
            val userId = "usr_${email.replace("@", "_").replace(".", "_")}"
            val existingUser = User(
                id = userId,
                role = role,
                fullName = when (role) {
                    Role.PHARMACIST -> "Dr. Ahmed El-Sayed"
                    Role.ADMIN -> "Eng. Sherif Mostafa (Platform Overseer)"
                    Role.DISTRIBUTOR -> "Al-Andalous Medical Dist."
                },
                email = email,
                phone = "+20 100 123 4567",
                createdAt = System.currentTimeMillis()
            )
            database.userDao().insertUser(
                UserEntity(
                    id = existingUser.id,
                    role = existingUser.role,
                    fullName = existingUser.fullName,
                    email = existingUser.email,
                    phone = existingUser.phone,
                    createdAt = existingUser.createdAt
                )
            )

            if (role == Role.PHARMACIST) {
                database.pharmacyDao().insertProfile(
                    PharmacyProfileEntity(
                        id = "pharm_$userId",
                        userId = userId,
                        pharmacyName = "El-Ezaby Pharmacy Branch 14",
                        licenseNumber = "EG-PH-2024-9981",
                        governorate = "Cairo",
                        city = "Nasr City",
                        addressLine = "24 Abbas El Akkad St",
                        latitude = 30.0561,
                        longitude = 31.3431,
                        isVerified = true
                    )
                )
            } else {
                database.distributorDao().insertProfile(
                    DistributorProfileEntity(
                        id = "dist_$userId",
                        userId = userId,
                        companyName = "Ibnsina Pharma S.A.E.",
                        taxRegistrationId = "EG-TR-491-882",
                        reputationScore = 4.85,
                        totalOrdersFulfilled = 1420,
                        defaultCommissionRatePct = 4.5,
                        isVerified = true
                    )
                )
            }

            sessionManager.saveSession("jwt_mock_token_${System.currentTimeMillis()}", "refresh_token", existingUser)
            NetworkResult.Success(existingUser)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Authentication failed", e)
        }
    }

    suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        role: Role,
        businessName: String,
        regOrLicenseNumber: String,
        governorate: String,
        city: String,
        address: String
    ): NetworkResult<User> = withContext(Dispatchers.IO) {
        try {
            val userId = "usr_${UUID.randomUUID().toString().take(8)}"
            val newUser = User(
                id = userId,
                role = role,
                fullName = fullName,
                email = email,
                phone = phone,
                createdAt = System.currentTimeMillis()
            )
            database.userDao().insertUser(
                UserEntity(
                    id = newUser.id,
                    role = newUser.role,
                    fullName = newUser.fullName,
                    email = newUser.email,
                    phone = newUser.phone,
                    createdAt = newUser.createdAt
                )
            )

            if (role == Role.PHARMACIST) {
                database.pharmacyDao().insertProfile(
                    PharmacyProfileEntity(
                        id = "pharm_$userId",
                        userId = userId,
                        pharmacyName = businessName,
                        licenseNumber = regOrLicenseNumber,
                        governorate = governorate,
                        city = city,
                        addressLine = address,
                        latitude = 30.0444,
                        longitude = 31.2357,
                        isVerified = true
                    )
                )
            } else {
                database.distributorDao().insertProfile(
                    DistributorProfileEntity(
                        id = "dist_$userId",
                        userId = userId,
                        companyName = businessName,
                        taxRegistrationId = regOrLicenseNumber,
                        reputationScore = 5.0,
                        totalOrdersFulfilled = 0,
                        defaultCommissionRatePct = 4.5,
                        isVerified = true
                    )
                )
            }

            sessionManager.saveSession("jwt_mock_token_${System.currentTimeMillis()}", "refresh_token", newUser)
            NetworkResult.Success(newUser)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Registration failed", e)
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }
}

class CatalogRepository(
    private val database: PharmaChainDatabase,
    private val networkClient: NetworkClient
) {
    suspend fun seedInitialCatalogIfEmpty() = withContext(Dispatchers.IO) {
        // Step 1: Seed Parent UserEntity rows required by foreign key constraints
        val distributorUsers = listOf(
            UserEntity("u1", Role.DISTRIBUTOR, "Ibnsina Pharma", "contact@ibnsina.com.eg", "+20 2 2414 0000", System.currentTimeMillis()),
            UserEntity("u2", Role.DISTRIBUTOR, "United Co. Pharmacists", "support@ucp.com.eg", "+20 2 2773 0000", System.currentTimeMillis()),
            UserEntity("u3", Role.DISTRIBUTOR, "Ramco Pharm Distribution", "orders@ramco.com.eg", "+20 2 3345 0000", System.currentTimeMillis()),
            UserEntity("u4", Role.DISTRIBUTOR, "Soficopharm Trade", "info@soficopharm.com.eg", "+20 2 2590 0000", System.currentTimeMillis()),
            UserEntity("u_pharm_demo", Role.PHARMACIST, "Dr. Ahmed El-Sayed", "demo@pharmachain.eg", "+20 100 123 4567", System.currentTimeMillis())
        )
        database.userDao().insertUsers(distributorUsers)

        // Step 2: Seed Pharmacy and Distributor Profiles (referencing existing UserEntity IDs)
        database.pharmacyDao().insertProfile(
            PharmacyProfileEntity(
                id = "pharm_demo",
                userId = "u_pharm_demo",
                pharmacyName = "El-Ezaby Pharmacy",
                licenseNumber = "EG-PH-2024-9981",
                governorate = "Cairo",
                city = "Nasr City",
                addressLine = "24 Abbas El Akkad St",
                latitude = 30.0561,
                longitude = 31.3431,
                isVerified = true
            )
        )

        val distributors = listOf(
            DistributorProfileEntity("dist_ibnsina", "u1", "Ibnsina Pharma", "TR-101", 4.9, 3200, 4.5, true),
            DistributorProfileEntity("dist_ucp", "u2", "United Co. Pharmacists (UCP)", "TR-102", 4.7, 2850, 4.0, true),
            DistributorProfileEntity("dist_ramco", "u3", "Ramco Pharm Distribution", "TR-103", 4.6, 1940, 5.0, true),
            DistributorProfileEntity("dist_soficopharm", "u4", "Soficopharm Trade", "TR-104", 4.8, 2210, 4.5, true)
        )
        database.distributorDao().insertProfiles(distributors)

        val meds = listOf(
            MedicationEntity("med_1", "Paracetamol + Caffeine", "Panadol Extra 500mg", "Tablet", "500mg/65mg", "Analgesics & Antipyretics", null),
            MedicationEntity("med_2", "Amoxicillin + Clavulanate", "Augmentin 1g", "Tablet", "1000mg", "Antibiotics", null),
            MedicationEntity("med_3", "Bisoprolol Fumarate", "Concor 5mg", "Tablet", "5mg", "Cardiovascular", null),
            MedicationEntity("med_4", "Ciprofloxacin", "Ciprofar 500mg", "Tablet", "500mg", "Antibiotics", null),
            MedicationEntity("med_5", "Nifuroxazide", "Antinal 200mg", "Capsule", "200mg", "Gastrointestinal", null),
            MedicationEntity("med_6", "Fexofenadine HCl", "Telfast 180mg", "Tablet", "180mg", "Antihistamines", null),
            MedicationEntity("med_7", "Ketoprofen", "Ketofan 75mg", "Capsule", "75mg", "Analgesics & NSAIDs", null),
            MedicationEntity("med_8", "Omeprazole", "Gastrazole 40mg", "Capsule", "40mg", "Gastrointestinal", null),
            MedicationEntity("med_9", "Metformin HCl", "Cidophage 850mg", "Tablet", "850mg", "Antidiabetic", null),
            MedicationEntity("med_10", "Atorvastatin Calcium", "Ator 20mg", "Tablet", "20mg", "Cardiovascular", null)
        )
        database.medicationDao().insertMedications(meds)

        val listings = listOf(
            // Panadol Extra
            ProductListingEntity("list_1a", "med_1", "dist_ibnsina", "Ibnsina Pharma", 4.9, 45.00, 450, System.currentTimeMillis() + 86400000L * 500, System.currentTimeMillis()),
            ProductListingEntity("list_1b", "med_1", "dist_ucp", "United Co. Pharmacists (UCP)", 4.7, 43.50, 120, System.currentTimeMillis() + 86400000L * 400, System.currentTimeMillis()),
            ProductListingEntity("list_1c", "med_1", "dist_ramco", "Ramco Pharm Distribution", 4.6, 46.00, 600, System.currentTimeMillis() + 86400000L * 600, System.currentTimeMillis()),
            // Augmentin 1g
            ProductListingEntity("list_2a", "med_2", "dist_ibnsina", "Ibnsina Pharma", 4.9, 115.00, 310, System.currentTimeMillis() + 86400000L * 350, System.currentTimeMillis()),
            ProductListingEntity("list_2b", "med_2", "dist_soficopharm", "Soficopharm Trade", 4.8, 110.00, 180, System.currentTimeMillis() + 86400000L * 420, System.currentTimeMillis()),
            // Concor 5mg
            ProductListingEntity("list_3a", "med_3", "dist_ucp", "United Co. Pharmacists (UCP)", 4.7, 62.00, 520, System.currentTimeMillis() + 86400000L * 480, System.currentTimeMillis()),
            ProductListingEntity("list_3b", "med_3", "dist_ramco", "Ramco Pharm Distribution", 4.6, 60.50, 90, System.currentTimeMillis() + 86400000L * 300, System.currentTimeMillis()),
            // Ciprofar 500mg
            ProductListingEntity("list_4a", "med_4", "dist_ibnsina", "Ibnsina Pharma", 4.9, 54.00, 240, System.currentTimeMillis() + 86400000L * 400, System.currentTimeMillis()),
            // Antinal 200mg
            ProductListingEntity("list_5a", "med_5", "dist_soficopharm", "Soficopharm Trade", 4.8, 38.00, 700, System.currentTimeMillis() + 86400000L * 550, System.currentTimeMillis()),
            ProductListingEntity("list_5b", "med_5", "dist_ucp", "United Co. Pharmacists (UCP)", 4.7, 36.50, 350, System.currentTimeMillis() + 86400000L * 450, System.currentTimeMillis()),
            // Telfast 180mg
            ProductListingEntity("list_6a", "med_6", "dist_ramco", "Ramco Pharm Distribution", 4.6, 92.00, 140, System.currentTimeMillis() + 86400000L * 380, System.currentTimeMillis()),
            ProductListingEntity("list_6b", "med_6", "dist_ibnsina", "Ibnsina Pharma", 4.9, 95.00, 400, System.currentTimeMillis() + 86400000L * 520, System.currentTimeMillis()),
            // Ketofan 75mg
            ProductListingEntity("list_7a", "med_7", "dist_ucp", "United Co. Pharmacists (UCP)", 4.7, 28.00, 800, System.currentTimeMillis() + 86400000L * 600, System.currentTimeMillis()),
            // Gastrazole 40mg
            ProductListingEntity("list_8a", "med_8", "dist_soficopharm", "Soficopharm Trade", 4.8, 74.00, 260, System.currentTimeMillis() + 86400000L * 450, System.currentTimeMillis()),
            // Cidophage 850mg
            ProductListingEntity("list_9a", "med_9", "dist_ibnsina", "Ibnsina Pharma", 4.9, 32.50, 950, System.currentTimeMillis() + 86400000L * 700, System.currentTimeMillis()),
            // Ator 20mg
            ProductListingEntity("list_10a", "med_10", "dist_ucp", "United Co. Pharmacists (UCP)", 4.7, 85.00, 320, System.currentTimeMillis() + 86400000L * 500, System.currentTimeMillis())
        )
        database.medicationDao().insertProductListings(listings)
    }

    fun getMedications(query: String = ""): Flow<List<MedicationWithListings>> {
        val flow = if (query.isBlank()) {
            database.medicationDao().getMedicationsWithListings()
        } else {
            database.medicationDao().searchMedicationsWithListings(query)
        }

        return flow.map { relations ->
            relations.map { rel ->
                val med = Medication(
                    id = rel.medication.id,
                    genericName = rel.medication.genericName,
                    brandName = rel.medication.brandName,
                    form = rel.medication.form,
                    strength = rel.medication.strength,
                    category = rel.medication.category,
                    imageUrl = rel.medication.imageUrl
                )
                val domainListings = rel.listings.map { l ->
                    ProductListing(
                        id = l.id,
                        medicationId = l.medicationId,
                        distributorId = l.distributorId,
                        distributorName = l.distributorName,
                        distributorReputation = l.distributorReputation,
                        price = l.price,
                        stockQuantity = l.stockQuantity,
                        expiryDate = l.expiryDate,
                        updatedAt = l.updatedAt
                    )
                }
                // Rank listings using algorithmic ProductSortEngine (Section 8.2)
                val rankedListings = ProductSortEngine.rankListings(domainListings)
                MedicationWithListings(
                    medication = med,
                    listings = rankedListings,
                    topOffer = rankedListings.firstOrNull()
                )
            }
        }.flowOn(Dispatchers.IO)
    }
}

class OrdersRepository(
    private val database: PharmaChainDatabase,
    private val sessionManager: SessionManager
) {
    suspend fun seedInitialOrdersIfEmpty() = withContext(Dispatchers.IO) {
        val existingOrders = listOf(
            OrderEntity(
                id = "ORD-2026-9042",
                pharmacyId = "pharm_demo",
                pharmacyName = "El-Ezaby Pharmacy",
                distributorId = "dist_ibnsina",
                distributorName = "Ibnsina Pharma",
                status = OrderStatus.PENDING_APPROVAL,
                totalAmount = 5820.00,
                placedAt = System.currentTimeMillis() - 1000L * 60 * 18, // 18 mins ago
                updatedAt = System.currentTimeMillis() - 1000L * 60 * 18,
                deliveryAddress = "24 Abbas El Akkad St, Nasr City, Cairo"
            ),
            OrderEntity(
                id = "ORD-2026-9015",
                pharmacyId = "pharm_demo",
                pharmacyName = "Seif Pharmacy - Zamalek",
                distributorId = "dist_ibnsina",
                distributorName = "Ibnsina Pharma",
                status = OrderStatus.PENDING_APPROVAL,
                totalAmount = 3450.00,
                placedAt = System.currentTimeMillis() - 1000L * 60 * 45, // 45 mins ago
                updatedAt = System.currentTimeMillis() - 1000L * 60 * 45,
                deliveryAddress = "12 Brazil St, Zamalek, Cairo"
            ),
            OrderEntity(
                id = "ORD-2026-8801",
                pharmacyId = "pharm_demo",
                pharmacyName = "El-Ezaby Pharmacy",
                distributorId = "dist_ibnsina",
                distributorName = "Ibnsina Pharma",
                status = OrderStatus.SHIPPED,
                totalAmount = 4520.00,
                placedAt = System.currentTimeMillis() - 86400000L * 1,
                updatedAt = System.currentTimeMillis() - 3600000L * 4,
                deliveryAddress = "24 Abbas El Akkad St, Nasr City, Cairo"
            ),
            OrderEntity(
                id = "ORD-2026-8742",
                pharmacyId = "pharm_demo",
                pharmacyName = "El-Ezaby Pharmacy",
                distributorId = "dist_ucp",
                distributorName = "United Co. Pharmacists (UCP)",
                status = OrderStatus.DELIVERED,
                totalAmount = 8940.00,
                placedAt = System.currentTimeMillis() - 86400000L * 5,
                updatedAt = System.currentTimeMillis() - 86400000L * 4,
                deliveryAddress = "24 Abbas El Akkad St, Nasr City, Cairo"
            ),
            OrderEntity(
                id = "ORD-2026-8610",
                pharmacyId = "pharm_demo",
                pharmacyName = "El-Ezaby Pharmacy",
                distributorId = "dist_soficopharm",
                distributorName = "Soficopharm Trade",
                status = OrderStatus.DELIVERED,
                totalAmount = 6200.00,
                placedAt = System.currentTimeMillis() - 86400000L * 12,
                updatedAt = System.currentTimeMillis() - 86400000L * 11,
                deliveryAddress = "24 Abbas El Akkad St, Nasr City, Cairo"
            )
        )
        existingOrders.forEach { database.orderDao().insertOrder(it) }

        val items = listOf(
            OrderItemEntity("item_p1", "ORD-2026-9042", "list_1a", "Panadol Extra 500mg", 60, 45.0, 2700.0),
            OrderItemEntity("item_p2", "ORD-2026-9042", "list_2a", "Augmentin 1g", 25, 115.0, 2875.0),
            OrderItemEntity("item_p3", "ORD-2026-9042", "list_9a", "Cidophage 850mg", 8, 32.5, 245.0),

            OrderItemEntity("item_p4", "ORD-2026-9015", "list_4a", "Amoclan 1g", 30, 95.0, 2850.0),
            OrderItemEntity("item_p5", "ORD-2026-9015", "list_5a", "Antinal 200mg", 15, 40.0, 600.0),

            OrderItemEntity("item_1", "ORD-2026-8801", "list_1a", "Panadol Extra 500mg", 40, 45.0, 1800.0),
            OrderItemEntity("item_2", "ORD-2026-8801", "list_2a", "Augmentin 1g", 20, 115.0, 2300.0),
            OrderItemEntity("item_3", "ORD-2026-8801", "list_9a", "Cidophage 850mg", 13, 32.5, 420.0),

            OrderItemEntity("item_4", "ORD-2026-8742", "list_3a", "Concor 5mg", 80, 62.0, 4960.0),
            OrderItemEntity("item_5", "ORD-2026-8742", "list_7a", "Ketofan 75mg", 100, 28.0, 2800.0),
            OrderItemEntity("item_6", "ORD-2026-8742", "list_10a", "Ator 20mg", 14, 85.0, 1180.0),

            OrderItemEntity("item_7", "ORD-2026-8610", "list_5a", "Antinal 200mg", 100, 38.0, 3800.0),
            OrderItemEntity("item_8", "ORD-2026-8610", "list_8a", "Gastrazole 40mg", 32, 74.0, 2400.0)
        )
        database.orderDao().insertOrderItems(items)

        // Distributor Commission Records (Backend calculation mirror)
        val commissions = listOf(
            CommissionRecordEntity("comm_0a", "ORD-2026-9042", "dist_ibnsina", 5820.00, 4.5, 261.90, CommissionStatus.PENDING, System.currentTimeMillis() - 1000L * 60 * 18),
            CommissionRecordEntity("comm_0b", "ORD-2026-9015", "dist_ibnsina", 3450.00, 4.5, 155.25, CommissionStatus.PENDING, System.currentTimeMillis() - 1000L * 60 * 45),
            CommissionRecordEntity("comm_1", "ORD-2026-8801", "dist_ibnsina", 4520.00, 4.5, 203.40, CommissionStatus.PENDING, System.currentTimeMillis() - 86400000L * 1),
            CommissionRecordEntity("comm_2", "ORD-2026-8742", "dist_ucp", 8940.00, 4.0, 357.60, CommissionStatus.INVOICED, System.currentTimeMillis() - 86400000L * 5),
            CommissionRecordEntity("comm_3", "ORD-2026-8610", "dist_soficopharm", 6200.00, 4.5, 279.00, CommissionStatus.PAID, System.currentTimeMillis() - 86400000L * 12)
        )
        commissions.forEach { database.commissionDao().insertCommissionRecord(it) }
    }

    fun getOrdersForPharmacy(pharmacyId: String = "pharm_demo"): Flow<List<Order>> {
        return database.orderDao().getOrdersForPharmacy(pharmacyId).map { relations ->
            relations.map { r ->
                Order(
                    id = r.order.id,
                    pharmacyId = r.order.pharmacyId,
                    pharmacyName = r.order.pharmacyName,
                    distributorId = r.order.distributorId,
                    distributorName = r.order.distributorName,
                    status = r.order.status,
                    totalAmount = r.order.totalAmount,
                    placedAt = r.order.placedAt,
                    updatedAt = r.order.updatedAt,
                    deliveryAddress = r.order.deliveryAddress,
                    items = r.items.map { i ->
                        OrderItem(
                            id = i.id,
                            orderId = i.orderId,
                            productListingId = i.productListingId,
                            medicationName = i.medicationName,
                            quantity = i.quantity,
                            unitPriceAtOrder = i.unitPriceAtOrder,
                            lineTotal = i.lineTotal
                        )
                    }
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getOrderById(orderId: String): Flow<Order?> {
        return database.orderDao().getOrderById(orderId).map { r ->
            r?.let {
                Order(
                    id = it.order.id,
                    pharmacyId = it.order.pharmacyId,
                    pharmacyName = it.order.pharmacyName,
                    distributorId = it.order.distributorId,
                    distributorName = it.order.distributorName,
                    status = it.order.status,
                    totalAmount = it.order.totalAmount,
                    placedAt = it.order.placedAt,
                    updatedAt = it.order.updatedAt,
                    deliveryAddress = it.order.deliveryAddress,
                    items = it.items.map { i ->
                        OrderItem(
                            id = i.id,
                            orderId = i.orderId,
                            productListingId = i.productListingId,
                            medicationName = i.medicationName,
                            quantity = i.quantity,
                            unitPriceAtOrder = i.unitPriceAtOrder,
                            lineTotal = i.lineTotal
                        )
                    }
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun createOrdersFromSmartCart(
        items: List<SmartCartItem>,
        pharmacyId: String = "pharm_demo",
        pharmacyName: String = "El-Ezaby Pharmacy",
        deliveryAddress: String = "24 Abbas El Akkad St, Nasr City, Cairo"
    ): List<String> = withContext(Dispatchers.IO) {
        // Group by distributor because Order is single-distributor per Section 8.4 schema
        val groupedByDistributor = items.filter { it.isSelected }.groupBy { it.distributorId }
        val createdOrderIds = mutableListOf<String>()

        for ((distributorId, distItems) in groupedByDistributor) {
            val orderId = "ORD-${System.currentTimeMillis().toString().takeLast(6)}"
            val totalAmount = distItems.sumOf { it.lineTotal }
            val distName = distItems.first().distributorName

            val orderEntity = OrderEntity(
                id = orderId,
                pharmacyId = pharmacyId,
                pharmacyName = pharmacyName,
                distributorId = distributorId,
                distributorName = distName,
                status = OrderStatus.PENDING_APPROVAL,
                totalAmount = totalAmount,
                placedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                deliveryAddress = deliveryAddress
            )
            database.orderDao().insertOrder(orderEntity)

            val orderItems = distItems.map { item ->
                OrderItemEntity(
                    id = "item_${UUID.randomUUID().toString().take(8)}",
                    orderId = orderId,
                    productListingId = item.selectedProductListingId,
                    medicationName = item.medicationName,
                    quantity = item.quantity,
                    unitPriceAtOrder = item.unitPrice,
                    lineTotal = item.lineTotal
                )
            }
            database.orderDao().insertOrderItems(orderItems)

            // Calculate commission record (tracked server-side, never surfaced to pharmacist)
            val commissionEntity = CommissionRecordEntity(
                id = "comm_${UUID.randomUUID().toString().take(8)}",
                orderId = orderId,
                distributorId = distributorId,
                orderTotal = totalAmount,
                commissionRatePct = 4.5,
                commissionAmount = totalAmount * 0.045,
                status = CommissionStatus.PENDING,
                computedAt = System.currentTimeMillis()
            )
            database.commissionDao().insertCommissionRecord(commissionEntity)

            createdOrderIds.add(orderId)
        }
        createdOrderIds
    }

    suspend fun submitBatchOrder(
        items: List<CartItem>,
        pharmacyId: String = "pharm_demo",
        pharmacyName: String = "El-Ezaby Pharmacy",
        deliveryAddress: String = "24 Abbas El Akkad St, Nasr City, Cairo"
    ): NetworkResult<List<String>> = withContext(Dispatchers.IO) {
        try {
            if (items.isEmpty()) {
                return@withContext NetworkResult.Error("Cannot submit an empty cart")
            }
            val groupedByDistributor = items.groupBy { it.distributorId }
            val createdOrderIds = mutableListOf<String>()

            for ((distributorId, distItems) in groupedByDistributor) {
                val orderId = "ORD-${System.currentTimeMillis().toString().takeLast(6)}"
                val totalAmount = distItems.sumOf { it.lineTotal }
                val distName = distItems.first().distributorName

                val orderEntity = OrderEntity(
                    id = orderId,
                    pharmacyId = pharmacyId,
                    pharmacyName = pharmacyName,
                    distributorId = distributorId,
                    distributorName = distName,
                    status = OrderStatus.PENDING_APPROVAL,
                    totalAmount = totalAmount,
                    placedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    deliveryAddress = deliveryAddress
                )
                database.orderDao().insertOrder(orderEntity)

                val orderItems = distItems.map { item ->
                    OrderItemEntity(
                        id = "item_${UUID.randomUUID().toString().take(8)}",
                        orderId = orderId,
                        productListingId = item.selectedListingId,
                        medicationName = item.brandName,
                        quantity = item.quantity,
                        unitPriceAtOrder = item.unitPrice,
                        lineTotal = item.lineTotal
                    )
                }
                database.orderDao().insertOrderItems(orderItems)

                val commissionEntity = CommissionRecordEntity(
                    id = "comm_${UUID.randomUUID().toString().take(8)}",
                    orderId = orderId,
                    distributorId = distributorId,
                    orderTotal = totalAmount,
                    commissionRatePct = 4.5,
                    commissionAmount = totalAmount * 0.045,
                    status = CommissionStatus.PENDING,
                    computedAt = System.currentTimeMillis()
                )
                database.commissionDao().insertCommissionRecord(commissionEntity)

                createdOrderIds.add(orderId)
            }
            NetworkResult.Success(createdOrderIds)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to submit batch order", e)
        }
    }
}

class ForecastRepository(
    private val database: PharmaChainDatabase,
    private val catalogRepository: CatalogRepository
) {
    suspend fun saveUpload(
        fileName: String,
        fileSizeKb: Double,
        customHeaders: List<String>? = null,
        actualRowCount: Int = 0,
        pharmacyId: String = "pharm_demo"
    ): SalesHistoryUpload = withContext(Dispatchers.IO) {
        val uploadId = "upl_${System.currentTimeMillis()}"

        val count = if (actualRowCount > 0) actualRowCount else 485

        val entity = SalesHistoryUploadEntity(
            id = uploadId,
            pharmacyId = pharmacyId,
            fileName = fileName,
            fileSizeKb = fileSizeKb,
            remoteJobId = "rem_job_${UUID.randomUUID().toString().take(6)}",
            status = UploadStatus.PARSED,
            rowCount = count,
            uploadedAt = System.currentTimeMillis()
        )
        // 1. Insert parent SalesHistoryUploadEntity first to satisfy Foreign Key constraint
        database.forecastDao().insertUpload(entity)

        // 2. Detect appropriate headers from customHeaders or default
        val detectedHeaders = if (!customHeaders.isNullOrEmpty()) {
            customHeaders
        } else if (fileName.contains("arabic", ignoreCase = true) || fileName.contains("ar", ignoreCase = true) || fileName.contains("ezaby", ignoreCase = true)) {
            listOf("تاريخ البيع", "اسم الصنف", "الكمية المباعة", "سعر الجمهور (ج.م)", "كود الفرع")
        } else {
            listOf("Transaction_Date", "Item_Description", "Units_Sold", "Unit_Cost_EGP", "Branch_Code")
        }

        // Run through bilingual fuzzy matching engine
        val suggestedMatches = detectedHeaders.map { header ->
            com.pharmachain.ai.feature.forecasting.engine.DataCleaningAndMappingService.matchHeaderToField(header)
        }

        val initialMappings = suggestedMatches.mapIndexed { index, match ->
            ColumnMappingEntity(
                id = "cm_${uploadId}_${index + 1}",
                uploadId = uploadId,
                sourceColumnName = match.sourceColumnName,
                mappedField = match.suggestedField,
                serverConfidence = match.confidenceScore
            )
        }
        // 3. Insert child ColumnMappingEntity rows referencing the existing uploadId
        database.forecastDao().insertMappings(initialMappings)

        SalesHistoryUpload(
            id = entity.id,
            pharmacyId = entity.pharmacyId,
            fileName = entity.fileName,
            fileSizeKb = entity.fileSizeKb,
            remoteJobId = entity.remoteJobId,
            status = entity.status,
            rowCount = entity.rowCount,
            uploadedAt = entity.uploadedAt
        )
    }

    suspend fun getColumnMappings(uploadId: String): List<com.pharmachain.ai.core.model.ColumnMapping> = withContext(Dispatchers.IO) {
        database.forecastDao().getMappings(uploadId).map {
            com.pharmachain.ai.core.model.ColumnMapping(
                id = it.id,
                uploadId = it.uploadId,
                sourceColumnName = it.sourceColumnName,
                mappedField = it.mappedField,
                serverConfidence = it.serverConfidence
            )
        }
    }

    suspend fun saveMappings(mappings: List<com.pharmachain.ai.core.model.ColumnMapping>) = withContext(Dispatchers.IO) {
        val entities = mappings.map {
            ColumnMappingEntity(
                id = it.id,
                uploadId = it.uploadId,
                sourceColumnName = it.sourceColumnName,
                mappedField = it.mappedField,
                serverConfidence = it.serverConfidence
            )
        }
        database.forecastDao().insertMappings(entities)
    }

    suspend fun createAndRunForecastJobFromUploadedData(
        uploadId: String,
        duration: ForecastDuration,
        budget: Double,
        accountForSeasonality: Boolean,
        cleanedRows: List<Map<String, String>>,
        mappings: List<com.pharmachain.ai.core.model.ColumnMapping>,
        pharmacyId: String = "pharm_demo"
    ): ForecastJob = withContext(Dispatchers.IO) {
        val jobId = "job_${System.currentTimeMillis()}"
        val jobEntity = ForecastJobEntity(
            id = jobId,
            pharmacyId = pharmacyId,
            uploadId = uploadId,
            durationDays = duration,
            maxBudgetEGP = budget,
            status = JobStatus.COMPLETED,
            createdAt = System.currentTimeMillis(),
            completedAt = System.currentTimeMillis() + 1500
        )
        database.forecastDao().insertJob(jobEntity)

        // Retrieve catalog medications for matching
        val catalogMedications = catalogRepository.getMedications().first().map { it.medication }

        // Dynamically generate forecast predictions based on the user's actual uploaded file
        val results = com.pharmachain.ai.feature.forecasting.engine.DataCleaningAndMappingService.generatePredictionsFromUpload(
            jobId = jobId,
            duration = duration,
            maxBudgetEgp = budget,
            accountForSeasonality = accountForSeasonality,
            cleanedRows = cleanedRows,
            mappings = mappings,
            catalogMedications = catalogMedications
        )
        database.forecastDao().insertResults(results)

        ForecastJob(
            id = jobEntity.id,
            pharmacyId = jobEntity.pharmacyId,
            uploadId = jobEntity.uploadId,
            durationDays = jobEntity.durationDays,
            maxBudgetEGP = jobEntity.maxBudgetEGP,
            status = jobEntity.status,
            createdAt = jobEntity.createdAt,
            completedAt = jobEntity.completedAt
        )
    }

    suspend fun createAndRunForecastJob(
        uploadId: String,
        duration: ForecastDuration,
        budget: Double,
        pharmacyId: String = "pharm_demo"
    ): ForecastJob = createAndRunForecastJobFromUploadedData(
        uploadId = uploadId,
        duration = duration,
        budget = budget,
        accountForSeasonality = true,
        cleanedRows = emptyList(),
        mappings = emptyList(),
        pharmacyId = pharmacyId
    )

    fun getForecastResults(jobId: String): Flow<List<ForecastResult>> {
        return database.forecastDao().getResultsForJob(jobId).map { entities ->
            entities.map {
                ForecastResult(
                    id = it.id,
                    jobId = it.jobId,
                    medicationId = it.medicationId,
                    medicationName = it.medicationName,
                    category = it.category,
                    predictedQuantity = it.predictedQuantity,
                    confidenceScore = it.confidenceScore,
                    seasonalTrendDetected = it.seasonalTrendDetected
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun saveSmartCartItems(items: List<SmartCartItem>) = withContext(Dispatchers.IO) {
        val entities = items.map {
            SmartCartItemEntity(
                id = it.id,
                jobId = it.jobId,
                medicationId = it.medicationId,
                medicationName = it.medicationName,
                selectedProductListingId = it.selectedProductListingId,
                distributorId = it.distributorId,
                distributorName = it.distributorName,
                quantity = it.quantity,
                unitPrice = it.unitPrice,
                lineTotal = it.lineTotal,
                distributorReputationAtSelection = it.distributorReputationAtSelection,
                scoringRationale = it.scoringRationale,
                isSelected = it.isSelected
            )
        }
        database.forecastDao().insertSmartCartItems(entities)
    }

    fun getSmartCartItems(jobId: String): Flow<List<SmartCartItem>> {
        return database.forecastDao().getSmartCartItems(jobId).map { entities ->
            entities.map {
                SmartCartItem(
                    id = it.id,
                    jobId = it.jobId,
                    medicationId = it.medicationId,
                    medicationName = it.medicationName,
                    selectedProductListingId = it.selectedProductListingId,
                    distributorId = it.distributorId,
                    distributorName = it.distributorName,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice,
                    lineTotal = it.lineTotal,
                    distributorReputationAtSelection = it.distributorReputationAtSelection,
                    scoringRationale = it.scoringRationale,
                    isSelected = it.isSelected
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun toggleSmartCartSelection(itemId: String, isSelected: Boolean) = withContext(Dispatchers.IO) {
        database.forecastDao().updateCartItemSelection(itemId, isSelected)
    }
}

class DistributorRepository(
    private val database: PharmaChainDatabase,
    private val sessionManager: SessionManager? = null,
    private val networkClient: NetworkClient? = null
) {
    // In-memory hold for active upload parsing/validation steps
    private val uploadParsedLines = mutableMapOf<String, List<List<String>>>()
    private val uploadValidationCache = mutableMapOf<String, InventoryValidationResponseDto>()
    private val activeSyncConflicts = mutableListOf<SyncConflictItem>()

    // ==========================================================================
    // Inventory Listings & Sources
    // ==========================================================================

    fun getListingsForDistributor(distributorId: String = "dist_ibnsina"): Flow<List<ProductListing>> {
        return database.medicationDao().getListingsForDistributor(distributorId).map { list ->
            list.map {
                ProductListing(
                    id = it.id,
                    medicationId = it.medicationId,
                    distributorId = it.distributorId,
                    distributorName = it.distributorName,
                    distributorReputation = it.distributorReputation,
                    price = it.price,
                    stockQuantity = it.stockQuantity,
                    expiryDate = it.expiryDate,
                    updatedAt = it.updatedAt
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateListing(listingId: String, newPrice: Double, newStock: Int, distributorId: String = "dist_ibnsina") = withContext(Dispatchers.IO) {
        val existing = database.medicationDao().getListingsForDistributor(distributorId).first().firstOrNull { it.id == listingId }
        val medId = existing?.medicationId ?: "med_1"
        val distName = existing?.distributorName ?: "Ibnsina Pharma"
        val rep = existing?.distributorReputation ?: 4.9

        val item = ProductListingEntity(
            id = listingId,
            medicationId = medId,
            distributorId = distributorId,
            distributorName = distName,
            distributorReputation = rep,
            price = newPrice,
            stockQuantity = newStock,
            expiryDate = existing?.expiryDate ?: (System.currentTimeMillis() + 86400000L * 450),
            updatedAt = System.currentTimeMillis()
        )
        database.medicationDao().updateProductListing(item)

        // Track source as manual entry
        database.distributorDao().insertOrUpdateListingSource(
            ListingSourceEntity(
                productListingId = listingId,
                source = ListingSourceType.MANUAL_ENTRY,
                lastUpdatedAt = System.currentTimeMillis(),
                lastUpdatedByUploadId = null,
                lastUpdatedByIntegrationId = null
            )
        )
        refreshLowStockAlerts(distributorId)
    }

    // ==========================================================================
    // Orders & Approval Gate (Section 4)
    // ==========================================================================

    fun getIncomingOrders(distributorId: String = "dist_ibnsina"): Flow<List<Order>> {
        return database.orderDao().getOrdersForDistributor(distributorId).map { relations ->
            relations.map { r ->
                Order(
                    id = r.order.id,
                    pharmacyId = r.order.pharmacyId,
                    pharmacyName = r.order.pharmacyName,
                    distributorId = r.order.distributorId,
                    distributorName = r.order.distributorName,
                    status = r.order.status,
                    totalAmount = r.order.totalAmount,
                    placedAt = r.order.placedAt,
                    updatedAt = r.order.updatedAt,
                    deliveryAddress = r.order.deliveryAddress,
                    items = r.items.map { i ->
                        OrderItem(
                            id = i.id,
                            orderId = i.orderId,
                            productListingId = i.productListingId,
                            medicationName = i.medicationName,
                            quantity = i.quantity,
                            unitPriceAtOrder = i.unitPriceAtOrder,
                            lineTotal = i.lineTotal
                        )
                    },
                    rejectionReason = r.order.rejectionReason
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun approveOrder(orderId: String) = withContext(Dispatchers.IO) {
        database.orderDao().updateOrderStatus(orderId, OrderStatus.CONFIRMED, System.currentTimeMillis())
    }

    suspend fun rejectOrder(orderId: String, reason: String) = withContext(Dispatchers.IO) {
        database.orderDao().updateOrderStatusWithReason(orderId, OrderStatus.REJECTED, System.currentTimeMillis(), reason)
    }

    suspend fun advanceOrderStatus(orderId: String, newStatus: OrderStatus) = withContext(Dispatchers.IO) {
        database.orderDao().updateOrderStatus(orderId, newStatus, System.currentTimeMillis())
    }

    // ==========================================================================
    // Section 3.1: Excel / CSV Bulk Upload Flow
    // ==========================================================================

    suspend fun parseAndCreateUpload(
        fileName: String,
        lines: List<List<String>>,
        distributorId: String = "dist_ibnsina"
    ): InventoryUpload = withContext(Dispatchers.IO) {
        val uploadId = "upl_${System.currentTimeMillis()}"
        val remoteJobId = "job_inv_${UUID.randomUUID().toString().take(8)}"

        uploadParsedLines[uploadId] = lines

        val uploadEntity = InventoryUploadEntity(
            id = uploadId,
            distributorId = distributorId,
            fileName = fileName,
            remoteJobId = remoteJobId,
            status = InventoryUploadStatus.MAPPING_REQUIRED,
            rowCount = maxOf(0, lines.size - 1),
            successCount = null,
            errorCount = null,
            uploadedAt = System.currentTimeMillis(),
            appliedAt = null
        )
        database.distributorDao().insertInventoryUpload(uploadEntity)

        // Suggest automatic column mappings based on headers
        val headers = lines.firstOrNull() ?: emptyList()
        val detectedMappings = headers.map { header ->
            val clean = header.trim().lowercase()
            val field = when {
                clean.contains("name") || clean.contains("product") || clean.contains("item") || clean.contains("اسم") || clean.contains("صنف") || clean.contains("دواء") || clean.contains("sku") ->
                    InventoryMappingField.PRODUCT_NAME_OR_SKU
                clean.contains("price") || clean.contains("سعر") || clean.contains("cost") || clean.contains("egp") ->
                    InventoryMappingField.PRICE
                clean.contains("stock") || clean.contains("qty") || clean.contains("quantity") || clean.contains("كمية") || clean.contains("رصيد") || clean.contains("مخزون") ->
                    InventoryMappingField.STOCK_QUANTITY
                clean.contains("expir") || clean.contains("date") || clean.contains("صلاحية") || clean.contains("تاريخ") ->
                    InventoryMappingField.EXPIRY_DATE
                else -> InventoryMappingField.IGNORE
            }
            InventoryColumnMappingEntity(
                id = "map_${UUID.randomUUID().toString().take(8)}",
                uploadId = uploadId,
                sourceColumnName = header,
                mappedField = field,
                serverConfidence = if (field != InventoryMappingField.IGNORE) 0.95 else 0.40
            )
        }
        database.distributorDao().insertColumnMappings(detectedMappings)

        InventoryUpload(
            id = uploadEntity.id,
            distributorId = uploadEntity.distributorId,
            fileName = uploadEntity.fileName,
            remoteJobId = uploadEntity.remoteJobId,
            status = uploadEntity.status,
            rowCount = uploadEntity.rowCount,
            successCount = uploadEntity.successCount,
            errorCount = uploadEntity.errorCount,
            uploadedAt = uploadEntity.uploadedAt,
            appliedAt = uploadEntity.appliedAt
        )
    }

    suspend fun getColumnMappings(uploadId: String): List<InventoryColumnMapping> = withContext(Dispatchers.IO) {
        database.distributorDao().getColumnMappings(uploadId).map {
            InventoryColumnMapping(
                id = it.id,
                uploadId = it.uploadId,
                sourceColumnName = it.sourceColumnName,
                mappedField = it.mappedField,
                serverConfidence = it.serverConfidence
            )
        }
    }

    suspend fun saveColumnMappings(uploadId: String, mappings: Map<String, InventoryMappingField>) = withContext(Dispatchers.IO) {
        val entities = mappings.map { (header, field) ->
            InventoryColumnMappingEntity(
                id = "map_${UUID.randomUUID().toString().take(8)}",
                uploadId = uploadId,
                sourceColumnName = header,
                mappedField = field,
                serverConfidence = 1.0
            )
        }
        database.distributorDao().insertColumnMappings(entities)
    }

    suspend fun validateInventoryUpload(uploadId: String, distributorId: String = "dist_ibnsina"): InventoryValidationResponseDto = withContext(Dispatchers.IO) {
        val lines = uploadParsedLines[uploadId] ?: emptyList()
        val mappings = database.distributorDao().getColumnMappings(uploadId).associate { it.sourceColumnName to it.mappedField }

        val headers = lines.firstOrNull() ?: emptyList()
        val nameColIdx = headers.indexOfFirst { mappings[it] == InventoryMappingField.PRODUCT_NAME_OR_SKU }
        val priceColIdx = headers.indexOfFirst { mappings[it] == InventoryMappingField.PRICE }
        val stockColIdx = headers.indexOfFirst { mappings[it] == InventoryMappingField.STOCK_QUANTITY }

        val currentListings = database.medicationDao().getListingsForDistributor(distributorId).first()
        val allMedications = database.medicationDao().getAllMedications().first()

        val validationRows = mutableListOf<InventoryValidationRowDto>()
        var matchedCount = 0
        var newCount = 0
        var errorCount = 0

        for (i in 1 until lines.size) {
            val row = lines[i]
            val rawName = if (nameColIdx in row.indices) row[nameColIdx].trim() else ""
            val rawPrice = if (priceColIdx in row.indices) row[priceColIdx].replace(",", ".").toDoubleOrNull() else null
            val rawStock = if (stockColIdx in row.indices) row[stockColIdx].filter { it.isDigit() }.toIntOrNull() else null

            if (rawName.isBlank() || (rawPrice == null && rawStock == null)) {
                errorCount++
                validationRows.add(
                    InventoryValidationRowDto(
                        rowIndex = i,
                        productNameOrSku = if (rawName.isBlank()) "Row $i (Missing name)" else rawName,
                        matchedMedicationId = null,
                        matchedMedicationName = null,
                        incomingPrice = rawPrice,
                        currentPrice = null,
                        incomingStock = rawStock,
                        currentStock = null,
                        matchStatus = "ERROR",
                        errorMessage = "Missing product name or unparseable price/stock values"
                    )
                )
                continue
            }

            // Look up medication in catalog
            val matchedMed = allMedications.firstOrNull {
                it.brandName.contains(rawName, ignoreCase = true) ||
                it.genericName.contains(rawName, ignoreCase = true) ||
                rawName.contains(it.brandName, ignoreCase = true) ||
                rawName.contains(it.genericName, ignoreCase = true)
            }

            val matchedListing = currentListings.firstOrNull { listing ->
                val med = allMedications.firstOrNull { it.id == listing.medicationId }
                med != null && (
                    med.brandName.contains(rawName, ignoreCase = true) ||
                    rawName.contains(med.brandName, ignoreCase = true)
                )
            }

            if (matchedListing != null) {
                matchedCount++
                val medName = allMedications.firstOrNull { it.id == matchedListing.medicationId }?.brandName ?: rawName
                validationRows.add(
                    InventoryValidationRowDto(
                        rowIndex = i,
                        productNameOrSku = rawName,
                        matchedMedicationId = matchedListing.medicationId,
                        matchedMedicationName = medName,
                        incomingPrice = rawPrice ?: matchedListing.price,
                        currentPrice = matchedListing.price,
                        incomingStock = rawStock ?: matchedListing.stockQuantity,
                        currentStock = matchedListing.stockQuantity,
                        matchStatus = "MATCHED"
                    )
                )
            } else if (matchedMed != null) {
                newCount++
                validationRows.add(
                    InventoryValidationRowDto(
                        rowIndex = i,
                        productNameOrSku = rawName,
                        matchedMedicationId = matchedMed.id,
                        matchedMedicationName = matchedMed.brandName,
                        incomingPrice = rawPrice ?: 50.0,
                        currentPrice = null,
                        incomingStock = rawStock ?: 100,
                        currentStock = null,
                        matchStatus = "NEW"
                    )
                )
            } else {
                // Unknown medication in catalog - mark as error or new custom listing
                newCount++
                validationRows.add(
                    InventoryValidationRowDto(
                        rowIndex = i,
                        productNameOrSku = rawName,
                        matchedMedicationId = "custom_${UUID.randomUUID().toString().take(6)}",
                        matchedMedicationName = rawName,
                        incomingPrice = rawPrice ?: 50.0,
                        currentPrice = null,
                        incomingStock = rawStock ?: 50,
                        currentStock = null,
                        matchStatus = "NEW"
                    )
                )
            }
        }

        val result = InventoryValidationResponseDto(
            uploadId = uploadId,
            totalRows = lines.size - 1,
            matchedCount = matchedCount,
            newCount = newCount,
            errorCount = errorCount,
            validationRows = validationRows
        )
        uploadValidationCache[uploadId] = result
        result
    }

    suspend fun applyInventoryUpload(uploadId: String, distributorId: String = "dist_ibnsina"): Int = withContext(Dispatchers.IO) {
        val validation = uploadValidationCache[uploadId] ?: validateInventoryUpload(uploadId, distributorId)
        val currentListings = database.medicationDao().getListingsForDistributor(distributorId).first()
        val allMedications = database.medicationDao().getAllMedications().first()

        var appliedCount = 0
        val listingSources = mutableListOf<ListingSourceEntity>()

        for (row in validation.validationRows) {
            if (row.matchStatus == "ERROR") continue

            val existingListing = currentListings.firstOrNull { it.medicationId == row.matchedMedicationId }
            if (existingListing != null) {
                val updatedListing = existingListing.copy(
                    price = row.incomingPrice ?: existingListing.price,
                    stockQuantity = row.incomingStock ?: existingListing.stockQuantity,
                    updatedAt = System.currentTimeMillis()
                )
                database.medicationDao().updateProductListing(updatedListing)
                listingSources.add(
                    ListingSourceEntity(
                        productListingId = existingListing.id,
                        source = ListingSourceType.EXCEL_UPLOAD,
                        lastUpdatedAt = System.currentTimeMillis(),
                        lastUpdatedByUploadId = uploadId,
                        lastUpdatedByIntegrationId = null
                    )
                )
                appliedCount++
            } else {
                val listingId = "list_upl_${UUID.randomUUID().toString().take(8)}"
                val medId = row.matchedMedicationId ?: "med_custom_${UUID.randomUUID().toString().take(6)}"

                // If brand new medication not in catalog, insert medication entity
                if (allMedications.none { it.id == medId }) {
                    database.medicationDao().insertMedications(
                        listOf(
                            MedicationEntity(
                                id = medId,
                                genericName = row.productNameOrSku,
                                brandName = row.productNameOrSku,
                                form = "Tablet",
                                strength = "Standard",
                                category = "General Medicine",
                                imageUrl = null
                            )
                        )
                    )
                }

                val newListing = ProductListingEntity(
                    id = listingId,
                    medicationId = medId,
                    distributorId = distributorId,
                    distributorName = "Ibnsina Pharma",
                    distributorReputation = 4.9,
                    price = row.incomingPrice ?: 50.0,
                    stockQuantity = row.incomingStock ?: 100,
                    expiryDate = System.currentTimeMillis() + 86400000L * 365,
                    updatedAt = System.currentTimeMillis()
                )
                database.medicationDao().insertProductListings(listOf(newListing))
                listingSources.add(
                    ListingSourceEntity(
                        productListingId = listingId,
                        source = ListingSourceType.EXCEL_UPLOAD,
                        lastUpdatedAt = System.currentTimeMillis(),
                        lastUpdatedByUploadId = uploadId,
                        lastUpdatedByIntegrationId = null
                    )
                )
                appliedCount++
            }
        }

        database.distributorDao().insertOrUpdateListingSources(listingSources)
        database.distributorDao().updateInventoryUploadStatus(
            uploadId = uploadId,
            status = InventoryUploadStatus.APPLIED,
            rowCount = validation.totalRows,
            successCount = validation.matchedCount + validation.newCount,
            errorCount = validation.errorCount,
            appliedAt = System.currentTimeMillis()
        )
        refreshLowStockAlerts(distributorId)
        appliedCount
    }

    fun getInventoryUploads(distributorId: String = "dist_ibnsina"): Flow<List<InventoryUpload>> {
        return database.distributorDao().getInventoryUploads(distributorId).map { entities ->
            entities.map {
                InventoryUpload(
                    id = it.id,
                    distributorId = it.distributorId,
                    fileName = it.fileName,
                    remoteJobId = it.remoteJobId,
                    status = it.status,
                    rowCount = it.rowCount,
                    successCount = it.successCount,
                    errorCount = it.errorCount,
                    uploadedAt = it.uploadedAt,
                    appliedAt = it.appliedAt
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    // ==========================================================================
    // Section 3.2 & 3.3: API Key / ERP System Integration & Conflict Resolution
    // ==========================================================================

    fun getApiIntegration(distributorId: String = "dist_ibnsina"): Flow<ApiIntegration?> {
        return database.distributorDao().getApiIntegration(distributorId).map { entity ->
            entity?.let {
                ApiIntegration(
                    id = it.id,
                    distributorId = it.distributorId,
                    providerName = it.providerName,
                    baseUrl = it.baseUrl,
                    authType = it.authType,
                    credentialRef = it.credentialRef,
                    syncFrequency = it.syncFrequency,
                    lastSyncAt = it.lastSyncAt,
                    lastSyncStatus = it.lastSyncStatus,
                    lastSyncErrorMessage = it.lastSyncErrorMessage,
                    isActive = it.isActive
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun saveApiIntegration(
        providerName: String,
        baseUrl: String,
        authType: ApiAuthType,
        secret: String,
        syncFrequency: ApiSyncFrequency,
        distributorId: String = "dist_ibnsina"
    ) = withContext(Dispatchers.IO) {
        val credentialRef = "cred_ref_${UUID.randomUUID().toString().take(10)}"

        // Store raw secret strictly in EncryptedSharedPreferences via SessionManager
        sessionManager?.saveIntegrationSecret(credentialRef, secret)

        val entity = ApiIntegrationEntity(
            id = "integ_${UUID.randomUUID().toString().take(8)}",
            distributorId = distributorId,
            providerName = providerName,
            baseUrl = baseUrl,
            authType = authType,
            credentialRef = credentialRef,
            syncFrequency = syncFrequency,
            lastSyncAt = null,
            lastSyncStatus = ApiSyncStatus.NEVER_RUN,
            lastSyncErrorMessage = null,
            isActive = true
        )
        database.distributorDao().insertOrUpdateApiIntegration(entity)
    }

    suspend fun testApiConnection(
        providerName: String,
        baseUrl: String,
        authType: ApiAuthType,
        secret: String
    ): Boolean = withContext(Dispatchers.IO) {
        kotlinx.coroutines.delay(1200) // Realistic handshake latency
        if (baseUrl.isBlank()) false else true
    }

    suspend fun triggerSyncNow(integrationId: String, distributorId: String = "dist_ibnsina"): List<SyncConflictItem> = withContext(Dispatchers.IO) {
        kotlinx.coroutines.delay(1500) // Simulated background sync
        val now = System.currentTimeMillis()
        val twentyFourHoursAgo = now - (24L * 60 * 60 * 1000)

        val listings = database.medicationDao().getListingsForDistributor(distributorId).first()
        val allMedications = database.medicationDao().getAllMedications().first()
        val sources = database.distributorDao().getAllListingSources().associateBy { it.productListingId }

        val conflicts = mutableListOf<SyncConflictItem>()
        val sourcesToUpdate = mutableListOf<ListingSourceEntity>()

        listings.forEach { listing ->
            val source = sources[listing.id]
            val med = allMedications.firstOrNull { it.id == listing.medicationId }
            val medName = med?.brandName ?: "Pharmaceutical Item"

            // Check if listing was updated via Excel or Manual entry in last 24h
            if (source != null && source.source != ListingSourceType.API_SYNC && source.lastUpdatedAt >= twentyFourHoursAgo) {
                // 24h Conflict detected per Section 3.3
                val incomingPrice = listing.price * 1.05
                val incomingStock = listing.stockQuantity + 50
                conflicts.add(
                    SyncConflictItem(
                        productListingId = listing.id,
                        medicationName = medName,
                        currentPrice = listing.price,
                        incomingPrice = incomingPrice,
                        currentStock = listing.stockQuantity,
                        incomingStock = incomingStock,
                        lastSource = source.source,
                        lastUpdatedAt = source.lastUpdatedAt
                    )
                )
            } else {
                // Non-conflicting row: apply automatically
                sourcesToUpdate.add(
                    ListingSourceEntity(
                        productListingId = listing.id,
                        source = ListingSourceType.API_SYNC,
                        lastUpdatedAt = now,
                        lastUpdatedByUploadId = null,
                        lastUpdatedByIntegrationId = integrationId
                    )
                )
            }
        }

        database.distributorDao().insertOrUpdateListingSources(sourcesToUpdate)
        database.distributorDao().updateSyncStatus(
            id = integrationId,
            lastSyncAt = now,
            status = if (conflicts.isEmpty()) ApiSyncStatus.SUCCESS else ApiSyncStatus.PARTIAL_FAILURE,
            errorMessage = if (conflicts.isNotEmpty()) "${conflicts.size} listing(s) have manual changes within 24h requiring distributor review." else null
        )

        activeSyncConflicts.clear()
        activeSyncConflicts.addAll(conflicts)
        refreshLowStockAlerts(distributorId)
        conflicts
    }

    fun getActiveConflicts(): List<SyncConflictItem> = activeSyncConflicts.toList()

    suspend fun resolveConflict(
        item: SyncConflictItem,
        action: ConflictResolutionAction,
        integrationId: String,
        distributorId: String = "dist_ibnsina"
    ) = withContext(Dispatchers.IO) {
        if (action == ConflictResolutionAction.ACCEPT) {
            val existing = database.medicationDao().getListingsForDistributor(distributorId).first().firstOrNull { it.id == item.productListingId }
            if (existing != null) {
                val updated = existing.copy(
                    price = item.incomingPrice,
                    stockQuantity = item.incomingStock,
                    updatedAt = System.currentTimeMillis()
                )
                database.medicationDao().updateProductListing(updated)
                database.distributorDao().insertOrUpdateListingSource(
                    ListingSourceEntity(
                        productListingId = item.productListingId,
                        source = ListingSourceType.API_SYNC,
                        lastUpdatedAt = System.currentTimeMillis(),
                        lastUpdatedByUploadId = null,
                        lastUpdatedByIntegrationId = integrationId
                    )
                )
            }
        }
        activeSyncConflicts.removeAll { it.productListingId == item.productListingId }
    }

    suspend fun disconnectApiIntegration(distributorId: String = "dist_ibnsina") = withContext(Dispatchers.IO) {
        val existing = database.distributorDao().getApiIntegration(distributorId).first()
        if (existing != null) {
            // Purge secret from EncryptedSharedPreferences
            sessionManager?.deleteIntegrationSecret(existing.credentialRef)
            database.distributorDao().deleteApiIntegration(distributorId)
        }
    }

    // ==========================================================================
    // Section 7: Analytics & Offline-First Snapshot Caching
    // ==========================================================================

    suspend fun getAnalytics(periodDays: Int = 30, distributorId: String = "dist_ibnsina"): DistributorAnalyticsDto = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val periodStart = now - (periodDays.toLong() * 86400000L)

        val orders = database.orderDao().getOrdersForDistributor(distributorId).first()
        val periodOrders = orders.filter { it.order.placedAt >= periodStart }

        val totalOrders = if (periodOrders.isNotEmpty()) periodOrders.size else 14
        val totalRevenue = if (periodOrders.isNotEmpty()) periodOrders.sumOf { it.order.totalAmount } else 142850.0
        val totalUnits = if (periodOrders.isNotEmpty()) periodOrders.sumOf { o -> o.items.sumOf { it.quantity } } else 1840
        val avgOrderValue = if (totalOrders > 0) totalRevenue / totalOrders else 0.0
        val fulfillmentRatePct = 98.4
        val avgApprovalTimeMinutes = 18.5

        // Top 5 selling medications
        val allMedications = database.medicationDao().getAllMedications().first()
        val topSelling = listOf(
            TopSellingMedicationDto("med_1", "Panadol Extra 500mg", 420, 18900.0),
            TopSellingMedicationDto("med_2", "Augmentin 1g Tablets", 210, 24150.0),
            TopSellingMedicationDto("med_3", "Concor 5mg Plus", 310, 19220.0),
            TopSellingMedicationDto("med_5", "Antinal 200mg Capsules", 290, 11600.0),
            TopSellingMedicationDto("med_7", "Ketofan 75mg Ampoules", 180, 5040.0)
        )

        // Generate daily revenue trend points
        val dailyTrend = (0 until minOf(periodDays, 14)).map { dayIndex ->
            val dayTs = now - ((periodDays - 1 - dayIndex) * 86400000L)
            val baseRev = (totalRevenue / minOf(periodDays, 14)) * (0.8 + (dayIndex % 5) * 0.1)
            DailyRevenuePointDto(
                dayTimestamp = dayTs,
                revenue = baseRev,
                orderCount = maxOf(1, (baseRev / 4000).toInt())
            )
        }

        // Cache snapshot to Room for offline first display
        database.distributorDao().insertAnalyticsSnapshot(
            DistributorAnalyticsSnapshotEntity(
                distributorId = distributorId,
                periodStart = periodStart,
                periodEnd = now,
                totalRevenue = totalRevenue,
                totalOrders = totalOrders,
                totalUnitsSold = totalUnits,
                topSellingMedicationId = topSelling.firstOrNull()?.medicationId,
                avgOrderValue = avgOrderValue,
                fulfillmentRatePct = fulfillmentRatePct,
                avgApprovalTimeMinutes = avgApprovalTimeMinutes,
                computedAt = now
            )
        )

        DistributorAnalyticsDto(
            distributorId = distributorId,
            periodDays = periodDays,
            totalRevenue = totalRevenue,
            totalOrders = totalOrders,
            totalUnitsSold = totalUnits,
            avgOrderValue = avgOrderValue,
            fulfillmentRatePct = fulfillmentRatePct,
            avgApprovalTimeMinutes = avgApprovalTimeMinutes,
            topSellingMedications = topSelling,
            dailyRevenueTrend = dailyTrend
        )
    }

    // ==========================================================================
    // Section 7: Low Stock Alerts
    // ==========================================================================

    fun getLowStockAlerts(distributorId: String = "dist_ibnsina"): Flow<List<LowStockAlert>> {
        return database.distributorDao().getActiveLowStockAlerts(distributorId).map { entities ->
            val allMeds = database.medicationDao().getAllMedications().first().associateBy { it.id }
            val listings = database.medicationDao().getListingsForDistributor(distributorId).first().associateBy { it.id }

            entities.map { entity ->
                val medName = allMeds[entity.medicationId]?.brandName ?: "Pharmaceutical Item"
                val price = listings[entity.productListingId]?.price ?: 0.0
                LowStockAlert(
                    id = entity.id,
                    distributorId = entity.distributorId,
                    medicationId = entity.medicationId,
                    medicationName = medName,
                    productListingId = entity.productListingId,
                    currentStock = entity.currentStock,
                    thresholdStock = entity.thresholdStock,
                    unitPrice = price,
                    triggeredAt = entity.triggeredAt,
                    isDismissed = entity.isDismissed
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun dismissLowStockAlert(alertId: String) = withContext(Dispatchers.IO) {
        database.distributorDao().dismissLowStockAlert(alertId)
    }

    suspend fun refreshLowStockAlerts(distributorId: String = "dist_cairo_drugs") = withContext(Dispatchers.IO) {
        val listings = database.medicationDao().getListingsForDistributor(distributorId).first()
        val defaultThreshold = 25

        val lowStockEntities = listings.filter { it.stockQuantity <= defaultThreshold }.map { listing ->
            LowStockAlertEntity(
                id = "alert_${listing.id}",
                distributorId = distributorId,
                medicationId = listing.medicationId,
                productListingId = listing.id,
                currentStock = listing.stockQuantity,
                thresholdStock = defaultThreshold,
                triggeredAt = System.currentTimeMillis(),
                isDismissed = false
            )
        }
        database.distributorDao().insertLowStockAlerts(lowStockEntities)
    }

    fun getCommissionLedger(distributorId: String = "dist_cairo_drugs"): Flow<List<CommissionRecord>> {
        return database.commissionDao().getCommissionRecords(distributorId).map { list ->
            list.map {
                CommissionRecord(
                    id = it.id,
                    orderId = it.orderId,
                    distributorId = it.distributorId,
                    orderTotal = it.orderTotal,
                    commissionRatePct = it.commissionRatePct,
                    commissionAmount = it.commissionAmount,
                    status = it.status,
                    computedAt = it.computedAt
                )
            }
        }.flowOn(Dispatchers.IO)
    }
}
