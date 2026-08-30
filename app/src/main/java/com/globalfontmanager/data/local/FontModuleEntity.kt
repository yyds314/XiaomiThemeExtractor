package com.globalfontmanager.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "font_modules")
data class FontModuleEntity(
    @PrimaryKey val moduleId: String,
    val moduleName: String,
    val version: String,
    val fontName: String,
    val createdTime: Long,
    val zipPath: String,
    val installedStatus: Boolean,
)
