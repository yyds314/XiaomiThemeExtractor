package com.globalfontmanager.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "root_operation_logs")
data class RootOperationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val time: Long,
    val command: String,
    val result: String,
    val error: String,
)
