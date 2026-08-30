package com.globalfontmanager.service

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.File

data class ExportedModule(
    val moduleId: String,
    val moduleName: String,
    val version: String,
    val fontName: String,
    val zipFile: File,
)

class FontModuleExporter(private val outputDirectory: File) {
    fun export(moduleDirectory: File, fontName: String): ExportedModule {
        require(moduleDirectory.isDirectory) { "模块目录不存在" }
        REQUIRED_FILES.forEach { relative ->
            require(moduleDirectory.resolve(relative).exists()) { "模块缺少 $relative" }
        }
        val moduleId = moduleDirectory.name
        val moduleProp = moduleDirectory.resolve("module.prop").readText()
        val version = property(moduleProp, "version")
        val moduleName = property(moduleProp, "name")
        outputDirectory.mkdirs()
        val safeFontName = fontName.replace(Regex("[^A-Za-z0-9._-]"), "_").trim('_').ifBlank { "font" }
        val zip = outputDirectory.resolve("GlobalFont_${safeFontName}_${System.currentTimeMillis()}.zip")
        ZipArchiveOutputStream(zip).use { output ->
            moduleDirectory.walkTopDown().forEach { file ->
                val entryName = moduleDirectory.parentFile
                    .toPath()
                    .relativize(file.toPath())
                    .toString()
                    .replace(File.separatorChar, '/')
                    .let { if (file.isDirectory) "$it/" else it }
                val entry = ZipArchiveEntry(entryName)
                entry.unixMode = when {
                    file.isDirectory -> 0b111101101
                    file.name.endsWith(".sh") -> 0b111101101
                    else -> 0b110100100
                }
                output.putArchiveEntry(entry)
                if (file.isFile) file.inputStream().use { it.copyTo(output) }
                output.closeArchiveEntry()
            }
            output.finish()
        }
        validate(zip, moduleId, moduleName, version)
        return ExportedModule(moduleId, moduleName, version, fontName, zip)
    }

    private fun validate(zip: File, moduleId: String, moduleName: String, version: String) {
        require(zip.isFile && zip.length() > 0) { "ZIP 文件生成失败" }
        ZipFile(zip).use { archive ->
            val names = archive.entries().asSequence().map { it.name }.toSet()
            REQUIRED_FILES.forEach { relative ->
                require(names.contains("$moduleId/$relative")) { "ZIP 缺少 $relative" }
            }
            val prop = archive.getInputStream(archive.getEntry("$moduleId/module.prop"))
                .bufferedReader().use { it.readText() }
            require(property(prop, "id") == moduleId) { "module.prop id 无效" }
            require(property(prop, "name") == moduleName) { "module.prop name 无效" }
            require(property(prop, "version") == version) { "module.prop version 无效" }
            SCRIPT_FILES.forEach { relative ->
                val mode = archive.getEntry("$moduleId/$relative").unixMode and 0x1ff
                require(mode == 0x1ed) { "$relative 缺少 755 执行权限" }
            }
            archive.entries.asSequence()
                .filter { it.name.startsWith("$moduleId/system/") && it.name.matches(Regex(".*\\.(ttf|otf|ttc)$")) }
                .forEach { entry -> require((entry.unixMode and 0x1ff) == 0x1a4) { "字体文件权限无效" } }
        }
    }

    private fun property(content: String, key: String): String = content.lineSequence()
        .firstOrNull { it.startsWith("$key=") }
        ?.substringAfter('=')
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: error("module.prop 缺少 $key")

    companion object {
        val REQUIRED_FILES = listOf(
            "module.prop",
            "system",
            "system/fonts",
            "post-fs-data.sh",
            "service.sh",
            "customize.sh",
            "uninstall.sh",
        )
        val SCRIPT_FILES = listOf("post-fs-data.sh", "service.sh", "customize.sh", "uninstall.sh")
    }
}
