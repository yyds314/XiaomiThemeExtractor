package com.globalfontmanager.service

import com.globalfontmanager.data.model.FontFile
import java.io.File

class DeviceCompatibilityReport(private val outputDirectory: File) {
    fun export(
        profile: SystemFontProfile,
        font: FontFile,
        compatibility: FontCompatibility,
        targets: List<SystemFontTarget>,
        failureReason: String? = null,
    ): File {
        outputDirectory.mkdirs()
        val report = outputDirectory.resolve("compatibility-${System.currentTimeMillis()}.txt")
        report.writeText(
            buildString {
                appendLine("Global Font Manager Device Compatibility Report")
                appendLine("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                appendLine("Android: ${profile.systemVersion}")
                appendLine("MIUI: ${profile.miuiVersion ?: "未检测到"}")
                appendLine("HyperOS: ${profile.hyperOsVersion ?: "未检测到"}")
                appendLine("ROM flavor: ${profile.romFlavor}")
                appendLine("Font: ${font.displayName}")
                appendLine("Compatibility: ${compatibility.level}")
                appendLine("Chinese: ${compatibility.containsChinese}")
                appendLine("English: ${compatibility.containsEnglish}")
                appendLine("Digits: ${compatibility.containsDigits}")
                appendLine("Missing: ${compatibility.missingCharacters.joinToString()}")
                appendLine("Detected paths:")
                targets.forEach { appendLine("  ${it.path}") }
                appendLine("Default Chinese: ${profile.defaultChineseFont ?: "未检测到"}")
                appendLine("Default English: ${profile.defaultEnglishFont ?: "未检测到"}")
                appendLine("Fallback: ${profile.fallbackFonts.joinToString()}")
                appendLine("Configuration files: ${profile.configurationFiles.keys.joinToString()}")
                appendLine("Mappings: ${profile.mappings.size}")
                appendLine("TTC references: ${profile.fontReferences.count { it.path.endsWith(".ttc", true) }}")
                appendLine("Replacement strategy: ${targets.joinToString { it.path }}")
                appendLine("Failure: ${failureReason ?: "无"}")
            },
        )
        return report
    }
}
