package com.pharmachain.ai.core.database.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.pharmachain.ai.core.database.entity.*
import com.pharmachain.ai.core.model.*
import kotlinx.coroutines.flow.Flow

data class MedicationWithListingsRelation(
    @Embedded val medication: MedicationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "medicationId"
    )
    val listings: List<ProductListingEntity>
)

data class OrderWithItemsRelation(
    @Embedded val order: OrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItemEntity>
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
}

@Dao
interface PharmacyDao {
    @Query("SELECT * FROM pharmacy_profiles WHERE userId = :userId")
    fun getProfileByUserId(userId: String): Flow<PharmacyProfileEntity?>

    @Query("SELECT * FROM pharmacy_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getProfileByUserIdSync(userId: String): PharmacyProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: PharmacyProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<PharmacyProfileEntity>)
}

@Dao
interface DistributorDao {
    @Query("SELECT * FROM distributor_profiles WHERE userId = :userId")
    fun getProfileByUserId(userId: String): Flow<DistributorProfileEntity?>

    @Query("SELECT * FROM distributor_profiles WHERE id = :id")
    suspend fun getProfileById(id: String): DistributorProfileEntity?

    @Query("SELECT * FROM distributor_profiles")
    fun getAllDistributors(): Flow<List<DistributorProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: DistributorProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<DistributorProfileEntity>)

    // Inventory Uploads
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryUpload(upload: InventoryUploadEntity)

    @Query("SELECT * FROM distributor_inventory_uploads WHERE distributorId = :distributorId ORDER BY uploadedAt DESC")
    fun getInventoryUploads(distributorId: String): Flow<List<InventoryUploadEntity>>

    @Query("SELECT * FROM distributor_inventory_uploads WHERE id = :uploadId")
    suspend fun getInventoryUploadById(uploadId: String): InventoryUploadEntity?

    @Query("UPDATE distributor_inventory_uploads SET status = :status, rowCount = :rowCount, successCount = :successCount, errorCount = :errorCount, appliedAt = :appliedAt WHERE id = :uploadId")
    suspend fun updateInventoryUploadStatus(
        uploadId: String,
        status: InventoryUploadStatus,
        rowCount: Int?,
        successCount: Int?,
        errorCount: Int?,
        appliedAt: Long?
    )

    // Column Mappings
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColumnMappings(mappings: List<InventoryColumnMappingEntity>)

    @Query("SELECT * FROM distributor_column_mappings WHERE uploadId = :uploadId")
    suspend fun getColumnMappings(uploadId: String): List<InventoryColumnMappingEntity>

    // API Integrations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateApiIntegration(integration: ApiIntegrationEntity)

    @Query("SELECT * FROM distributor_api_integrations WHERE distributorId = :distributorId LIMIT 1")
    fun getApiIntegration(distributorId: String): Flow<ApiIntegrationEntity?>

    @Query("SELECT * FROM distributor_api_integrations WHERE id = :id LIMIT 1")
    suspend fun getApiIntegrationById(id: String): ApiIntegrationEntity?

    @Query("DELETE FROM distributor_api_integrations WHERE distributorId = :distributorId")
    suspend fun deleteApiIntegration(distributorId: String)

    @Query("UPDATE distributor_api_integrations SET lastSyncAt = :lastSyncAt, lastSyncStatus = :status, lastSyncErrorMessage = :errorMessage WHERE id = :id")
    suspend fun updateSyncStatus(id: String, lastSyncAt: Long, status: ApiSyncStatus, errorMessage: String?)

