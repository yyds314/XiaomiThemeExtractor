package com.globalfontmanager.data.parser

import com.globalfontmanager.data.model.FontFormat

data class FontMetadata(
    val name: String,
    val familyName: String,
    val author: String,
    val version: String,
    val format: FontFormat,
    val supportedLanguages: List<String>,
    val faces: List<FontFaceMetadata> = emptyList(),
)

data class FontFaceMetadata(
    val index: Int,
    val familyName: String,
    val fullName: String,
)
