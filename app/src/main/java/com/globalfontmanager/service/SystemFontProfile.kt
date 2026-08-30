package com.globalfontmanager.service

data class FontMapping(
    val family: String,
    val alias: String? = null,
    val fallback: Boolean = false,
    val fontFiles: List<String> = emptyList(),
    val fontIndices: Map<String, List<Int>> = emptyMap(),
)

data class FontReference(val path: String, val index: Int = 0)

enum class RomFlavor {
    MIUI,
    HYPEROS,
    OTHER,
}

data class SystemFontProfile(
    val systemVersion: String,
    val miuiVersion: String?,
    val hyperOsVersion: String?,
    val fontPaths: List<String>,
    val defaultChineseFont: String?,
    val defaultEnglishFont: String?,
    val fallbackFonts: List<String>,
    val mappings: List<FontMapping>,
    val configurationFiles: Map<String, String>,
    val romFlavor: RomFlavor,
    val fontReferences: List<FontReference> = emptyList(),
)
