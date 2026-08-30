package com.globalfontmanager.data.source

import com.globalfontmanager.data.local.FontEntity
import com.globalfontmanager.data.parser.FontParser
import java.io.File

class FontFileScanner(
    private val fontsDirectory: File,
    private val parser: FontParser,
) {
    fun scan(): List<FontEntity> = fontsDirectory.listFiles()
        ?.filter { it.isFile && it.extension.lowercase() in SUPPORTED_EXTENSIONS }
        ?.mapNotNull { file ->
            runCatching {
                val metadata = parser.parse(file)
                FontEntity(
                    id = file.nameWithoutExtension,
                    name = metadata.name,
                    familyName = metadata.familyName,
                    author = metadata.author,
                    version = metadata.version,
                    path = file.absolutePath,
                    type = metadata.format.name,
                    size = file.length(),
                    createTime = file.lastModified(),
                    supportedLanguages = metadata.supportedLanguages.joinToString("|"),
                    isApplied = false,
                )
            }.getOrNull()
        }
        .orEmpty()

    companion object {
        private val SUPPORTED_EXTENSIONS = setOf("ttf", "otf", "ttc")
    }
}
