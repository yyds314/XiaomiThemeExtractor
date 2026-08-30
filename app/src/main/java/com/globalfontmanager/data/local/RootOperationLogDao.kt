package com.globalfontmanager.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RootOperationLogDao {
    @Query("SELECT * FROM root_operation_logs ORDER BY time DESC")
    fun observeAll(): Flow<List<RootOperationLogEntity>>

    @Insert
    suspend fun insert(log: RootOperationLogEntity)
}
