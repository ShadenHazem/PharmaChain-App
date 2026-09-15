package com.pharmachain.ai.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Room entity representing a medication row matching Supabase PostgreSQL schema.
 */
@JsonClass(generateAdapter = true)
@Entity(
    tableName = "medications_catalog",
    indices = [
        Index(value = ["product_name"]),
        Index(value = ["price_egp"])
    ]
)
data class MedicationEntity(
    @PrimaryKey
    @Json(name = "id")
    @ColumnInfo(name = "id")
    val id: String,

    @Json(name = "product_name")
    @ColumnInfo(name = "product_name")
    val productName: String,

    @Json(name = "strength")
    @ColumnInfo(name = "strength")
    val strength: String,

    @Json(name = "dosage_form")
    @ColumnInfo(name = "dosage_form")
    val dosageForm: String,

    @Json(name = "price_egp")
    @ColumnInfo(name = "price_egp")
    val priceEgp: Double
)
