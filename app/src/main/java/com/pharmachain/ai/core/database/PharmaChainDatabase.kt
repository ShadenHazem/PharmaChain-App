package com.pharmachain.ai.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pharmachain.ai.core.database.dao.ChatMessageDao
import com.pharmachain.ai.core.database.dao.CommissionDao
import com.pharmachain.ai.core.database.dao.DistributorDao
import com.pharmachain.ai.core.database.dao.ForecastDao
import com.pharmachain.ai.core.database.dao.MedicationDao
import com.pharmachain.ai.core.database.dao.OrderDao
import com.pharmachain.ai.core.database.dao.PharmacyDao
import com.pharmachain.ai.core.database.dao.UserDao
import com.pharmachain.ai.core.database.entity.ApiIntegrationEntity
import com.pharmachain.ai.core.database.entity.ChatMessageEntity
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

@Database(
    entities = [
        UserEntity::class,
        PharmacyProfileEntity::class,
        DistributorProfileEntity::class,
        MedicationEntity::class,
        ProductListingEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        CommissionRecordEntity::class,
        SalesHistoryUploadEntity::class,
        ColumnMappingEntity::class,
        ForecastJobEntity::class,
        ForecastResultEntity::class,
        SmartCartItemEntity::class,
        InventoryUploadEntity::class,
        InventoryColumnMappingEntity::class,
        ApiIntegrationEntity::class,
        ListingSourceEntity::class,
        DistributorAnalyticsSnapshotEntity::class,
        LowStockAlertEntity::class,
        ChatMessageEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PharmaChainDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun pharmacyDao(): PharmacyDao
    abstract fun distributorDao(): DistributorDao
    abstract fun medicationDao(): MedicationDao
    abstract fun orderDao(): OrderDao
    abstract fun commissionDao(): CommissionDao
    abstract fun forecastDao(): ForecastDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: PharmaChainDatabase? = null

        fun getDatabase(context: Context): PharmaChainDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PharmaChainDatabase::class.java,
                    "pharmachain_egypt.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