    // Listing Sources
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateListingSource(source: ListingSourceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateListingSources(sources: List<ListingSourceEntity>)

    @Query("SELECT * FROM listing_sources WHERE productListingId = :listingId")
    suspend fun getListingSource(listingId: String): ListingSourceEntity?

    @Query("SELECT * FROM listing_sources")
    suspend fun getAllListingSources(): List<ListingSourceEntity>

    // Analytics Snapshot
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyticsSnapshot(snapshot: DistributorAnalyticsSnapshotEntity)

    @Query("SELECT * FROM distributor_analytics_snapshots WHERE distributorId = :distributorId LIMIT 1")
    fun getAnalyticsSnapshot(distributorId: String): Flow<DistributorAnalyticsSnapshotEntity?>

    // Low Stock Alerts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLowStockAlerts(alerts: List<LowStockAlertEntity>)

    @Query("SELECT * FROM distributor_low_stock_alerts WHERE distributorId = :distributorId AND isDismissed = 0 ORDER BY triggeredAt DESC")
    fun getActiveLowStockAlerts(distributorId: String): Flow<List<LowStockAlertEntity>>

    @Query("UPDATE distributor_low_stock_alerts SET isDismissed = 1 WHERE id = :alertId")
    suspend fun dismissLowStockAlert(alertId: String)

    @Query("DELETE FROM distributor_low_stock_alerts WHERE distributorId = :distributorId")
    suspend fun clearLowStockAlerts(distributorId: String)
}

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Transaction
    @Query("SELECT * FROM medications")
    fun getMedicationsWithListings(): Flow<List<MedicationWithListingsRelation>>

    @Transaction
    @Query("SELECT * FROM medications WHERE genericName LIKE '%' || :query || '%' OR brandName LIKE '%' || :query || '%'")
    fun searchMedicationsWithListings(query: String): Flow<List<MedicationWithListingsRelation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(medications: List<MedicationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductListings(listings: List<ProductListingEntity>)

    @Update
    suspend fun updateProductListing(listing: ProductListingEntity)

    @Query("SELECT * FROM product_listings WHERE distributorId = :distributorId")
    fun getListingsForDistributor(distributorId: String): Flow<List<ProductListingEntity>>
}

@Dao
interface OrderDao {
    @Transaction
    @Query("SELECT * FROM orders WHERE pharmacyId = :pharmacyId ORDER BY placedAt DESC")
    fun getOrdersForPharmacy(pharmacyId: String): Flow<List<OrderWithItemsRelation>>

    @Transaction
    @Query("SELECT * FROM orders WHERE distributorId = :distributorId ORDER BY placedAt DESC")
    fun getOrdersForDistributor(distributorId: String): Flow<List<OrderWithItemsRelation>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderById(orderId: String): Flow<OrderWithItemsRelation?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("UPDATE orders SET status = :status, updatedAt = :updatedAt, rejectionReason = :rejectionReason WHERE id = :orderId")
    suspend fun updateOrderStatusWithReason(orderId: String, status: OrderStatus, updatedAt: Long, rejectionReason: String?)

    @Query("UPDATE orders SET status = :status, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus, updatedAt: Long)
}

@Dao
interface CommissionDao {
    @Query("SELECT * FROM commission_records WHERE distributorId = :distributorId ORDER BY computedAt DESC")
    fun getCommissionRecords(distributorId: String): Flow<List<CommissionRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommissionRecord(record: CommissionRecordEntity)
}

@Dao
interface ForecastDao {
    @Query("SELECT * FROM sales_history_uploads WHERE pharmacyId = :pharmacyId ORDER BY uploadedAt DESC LIMIT 1")
    fun getLatestUpload(pharmacyId: String): Flow<SalesHistoryUploadEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpload(upload: SalesHistoryUploadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMappings(mappings: List<ColumnMappingEntity>)

    @Query("SELECT * FROM column_mappings WHERE uploadId = :uploadId")
    suspend fun getMappings(uploadId: String): List<ColumnMappingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: ForecastJobEntity)

    @Query("SELECT * FROM forecast_jobs WHERE id = :jobId")
    fun getJobById(jobId: String): Flow<ForecastJobEntity?>

    @Query("SELECT * FROM forecast_jobs WHERE pharmacyId = :pharmacyId ORDER BY createdAt DESC")
    fun getJobsForPharmacy(pharmacyId: String): Flow<List<ForecastJobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<ForecastResultEntity>)

    @Query("SELECT * FROM forecast_results WHERE jobId = :jobId")
    fun getResultsForJob(jobId: String): Flow<List<ForecastResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmartCartItems(items: List<SmartCartItemEntity>)

    @Query("SELECT * FROM smart_cart_items WHERE jobId = :jobId")
    fun getSmartCartItems(jobId: String): Flow<List<SmartCartItemEntity>>

    @Query("UPDATE smart_cart_items SET isSelected = :isSelected WHERE id = :itemId")
    suspend fun updateCartItemSelection(itemId: String, isSelected: Boolean)
}
