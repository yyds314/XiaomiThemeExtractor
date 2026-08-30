package com.globalfontmanager.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "font_backups")
data class FontBackupEntity(
    @PrimaryKey val id: String,
    val fontName: String,
    val originalPath: String,
    val backupPath: String,
    val createTime: Long,
    val deviceInfo: String,
)
