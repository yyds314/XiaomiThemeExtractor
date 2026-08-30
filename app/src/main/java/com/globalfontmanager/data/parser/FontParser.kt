package com.globalfontmanager.data.parser

import com.globalfontmanager.data.model.FontFormat
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FontParser {
    fun parse(file: File): FontMetadata {
        val bytes = file.readBytes()
        require(bytes.size >= 12) { "字体文件过小或已损坏" }
        val header = ascii(bytes, 0, 4)
        val offsets = if (header == "ttcf") collectionOffsets(bytes) else listOf(0)
        val offset = offsets.first()
        require(isSfnt(ascii(bytes, offset, 4))) { "不支持的字体文件格式" }

        val tables = readTables(bytes, offset)
        val names = readNames(bytes, tables["name"] ?: error("字体缺少 name 表"))
        val extension = file.extension.lowercase()
        val format = when (extension) {
            "ttf" -> FontFormat.TTF
            "otf" -> FontFormat.OTF
            "ttc" -> FontFormat.TTC
            else -> error("不支持的字体扩展名")
        }
        val family = names[1].orEmpty().ifBlank { names[4].orEmpty() }
        return FontMetadata(
            name = names[4].orEmpty().ifBlank { family }.ifBlank { file.nameWithoutExtension },
            familyName = family.ifBlank { file.nameWithoutExtension },
            author = names[9].orEmpty().ifBlank { "未知作者" },
            version = names[5].orEmpty().ifBlank { "未知版本" },
            format = format,
            supportedLanguages = readLanguages(bytes, tables["OS/2"]),
            faces = offsets.mapIndexed { index, faceOffset ->
                val faceTables = readTables(bytes, faceOffset)
                val faceNames = faceTables["name"]?.let { readNames(bytes, it) }.orEmpty()
                FontFaceMetadata(
                    index = index,
                    familyName = faceNames[1].orEmpty().ifBlank { faceNames[4].orEmpty() },
                    fullName = faceNames[4].orEmpty(),
                )
            },
        )
    }

    fun faceCount(file: File): Int = parse(file).faces.size

    private fun readTables(bytes: ByteArray, fontOffset: Int): Map<String, Table> {
        val count = u16(bytes, fontOffset + 4)
        val result = mutableMapOf<String, Table>()
        repeat(count) { index ->
            val position = fontOffset + 12 + index * 16
            val tag = ascii(bytes, position, 4)
            result[tag] = Table(u32(bytes, position + 8), u32(bytes, position + 12))
        }
        return result
    }

    private fun readNames(bytes: ByteArray, table: Table): Map<Int, String> {
        val start = table.offset.toInt()
        val count = u16(bytes, start + 2)
        val storage = start + u16(bytes, start + 4)
        val result = mutableMapOf<Int, String>()
        repeat(count) { index ->
            val position = start + 6 + index * 12
            val platform = u16(bytes, position)
            val nameId = u16(bytes, position + 6)
            val length = u16(bytes, position + 8)
            val valueOffset = u16(bytes, position + 10)
            val value = decodeName(bytes, storage + valueOffset, length, platform)
            if (value.isNotBlank() && (nameId !in result || platform == 3)) result[nameId] = value
        }
        return result
    }

    private fun readLanguages(bytes: ByteArray, table: Table?): List<String> {
        if (table == null || table.length < 58) return listOf("未声明")
        val start = table.offset.toInt()
        val unicodeRange1 = u32(bytes, start + 42)
        val unicodeRange2 = u32(bytes, start + 46)
        val unicodeRange3 = u32(bytes, start + 50)
        val unicodeRange4 = u32(bytes, start + 54)
        val languages = mutableListOf<String>()
        if (unicodeRange1 and (1L shl 0) != 0L) languages += "Basic Latin"
        if (unicodeRange1 and (1L shl 17) != 0L) languages += "Latin"
        if (unicodeRange1 and (1L shl 18) != 0L) languages += "Greek"
        if (unicodeRange1 and (1L shl 20) != 0L) languages += "Cyrillic"
        if ((unicodeRange2 and (0xffL shl 16)) != 0L) languages += "CJK"
        if (unicodeRange3 != 0L || unicodeRange4 != 0L) languages += "Extended Unicode"
        return languages.ifEmpty { listOf("未声明") }
    }

    private fun decodeName(bytes: ByteArray, offset: Int, length: Int, platform: Int): String {
        if (offset < 0 || offset + length > bytes.size) return ""
        val charset = if (platform == 0 || platform == 3) Charsets.UTF_16BE else Charsets.ISO_8859_1
        return bytes.copyOfRange(offset, offset + length).toString(charset).trim()
    }

    private fun collectionOffsets(bytes: ByteArray): List<Int> {
        require(bytes.size >= 16) { "TTC 文件缺少集合头" }
        val count = u32(bytes, 8).toInt()
        require(count in 1..4096 && 12 + count * 4 <= bytes.size) { "TTC 字体数量无效" }
        return (0 until count).map { u32(bytes, 12 + it * 4).toInt() }
    }

    private fun isSfnt(header: String): Boolean = header == "\u0000\u0001\u0000\u0000" || header == "OTTO" || header == "true" || header == "typ1"

    private fun ascii(bytes: ByteArray, offset: Int, length: Int): String =
        bytes.copyOfRange(offset, offset + length).toString(Charsets.ISO_8859_1)

    private fun u16(bytes: ByteArray, offset: Int): Int =
        ByteBuffer.wrap(bytes, offset, 2).order(ByteOrder.BIG_ENDIAN).short.toInt() and 0xffff

    private fun u32(bytes: ByteArray, offset: Int): Long =
        ByteBuffer.wrap(bytes, offset, 4).order(ByteOrder.BIG_ENDIAN).int.toLong() and 0xffffffffL

    private data class Table(val offset: Long, val length: Long)
}
