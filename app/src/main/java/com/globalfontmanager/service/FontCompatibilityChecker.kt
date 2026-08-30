package com.globalfontmanager.service

import android.graphics.Paint
import android.graphics.Typeface
import java.io.File

enum class CompatibilityLevel {
    FULL,
    PARTIAL,
    NOT_RECOMMENDED,
}

data class FontCompatibility(
    val level: CompatibilityLevel,
    val containsChinese: Boolean,
    val containsEnglish: Boolean,
    val containsDigits: Boolean,
    val missingCharacters: List<String>,
)

class FontCompatibilityChecker {
    fun check(file: File, ttcIndex: Int = 0): FontCompatibility {
        val typeface = Typeface.Builder(file).setTtcIndex(ttcIndex).build()
        val paint = Paint().apply { this.typeface = typeface }
        val samples = mapOf(
            "中文" to "你好世界小米科技",
            "英文" to "HelloAndroid",
            "数字" to "0123456789",
        )
        val missing = samples.values.flatMap { text -> text.filterNot { paint.hasGlyph(it.toString()) }.map(Char::toString) }.distinct()
        val chinese = samples.getValue("中文").all { paint.hasGlyph(it.toString()) }
        val english = samples.getValue("英文").all { paint.hasGlyph(it.toString()) }
        val digits = samples.getValue("数字").all { paint.hasGlyph(it.toString()) }
        val level = when {
            chinese && english && digits && missing.isEmpty() -> CompatibilityLevel.FULL
            english && digits -> CompatibilityLevel.PARTIAL
            else -> CompatibilityLevel.NOT_RECOMMENDED
        }
        return FontCompatibility(level, chinese, english, digits, missing)
    }

    fun checkFaces(file: File, indices: Set<Int>): FontCompatibility {
        val results = indices.ifEmpty { setOf(0) }.map { check(file, it) }
        return FontCompatibility(
            level = when {
                results.all { it.level == CompatibilityLevel.FULL } -> CompatibilityLevel.FULL
                results.all { it.level != CompatibilityLevel.NOT_RECOMMENDED } -> CompatibilityLevel.PARTIAL
                else -> CompatibilityLevel.NOT_RECOMMENDED
            },
            containsChinese = results.all { it.containsChinese },
            containsEnglish = results.all { it.containsEnglish },
            containsDigits = results.all { it.containsDigits },
            missingCharacters = results.flatMap { it.missingCharacters }.distinct(),
        )
    }
}
