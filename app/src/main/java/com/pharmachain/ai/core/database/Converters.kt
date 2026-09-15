package com.pharmachain.ai.core.database

import androidx.room.TypeConverter
import com.pharmachain.ai.core.model.ApiAuthType
import com.pharmachain.ai.core.model.ApiSyncFrequency
import com.pharmachain.ai.core.model.ApiSyncStatus
import com.pharmachain.ai.core.model.CommissionStatus
import com.pharmachain.ai.core.model.ForecastDuration
import com.pharmachain.ai.core.model.InventoryMappingField
import com.pharmachain.ai.core.model.InventoryUploadStatus
import com.pharmachain.ai.core.model.JobStatus
import com.pharmachain.ai.core.model.ListingSourceType
import com.pharmachain.ai.core.model.MappingField
import com.pharmachain.ai.core.model.OrderStatus
import com.pharmachain.ai.core.model.Role
import com.pharmachain.ai.core.model.UploadStatus

class Converters {
    @TypeConverter
    fun fromRole(role: Role?): String? = role?.name

    @TypeConverter
    fun toRole(name: String?): Role? = name?.let { Role.valueOf(it) }

    @TypeConverter
    fun fromOrderStatus(status: OrderStatus?): String? = status?.name

    @TypeConverter
    fun toOrderStatus(name: String?): OrderStatus? = name?.let { OrderStatus.valueOf(it) }

    @TypeConverter
    fun fromCommissionStatus(status: CommissionStatus?): String? = status?.name

    @TypeConverter
    fun toCommissionStatus(name: String?): CommissionStatus? = name?.let { CommissionStatus.valueOf(it) }

    @TypeConverter
    fun fromUploadStatus(status: UploadStatus?): String? = status?.name

    @TypeConverter
    fun toUploadStatus(name: String?): UploadStatus? = name?.let { UploadStatus.valueOf(it) }

    @TypeConverter
    fun fromMappingField(field: MappingField?): String? = field?.name

    @TypeConverter
    fun toMappingField(name: String?): MappingField? = name?.let { MappingField.valueOf(it) }

    @TypeConverter
    fun fromForecastDuration(duration: ForecastDuration?): String? = duration?.name

    @TypeConverter
    fun toForecastDuration(name: String?): ForecastDuration? = name?.let { ForecastDuration.valueOf(it) }

    @TypeConverter
    fun fromJobStatus(status: JobStatus?): String? = status?.name

    @TypeConverter
    fun toJobStatus(name: String?): JobStatus? = name?.let { JobStatus.valueOf(it) }

    @TypeConverter
    fun fromInventoryUploadStatus(status: InventoryUploadStatus?): String? = status?.name

    @TypeConverter
    fun toInventoryUploadStatus(name: String?): InventoryUploadStatus? = name?.let { InventoryUploadStatus.valueOf(it) }

    @TypeConverter
    fun fromInventoryMappingField(field: InventoryMappingField?): String? = field?.name

    @TypeConverter
    fun toInventoryMappingField(name: String?): InventoryMappingField? = name?.let { InventoryMappingField.valueOf(it) }

    @TypeConverter
    fun fromApiAuthType(type: ApiAuthType?): String? = type?.name

    @TypeConverter
    fun toApiAuthType(name: String?): ApiAuthType? = name?.let { ApiAuthType.valueOf(it) }

    @TypeConverter
    fun fromApiSyncFrequency(freq: ApiSyncFrequency?): String? = freq?.name

    @TypeConverter
    fun toApiSyncFrequency(name: String?): ApiSyncFrequency? = name?.let { ApiSyncFrequency.valueOf(it) }

    @TypeConverter
    fun fromApiSyncStatus(status: ApiSyncStatus?): String? = status?.name

    @TypeConverter
    fun toApiSyncStatus(name: String?): ApiSyncStatus? = name?.let { ApiSyncStatus.valueOf(it) }

    @TypeConverter
    fun fromListingSourceType(source: ListingSourceType?): String? = source?.name

    @TypeConverter
    fun toListingSourceType(name: String?): ListingSourceType? = name?.let { ListingSourceType.valueOf(it) }

    @TypeConverter
    fun fromChatMessageRole(role: com.pharmachain.ai.core.database.entity.ChatMessageRole?): String? = role?.name

    @TypeConverter
    fun toChatMessageRole(name: String?): com.pharmachain.ai.core.database.entity.ChatMessageRole? = name?.let { com.pharmachain.ai.core.database.entity.ChatMessageRole.valueOf(it) }
}
