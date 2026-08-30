package com.globalfontmanager.utils

import android.content.Context
import androidx.room.Room
import com.globalfontmanager.data.importer.FontImporter
import com.globalfontmanager.data.local.FontDatabase
import com.globalfontmanager.data.local.MIGRATION_1_2
import com.globalfontmanager.data.local.MIGRATION_2_3
import com.globalfontmanager.data.parser.FontParser
import com.globalfontmanager.data.source.FontFileScanner
import com.globalfontmanager.domain.repository.FontRepository
import com.globalfontmanager.domain.usecase.ImportFontsUseCase
import com.globalfontmanager.domain.usecase.ObserveFontsUseCase
import com.globalfontmanager.domain.usecase.RefreshFontsUseCase
import com.globalfontmanager.repository.DefaultFontRepository
import com.globalfontmanager.service.BackupManager
import com.globalfontmanager.service.FontModuleGenerator
import com.globalfontmanager.service.FontReplacementEngine
import com.globalfontmanager.service.FontModuleExporter
import com.globalfontmanager.service.FontCompatibilityChecker
import com.globalfontmanager.service.DeviceCompatibilityReport
import com.globalfontmanager.service.SystemFontAnalyzer
import com.globalfontmanager.service.ModuleInstaller
import com.globalfontmanager.service.RootShellManager
import com.globalfontmanager.service.RootOperationLogger
import com.globalfontmanager.service.RootEnvironmentChecker
import com.globalfontmanager.service.DiagnosticReportExporter
import com.globalfontmanager.data.AppSettingsRepository

class ServiceLocator(context: Context) {
    private val applicationContext = context.applicationContext
    private val fontsDirectory = applicationContext.filesDir.resolve("fonts")
    private val parser = FontParser()
    val settings = AppSettingsRepository(applicationContext)
    private val importer = FontImporter(applicationContext.contentResolver, fontsDirectory, parser)
    private val database = Room.databaseBuilder(
        applicationContext,
        FontDatabase::class.java,
        "global-font-manager.db",
    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()

    val rootLogger = RootOperationLogger(database.rootOperationLogDao(), settings.load().saveLogs)
    val rootShell = RootShellManager(rootLogger)
    val rootEnvironmentChecker = RootEnvironmentChecker(rootShell)
    val systemFontAnalyzer = SystemFontAnalyzer(rootShell)
    val compatibilityChecker = FontCompatibilityChecker()
    val backupManager = BackupManager(
        dao = database.fontBackupDao(),
        shell = rootShell,
        backupDirectory = applicationContext.filesDir.resolve("font-backups"),
    )
    val moduleGenerator = FontModuleGenerator(applicationContext.filesDir.resolve(settings.load().moduleSavePath))
    val replacementEngine = FontReplacementEngine(
        shell = rootShell,
        backupManager = backupManager,
        moduleGenerator = moduleGenerator,
        parser = parser,
        analyzer = systemFontAnalyzer,
        compatibilityChecker = compatibilityChecker,
    )
    val moduleExporter = FontModuleExporter(applicationContext.filesDir.resolve("module-exports"))
    val moduleInstaller = ModuleInstaller(applicationContext)
    val compatibilityReport = DeviceCompatibilityReport(applicationContext.filesDir.resolve("reports"))
    val diagnosticReportExporter = DiagnosticReportExporter(applicationContext)
    val moduleDao = database.fontModuleDao()
    val rootLogDao = database.rootOperationLogDao()

    private val repository: FontRepository = DefaultFontRepository(
        dao = database.fontDao(),
        importer = importer,
        scanner = FontFileScanner(fontsDirectory, parser),
    )

    val observeFonts = ObserveFontsUseCase(repository)
    val refreshFonts = RefreshFontsUseCase(repository)
    val importFonts = ImportFontsUseCase(repository)

    fun updateModuleSavePath(path: String) {
        moduleGenerator.updateStagingRoot(applicationContext.filesDir.resolve(path))
    }
}
