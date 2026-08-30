package com.globalfontmanager.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fonts")
data class FontEntity(
    @PrimaryKey val id: String,
    val name: String,
    val familyName: String,
    val author: String,
    val version: String,
    val path: String,
    val type: String,
    val size: Long,
    val createTime: Long,
    val supportedLanguages: String,
    val isApplied: Boolean,
)
