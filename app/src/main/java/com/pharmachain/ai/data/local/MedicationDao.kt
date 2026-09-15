package com.pharmachain.ai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO providing FTS / LIKE search queries and bulk upsert operations
 * for the offline-first pharmacy catalog.
 */
@Dao
interface MedicationDao {

    @Query("SELECT * FROM medications_catalog ORDER BY product_name ASC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query(
        """
        SELECT * FROM medications_catalog 
        WHERE product_name LIKE '%' || :query || '%' 
           OR strength LIKE '%' || :query || '%' 
           OR dosage_form LIKE '%' || :query || '%'
        ORDER BY price_egp ASC
        """
    )
    fun searchMedications(query: String): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications_catalog WHERE id = :id LIMIT 1")
    suspend fun getMedicationById(id: String): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(medications: List<MedicationEntity>)

    @Upsert
    suspend fun upsertAll(medications: List<MedicationEntity>)

    @Query("DELETE FROM medications_catalog")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM medications_catalog")
    suspend fun getCount(): Int
}
