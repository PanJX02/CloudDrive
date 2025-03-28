package com.panjx.clouddrive.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfers WHERE type = :type ORDER BY createdAt DESC")
    fun getTransfersByType(type: TransferType): Flow<List<TransferEntity>>
    
    @Query("SELECT * FROM transfers WHERE status = :status")
    fun getTransfersByStatus(status: String): Flow<List<TransferEntity>>
    
    @Query("SELECT * FROM transfers WHERE type = :type AND status = :status ORDER BY createdAt DESC")
    fun getTransfersByTypeAndStatus(type: TransferType, status: String): Flow<List<TransferEntity>>
    
    @Query("SELECT * FROM transfers WHERE type = :type AND status != :status ORDER BY createdAt DESC")
    fun getTransfersByTypeExcludeStatus(type: TransferType, status: String): Flow<List<TransferEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferEntity): Long
    
    @Update
    suspend fun updateTransfer(transfer: TransferEntity)
    
    @Delete
    suspend fun deleteTransfer(transfer: TransferEntity)
    
    @Query("DELETE FROM transfers WHERE status = :status")
    suspend fun deleteTransfersByStatus(status: String)
    
    @Query("DELETE FROM transfers WHERE id = :id")
    suspend fun deleteTransferById(id: Long)
} 