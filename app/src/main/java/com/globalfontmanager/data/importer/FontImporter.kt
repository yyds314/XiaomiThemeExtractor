package com.globalfontmanager.data.importer

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.globalfontmanager.data.local.FontEntity
import com.globalfontmanager.data.parser.FontParser
import java.io.File
import java.util.UUID

class FontImporter(
    private val contentResolver: ContentResolver,
    private val fontsDirectory: File,
    private val parser: FontParser,
) {
    suspend fun importFont(uri: Uri): FontEntity {
        val sourceName = queryDisplayName(uri) ?: "font-${UUID.randomUUID()}"
        val extension = sourceName.substringAfterLast('.', "").lowercase()
        require(extension in SUPPORTED_EXTENSIONS) { "仅支持 TTF、OTF 和 TTC 字体" }
        fontsDirectory.mkdirs()
        val id = UUID.randomUUID().toString()
        val target = File(fontsDirectory, "$id.$extension")
        return try {
            contentResolver.openInputStream(uri).use { input ->
                requireNotNull(input) { "无法读取选择的字体文件" }
                target.outputStream().use { output -> input.copyTo(output) }
            }
            toEntity(id, target, System.currentTimeMillis())
        } catch (error: Exception) {
            target.delete()
            throw IllegalArgumentException("字体解析失败：${error.message ?: "文件可能已损坏"}", error)
        }
    }

    private fun toEntity(id: String, file: File, createTime: Long): FontEntity {
        val metadata = parser.parse(file)
        return FontEntity(
            id = id,
            name = metadata.name,
            familyName = metadata.familyName,
            author = metadata.author,
            version = metadata.version,
            path = file.absolutePath,
            type = metadata.format.name,
            size = file.length(),
            createTime = createTime,
            supportedLanguages = metadata.supportedLanguages.joinToString("|").ifBlank { "未声明" },
            isApplied = false,
        )
    }

    private fun queryDisplayName(uri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            cursor = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            if (cursor?.moveToFirst() == true) cursor?.getString(0) else null
        } finally {
            cursor?.close()
        }
    }

    companion object {
        val SUPPORTED_EXTENSIONS = setOf("ttf", "otf", "ttc")
    }
}
