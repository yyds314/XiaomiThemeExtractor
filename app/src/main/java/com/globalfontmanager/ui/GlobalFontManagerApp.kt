package com.globalfontmanager.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import com.globalfontmanager.ui.screen.FontLibraryScreen
import com.globalfontmanager.ui.screen.FontDetailsScreen
import com.globalfontmanager.ui.screen.HomeScreen
import com.globalfontmanager.ui.screen.SettingsScreen
import com.globalfontmanager.ui.screen.RootManagementScreen
import com.globalfontmanager.ui.screen.RootLogScreen
import com.globalfontmanager.ui.screen.ReleaseReadinessScreen
import com.globalfontmanager.service.ModuleInstaller
import com.globalfontmanager.ui.viewmodel.FontManagerViewModel

private enum class AppDestination(val label: String) {
    HOME("首页"),
    LIBRARY("字体库"),
    ROOT("Root"),
    SETTINGS("设置"),
}

@Composable
fun GlobalFontManagerApp(viewModel: FontManagerViewModel) {
    var destination by rememberSaveable { mutableStateOf(AppDestination.HOME) }
    var selectedFontId by rememberSaveable { mutableStateOf<String?>(null) }
    var showRootLogs by rememberSaveable { mutableStateOf(false) }
    var showReleaseCheck by rememberSaveable { mutableStateOf(false) }
    val fonts by viewModel.fonts.collectAsStateWithLifecycle()
    val backups by viewModel.backups.collectAsStateWithLifecycle()
    val modules by viewModel.modules.collectAsStateWithLifecycle()
    val rootLogs by viewModel.rootLogs.collectAsStateWithLifecycle()
    val selectedFont = fonts.firstOrNull { it.id == selectedFontId }
    val context = LocalContext.current
    val moduleInstaller = ModuleInstaller(context)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = viewModel::importFonts,
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppDestination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item; showRootLogs = false },
                        icon = {
                            Icon(
                                imageVector = when (item) {
                                    AppDestination.HOME -> Icons.Outlined.Home
                                    AppDestination.LIBRARY -> Icons.Outlined.FontDownload
                                    AppDestination.ROOT -> Icons.Outlined.Security
                                    AppDestination.SETTINGS -> Icons.Outlined.Settings
                                },
                                contentDescription = item.label,
                            )
                        },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { paddingValues ->
        if (selectedFont != null) {
            FontDetailsScreen(
                font = selectedFont,
                modifier = Modifier.padding(paddingValues),
                onBack = { selectedFontId = null },
            )
        } else when (destination) {
            AppDestination.HOME -> HomeScreen(
                modifier = Modifier.padding(paddingValues),
                fontCount = fonts.size,
                onOpenLibrary = { destination = AppDestination.LIBRARY },
            )
            AppDestination.LIBRARY -> FontLibraryScreen(
                modifier = Modifier.padding(paddingValues),
                fonts = fonts,
                onRefresh = viewModel::refreshFonts,
                onImport = { importLauncher.launch(arrayOf("font/ttf", "font/otf", "font/collection", "application/octet-stream")) },
                onOpenDetails = { selectedFontId = it.id },
                errorMessage = viewModel.errorMessage,
                onDismissError = viewModel::clearError,
            )
            AppDestination.SETTINGS -> if (showReleaseCheck) {
                ReleaseReadinessScreen(
                    modifier = Modifier.padding(paddingValues),
                    environment = viewModel.rootEnvironment,
                    fontCount = viewModel.systemFontTargets.size,
                    moduleInstalled = viewModel.moduleInstalled,
                    logCount = rootLogs.size,
                    crashLogExists = java.io.File(context.filesDir, "crash.log").isFile,
                    onBack = { showReleaseCheck = false },
                )
            } else SettingsScreen(
                modifier = Modifier.padding(paddingValues),
                isDarkMode = viewModel.isDarkMode,
                onToggleTheme = viewModel::toggleTheme,
                autoBackup = viewModel.settings.autoBackup,
                autoDetectFonts = viewModel.settings.autoDetectFonts,
                restartReminder = viewModel.settings.restartReminder,
                saveLogs = viewModel.settings.saveLogs,
                moduleSavePath = viewModel.settings.moduleSavePath,
                onAutoBackupChanged = viewModel::setAutoBackup,
                onAutoDetectFontsChanged = viewModel::setAutoDetectFonts,
                onRestartReminderChanged = viewModel::setRestartReminder,
                onSaveLogsChanged = viewModel::setSaveLogs,
                onModuleSavePathChanged = viewModel::setModuleSavePath,
                onExportDiagnosticReport = viewModel::exportDiagnosticReport,
                onOpenReleaseCheck = { showReleaseCheck = true },
            )
            AppDestination.ROOT -> if (showRootLogs) {
                RootLogScreen(
                    logs = rootLogs,
                    modifier = Modifier.padding(paddingValues),
                )
            } else RootManagementScreen(
                modifier = Modifier.padding(paddingValues),
                environment = viewModel.rootEnvironment,
                fonts = fonts,
                backupCount = backups.size,
                backups = backups,
                modules = modules,
                currentTargets = viewModel.systemFontTargets,
                moduleInstalled = viewModel.moduleInstalled,
                busy = viewModel.rootBusy,
                message = viewModel.rootMessage,
                onRefresh = viewModel::refreshRootState,
                onApply = viewModel::applyFont,
                onGenerate = viewModel::generateModule,
                onRestore = viewModel::restoreFonts,
                onDeleteModule = viewModel::deleteModule,
                onDeleteBackup = viewModel::deleteBackup,
                onExport = viewModel::exportModule,
                onInstall = { module ->
                    val intent = moduleInstaller.installIntent(module.zipPath, viewModel.rootEnvironment.provider)
                        ?: moduleInstaller.shareIntent(module.zipPath)
                    context.startActivity(Intent.createChooser(intent, "安装 Global Font Manager 模块"))
                },
                onShare = { module ->
                    context.startActivity(Intent.createChooser(moduleInstaller.shareIntent(module.zipPath), "分享字体模块"))
                },
                onOpenFile = { module -> context.startActivity(moduleInstaller.openFileIntent(module.zipPath)) },
                onDeleteGeneratedModule = viewModel::deleteGeneratedModule,
                onOpenLogs = { showRootLogs = true },
                onExportReport = viewModel::exportCompatibilityReport,
                onDismissMessage = viewModel::clearRootMessage,
            )
        }
    }
}
