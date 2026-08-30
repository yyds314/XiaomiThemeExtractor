package com.globalfontmanager.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FontBackupDao {
    @Query("SELECT * FROM font_backups ORDER BY createTime DESC")
    fun observeAll(): Flow<List<FontBackupEntity>>

    @Query("SELECT * FROM font_backups ORDER BY createTime DESC")
    suspend fun getAll(): List<FontBackupEntity>

    @Insert
    suspend fun insert(backup: FontBackupEntity)

    @Query("DELETE FROM font_backups WHERE id = :id")
    suspend fun delete(id: String)
}
