package com.globalfontmanager.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.globalfontmanager.data.local.FontBackupEntity
import com.globalfontmanager.data.local.FontModuleEntity
import com.globalfontmanager.data.local.RootOperationLogEntity
import com.globalfontmanager.data.model.FontFile
import com.globalfontmanager.service.FontReplacementResult
import com.globalfontmanager.service.RootEnvironment
import com.globalfontmanager.service.RootProvider
import com.globalfontmanager.service.RootStatus
import com.globalfontmanager.service.SystemFontTarget
import com.globalfontmanager.utils.ServiceLocator
import java.io.File
import com.globalfontmanager.data.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FontManagerViewModel(application: Application) : AndroidViewModel(application) {
    private val services = ServiceLocator(application)

    val fonts: StateFlow<List<FontFile>> = services.observeFonts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val backups: StateFlow<List<FontBackupEntity>> = services.backupManager.observeBackups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val modules: StateFlow<List<FontModuleEntity>> = services.moduleDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val rootLogs: StateFlow<List<RootOperationLogEntity>> = services.rootLogDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var isDarkMode by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var rootEnvironment by mutableStateOf(
        RootEnvironment(RootStatus.UNAVAILABLE, RootProvider.UNKNOWN, null, null),
    )
        private set

    var moduleInstalled by mutableStateOf(false)
        private set

    var systemFontTargets by mutableStateOf<List<SystemFontTarget>>(emptyList())
        private set

    var rootMessage by mutableStateOf<String?>(null)
        private set

    var rootBusy by mutableStateOf(false)
        private set

    var settings by mutableStateOf(services.settings.load())
        private set

    init {
        refreshFonts()
        refreshRootState()
    }

    fun refreshFonts() {
        viewModelScope.launch {
            runCatching { services.refreshFonts() }
                .onFailure { errorMessage = it.message ?: "字体扫描失败" }
        }
    }

    fun importFonts(uris: List<Uri>) {
        viewModelScope.launch {
            runCatching { services.importFonts(uris) }
                .onFailure { errorMessage = it.message ?: "字体导入失败" }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun refreshRootState() {
        viewModelScope.launch {
            rootEnvironment = services.rootEnvironmentChecker.check()
            moduleInstalled = rootEnvironment.isGranted && services.rootShell.isModuleInstalled()
            systemFontTargets = if (rootEnvironment.isGranted && settings.autoDetectFonts) {
                services.rootShell.detectSystemFonts()
            } else emptyList()
        }
    }

    fun applyFont(font: FontFile) {
        runRootOperation {
            services.replacementEngine.apply(font, settings.autoBackup, settings.restartReminder)
        }
    }

    fun generateModule(font: FontFile) {
        runRootOperation { services.replacementEngine.generate(font) }
    }

    fun exportModule(font: FontFile) {
        viewModelScope.launch {
            rootBusy = true
            rootMessage = null
            try {
                val generated = services.replacementEngine.generate(font)
                if (!generated.success) {
                    rootMessage = generated.message
                } else {
                    val directory = services.replacementEngine.lastGeneratedModule
                        ?: error("模块目录不存在")
                    val exported = services.moduleExporter.export(directory, font.displayName)
                    services.moduleDao.insert(
                        FontModuleEntity(
                            moduleId = "${exported.moduleId}.${exported.zipFile.nameWithoutExtension}",
                            moduleName = exported.moduleName,
                            version = exported.version,
                            fontName = exported.fontName,
                            createdTime = exported.zipFile.lastModified(),
                            zipPath = exported.zipFile.absolutePath,
                            installedStatus = false,
                        ),
                    )
                    rootMessage = "模块 ZIP 已导出：${exported.zipFile.name}"
                }
            } catch (error: Exception) {
                rootMessage = error.message ?: "模块导出失败"
            } finally {
                rootBusy = false
                refreshRootState()
            }
        }
    }

    fun deleteGeneratedModule(module: FontModuleEntity) {
        viewModelScope.launch {
            File(module.zipPath).delete()
            services.moduleDao.delete(module.moduleId)
            rootMessage = "已删除模块记录"
        }
    }

    fun exportCompatibilityReport(font: FontFile) {
        viewModelScope.launch {
            rootBusy = true
            try {
                val profile = services.systemFontAnalyzer.analyze()
                val compatibility = services.compatibilityChecker.check(File(font.path))
                val targets = services.rootShell.detectSystemFonts()
                val report = services.compatibilityReport.export(profile, font, compatibility, targets)
                rootMessage = "兼容性报告已导出：${report.name}"
            } catch (error: Exception) {
                rootMessage = error.message ?: "兼容性报告导出失败"
            } finally {
                rootBusy = false
            }
        }
    }

    fun exportDiagnosticReport() {
        viewModelScope.launch {
            rootBusy = true
            try {
                val profile = runCatching { services.systemFontAnalyzer.analyze() }.getOrNull()
                val report = services.diagnosticReportExporter.export(
                    environment = rootEnvironment,
                    profile = profile,
                    moduleInstalled = moduleInstalled,
                    logs = rootLogs.value,
                )
                rootMessage = "诊断报告已导出：${report.name}"
            } catch (error: Exception) {
                rootMessage = error.message ?: "诊断报告导出失败"
            } finally {
                rootBusy = false
            }
        }
    }

    fun setAutoBackup(value: Boolean) = updateSettings("auto_backup", value) { copy(autoBackup = value) }
    fun setAutoDetectFonts(value: Boolean) {
        updateSettings("auto_detect_fonts", value) { copy(autoDetectFonts = value) }
        refreshRootState()
    }
    fun setRestartReminder(value: Boolean) = updateSettings("restart_reminder", value) { copy(restartReminder = value) }
    fun setSaveLogs(value: Boolean) {
        services.settings.setBoolean("save_logs", value)
        services.rootLogger.enabled = value
        settings = settings.copy(saveLogs = value)
    }

    fun setModuleSavePath(value: String) {
        services.settings.setModuleSavePath(value)
        val normalized = services.settings.load().moduleSavePath
        services.updateModuleSavePath(normalized)
        settings = settings.copy(moduleSavePath = normalized)
    }

    private fun updateSettings(key: String, value: Boolean, update: AppSettings.() -> AppSettings) {
        services.settings.setBoolean(key, value)
        settings = settings.update()
    }

    fun restoreFonts() {
        runRootOperation { services.replacementEngine.restore() }
    }

    fun deleteModule() {
        runRootOperation {
            val result = services.rootShell.removeModule()
            FontReplacementResult(result.isSuccess, if (result.isSuccess) "模块已删除" else result.stderr)
        }
    }

    fun deleteBackup(backupId: String) {
        viewModelScope.launch {
            rootBusy = true
            rootMessage = null
            try {
                val deleted = services.backupManager.delete(backupId)
                rootMessage = if (deleted) "备份已删除" else "备份删除失败"
            } catch (error: Exception) {
                rootMessage = error.message ?: "备份删除失败"
            } finally {
                rootBusy = false
            }
        }
    }

    private fun runRootOperation(operation: suspend () -> FontReplacementResult) {
        viewModelScope.launch {
            rootBusy = true
            rootMessage = null
            try {
                val result = operation()
                rootMessage = result.message
                moduleInstalled = result.success && services.rootShell.isModuleInstalled()
            } catch (error: Exception) {
                rootMessage = error.message ?: "Root 操作失败"
            } finally {
                rootBusy = false
                refreshRootState()
            }
        }
    }

    fun clearRootMessage() {
        rootMessage = null
    }

    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }
}
