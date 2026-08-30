package com.globalfontmanager.data.local

import com.globalfontmanager.data.model.FontFile

fun FontEntity.toDomain(): FontFile = FontFile(
    id = id,
    displayName = name,
    familyName = familyName,
    author = author,
    version = version,
    fileName = path.substringAfterLast('/'),
    path = path,
    fileSizeBytes = size,
    createTime = createTime,
    format = when (type) {
        "OTF" -> com.globalfontmanager.data.model.FontFormat.OTF
        "TTC" -> com.globalfontmanager.data.model.FontFormat.TTC
        else -> com.globalfontmanager.data.model.FontFormat.TTF
    },
    supportedLanguages = supportedLanguages.split('|').filter(String::isNotBlank),
    isApplied = isApplied,
)
