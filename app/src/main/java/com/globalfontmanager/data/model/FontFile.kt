package com.globalfontmanager.data.model

data class FontFile(
    val id: String,
    val displayName: String,
    val familyName: String,
    val author: String,
    val version: String,
    val fileName: String,
    val path: String,
    val fileSizeBytes: Long,
    val createTime: Long,
    val format: FontFormat,
    val supportedLanguages: List<String>,
    val isApplied: Boolean,
)

enum class FontFormat {
    TTF,
    OTF,
    TTC,
}
