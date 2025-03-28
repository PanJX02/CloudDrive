package com.panjx.clouddrive.data.database

import androidx.room.TypeConverter
import com.panjx.clouddrive.feature.transfersRoute.TransferStatus

class Converters {
    @TypeConverter
    fun fromTransferType(value: TransferType): String {
        return value.name
    }

    @TypeConverter
    fun toTransferType(value: String): TransferType {
        return TransferType.valueOf(value)
    }

    @TypeConverter
    fun fromTransferStatus(value: TransferStatus): String {
        return value.name
    }

    @TypeConverter
    fun toTransferStatus(value: String): TransferStatus {
        return TransferStatus.valueOf(value)
    }
} 