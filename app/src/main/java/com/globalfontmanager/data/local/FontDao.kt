package com.globalfontmanager.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FontDao {
    @Query("SELECT * FROM fonts ORDER BY createTime DESC")
    fun observeAll(): Flow<List<FontEntity>>

    @Query("SELECT * FROM fonts WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): FontEntity?

    @Upsert
    suspend fun upsert(font: FontEntity)
}
