package com.pharmachain.ai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room Database setup for PharmaChain catalog persistence.
 */
@Database(
    entities = [MedicationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PharmaChainDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao

    companion object {
        @Volatile
        private var INSTANCE: PharmaChainDatabase? = null

        fun getInstance(context: Context): PharmaChainDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PharmaChainDatabase::class.java,
                    "pharmachain_catalog.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
