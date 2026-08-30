package com.globalfontmanager.service

import android.os.Build
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

class SystemFontAnalyzer(private val shell: RootShellManager) {
    suspend fun analyze(): SystemFontProfile {
        val properties = mapOf(
            "miui" to shell.readProperty("ro.miui.ui.version.name"),
            "hyper" to shell.readProperty("ro.mi.os.version.name"),
            "incremental" to shell.readProperty("ro.build.version.incremental"),
        )
        val xmlPaths = CONFIG_PATHS.filter { shell.fileExists(it) }
        val mappings = xmlPaths.flatMap { parseFontXml(shell.readFile(it)) }
        val references = mappings.flatMap { mapping ->
            mapping.fontFiles.flatMap { file ->
                mapping.fontIndices[file].orEmpty().ifEmpty { listOf(0) }.map { index ->
                    FontReference(file, index)
                }
            }
        }.distinct()
        val fontPaths = FONT_ROOTS.flatMap { root -> shell.listFiles(root) }
            .filter { path -> path.substringAfterLast('/').substringAfterLast('.').lowercase() in SUPPORTED_EXTENSIONS }
            .distinct()
        val chinese = mappings.firstOrNull { mapping ->
            mapping.family.contains("zh", true) || mapping.family.contains("cjk", true) ||
                mapping.family.contains("han", true) || mapping.family.contains("chinese", true)
        }
        val english = mappings.firstOrNull { it.family.equals("sans-serif", true) || it.family.equals("serif", true) }
        val fallback = mappings.filter { it.fallback }.flatMap { it.fontFiles }.distinct()
        return SystemFontProfile(
            systemVersion = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}) build ${properties["incremental"]}",
            miuiVersion = properties["miui"]?.takeIf(String::isNotBlank),
            hyperOsVersion = properties["hyper"]?.takeIf(String::isNotBlank),
            fontPaths = fontPaths,
            defaultChineseFont = chinese?.fontFiles?.firstOrNull(),
            defaultEnglishFont = english?.fontFiles?.firstOrNull(),
            fallbackFonts = fallback,
            mappings = mappings,
            configurationFiles = xmlPaths.associateWith { shell.readFile(it) },
            romFlavor = when {
                properties["hyper"].orEmpty().isNotBlank() -> RomFlavor.HYPEROS
                properties["miui"].orEmpty().isNotBlank() -> RomFlavor.MIUI
                else -> RomFlavor.OTHER
            },
            fontReferences = references,
        )
    }

    private fun parseFontXml(xml: String): List<FontMapping> {
        if (xml.isBlank()) return emptyList()
        val parser = Xml.newPullParser().apply { setInput(StringReader(xml)) }
        val mappings = mutableListOf<FontMapping>()
        var event = parser.eventType
        var family: String? = null
        var familyFiles = mutableListOf<String>()
        var familyIndices = mutableMapOf<String, MutableList<Int>>()
        var fallback = false
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                "family" -> {
                        val namedFamily = parser.getAttributeValue(null, "name")
                        family = namedFamily ?: parser.getAttributeValue(null, "lang") ?: "fallback"
                        fallback = namedFamily == null || parser.getAttributeValue(null, "variant") == "elegant"
                        familyFiles = mutableListOf()
                        familyIndices = mutableMapOf()
                    }
                    "font" -> {
                        val index = parser.getAttributeValue(null, "index")?.toIntOrNull() ?: 0
                        val value = parser.nextText().trim()
                        if (value.isNotBlank()) {
                            familyFiles += value
                            familyIndices.getOrPut(value) { mutableListOf() } += index
                        }
                    }
                    "alias" -> mappings += FontMapping(
                        family = parser.getAttributeValue(null, "to").orEmpty(),
                        alias = parser.getAttributeValue(null, "name"),
                    )
                }
                XmlPullParser.END_TAG -> if (parser.name == "family" && family != null) {
                    mappings += FontMapping(
                        family.orEmpty(),
                        fallback = fallback,
                        fontFiles = familyFiles.toList(),
                        fontIndices = familyIndices.mapValues { it.value.toList() },
                    )
                    family = null
                }
            }
            event = parser.next()
        }
        return mappings
    }

    companion object {
        val FONT_ROOTS = listOf("/system/fonts", "/product/fonts", "/system_ext/fonts", "/vendor/fonts")
        val CONFIG_PATHS = listOf(
            "/product/etc/fonts.xml",
            "/system_ext/etc/fonts.xml",
            "/system/etc/fonts.xml",
            "/product/etc/font_fallback.xml",
            "/system_ext/etc/font_fallback.xml",
            "/system/etc/font_fallback.xml",
        )
        private val SUPPORTED_EXTENSIONS = setOf("ttf", "otf", "ttc")
    }
}
