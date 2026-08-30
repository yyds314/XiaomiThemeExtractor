package com.globalfontmanager.service

import com.globalfontmanager.data.model.FontFile
import com.globalfontmanager.data.model.FontFormat
import com.globalfontmanager.data.parser.FontParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class FontReplacementResult(
    val success: Boolean,
    val message: String,
    val targetCount: Int = 0,
)

class FontReplacementEngine(
    private val shell: RootShellManager,
    private val backupManager: BackupManager,
    private val moduleGenerator: FontModuleGenerator,
    private val parser: FontParser,
    private val analyzer: SystemFontAnalyzer,
    private val compatibilityChecker: FontCompatibilityChecker,
) {
    var lastGeneratedModule: File? = null
        private set

    suspend fun generate(font: FontFile): FontReplacementResult = withContext(Dispatchers.IO) {
        try {
            val environment = shell.detectEnvironment()
            check(environment.isGranted) { "Root 状态：${environment.status}" }
            val source = File(font.path)
            check(source.isFile && source.length() > 0) { "字体文件不存在或为空" }
            parser.parse(source)
            val profile = analyzer.analyze()
            val targets = shell.detectSystemFonts()
            check(targets.isNotEmpty()) { "当前系统未找到支持的目标字体" }
            val preferredNames = preferredTargetNames(profile)
            val analyzedTargets = targets.filter { target -> target.fileName in preferredNames }
            val replacementTargets = (analyzedTargets.ifEmpty { targets }).filter { target ->
                target.fileName.endsWith(".ttc", ignoreCase = true) == (font.format == FontFormat.TTC)
            }
            check(replacementTargets.isNotEmpty()) { "字体格式与当前系统目标字体不匹配" }
            validateTtcCollection(source, replacementTargets, profile)
            moduleGenerator.generateRecoveryModule(backupManager.directory)
            lastGeneratedModule = moduleGenerator.generate(font, replacementTargets, profile)
            FontReplacementResult(true, "模块已生成，可通过 Root 安装", replacementTargets.size)
        } catch (error: Exception) {
            FontReplacementResult(false, error.message ?: "模块生成失败")
        }
    }

    suspend fun apply(
        font: FontFile,
        autoBackup: Boolean = true,
        restartReminder: Boolean = true,
    ): FontReplacementResult = withContext(Dispatchers.IO) {
        try {
            val environment = shell.detectEnvironment()
            check(environment.isGranted) { "Root 状态：${environment.status}" }
            val source = File(font.path)
            check(source.isFile && source.length() > 0) { "字体文件不存在或为空" }
            parser.parse(source)
            val profile = analyzer.analyze()
            val targets = shell.detectSystemFonts()
            check(targets.isNotEmpty()) { "当前系统未找到支持的目标字体" }
            val preferredNames = preferredTargetNames(profile)
            val analyzedTargets = targets.filter { target -> target.fileName in preferredNames }
            val replacementTargets = (analyzedTargets.ifEmpty { targets }).filter { target ->
                target.fileName.endsWith(".ttc", ignoreCase = true) == (font.format == FontFormat.TTC)
            }
            check(replacementTargets.isNotEmpty()) { "字体格式与当前系统目标字体不匹配" }
            validateTtcCollection(source, replacementTargets, profile)
            val compatibility = compatibilityChecker.checkFaces(source, requiredTtcIndices(profile, replacementTargets))
            check(compatibility.level != CompatibilityLevel.NOT_RECOMMENDED) { "字体缺少中文、英文或数字字符，不推荐应用" }
            check(shell.checkFreeSpace("/data", source.length() * targets.size + 2 * 1024 * 1024)) { "系统剩余空间不足" }

            if (autoBackup) backupManager.backup(targets)
            moduleGenerator.generateRecoveryModule(backupManager.directory)
            val module = moduleGenerator.generate(font, replacementTargets, profile)
            lastGeneratedModule = module
            val result = shell.installModule(module.absolutePath)
            check(result.isSuccess) { result.stderr.ifBlank { "Root 模块安装失败" } }
            check(shell.isModuleInstalled()) { "模块安装验证失败" }
            val message = if (restartReminder) {
                "模块已生成并安装，请重启系统使字体生效"
            } else {
                "模块已生成并安装，字体将在系统重新加载后生效"
            }
            FontReplacementResult(true, message, replacementTargets.size)
        } catch (error: Exception) {
            shell.disableModule()
            backupManager.restoreLatest()
            FontReplacementResult(false, "字体应用失败，已执行恢复：${error.message ?: "未知错误"}")
        }
    }

    suspend fun restore(): FontReplacementResult = withContext(Dispatchers.IO) {
        shell.emergencyRestore()
        val restored = backupManager.restoreLatest()
        if (restored) FontReplacementResult(true, "已移除字体 overlay，系统原字体已恢复")
        else FontReplacementResult(false, "没有可用备份或恢复失败")
    }

    private fun validateTtcCollection(
        source: File,
        targets: List<SystemFontTarget>,
        profile: SystemFontProfile,
    ) {
        if (source.extension.equals("ttc", true)) {
            val sourceFaceCount = parser.faceCount(source)
            val requiredIndex = targets
                .filter { it.fileName.endsWith(".ttc", true) }
                .flatMap { target -> indicesForTarget(profile, target) }
                .maxOrNull() ?: 0
            check(requiredIndex < sourceFaceCount) {
                "TTC 字体集合缺少系统所需的 face index：$requiredIndex / $sourceFaceCount"
            }
        }
    }

    private fun requiredTtcIndices(profile: SystemFontProfile, targets: List<SystemFontTarget>): Set<Int> =
        targets.filter { it.fileName.endsWith(".ttc", true) }
            .flatMap { target -> indicesForTarget(profile, target) }
            .toSet()

    private fun indicesForTarget(profile: SystemFontProfile, target: SystemFontTarget): List<Int> {
        val indices = profile.fontReferences
            .filter { reference -> reference.path.substringAfterLast('/') == target.fileName }
            .map { it.index }
        return indices.ifEmpty { listOf(target.fontIndex) }
    }

    private fun preferredTargetNames(profile: SystemFontProfile): Set<String> = sequenceOf(
        profile.defaultChineseFont,
        profile.defaultEnglishFont,
    ).filterNotNull().plus(profile.fallbackFonts.asSequence()).plus(
        profile.fontReferences.asSequence().map { it.path },
    ).map { it.substringAfterLast('/') }.filter(String::isNotBlank).toSet()
}
