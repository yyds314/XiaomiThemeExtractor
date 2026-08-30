package com.globalfontmanager.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FontModuleDao {
    @Query("SELECT * FROM font_modules ORDER BY createdTime DESC")
    fun observeAll(): Flow<List<FontModuleEntity>>

    @Insert
    suspend fun insert(module: FontModuleEntity)

    @Query("UPDATE font_modules SET installedStatus = :installed WHERE moduleId = :moduleId")
    suspend fun setInstalled(moduleId: String, installed: Boolean)

    @Query("DELETE FROM font_modules WHERE moduleId = :moduleId")
    suspend fun delete(moduleId: String)
}
