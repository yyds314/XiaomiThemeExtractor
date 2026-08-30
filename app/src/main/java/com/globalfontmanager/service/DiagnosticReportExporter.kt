package com.globalfontmanager.service

import android.content.Context
import com.globalfontmanager.data.local.RootOperationLogEntity
import java.io.File

class DiagnosticReportExporter(private val context: Context) {
    fun export(
        environment: RootEnvironment,
        profile: SystemFontProfile?,
        moduleInstalled: Boolean,
        logs: List<RootOperationLogEntity>,
    ): File {
        val directory = context.filesDir.resolve("diagnostics").apply { mkdirs() }
        return directory.resolve("diagnostic-${System.currentTimeMillis()}.txt").apply {
            writeText(buildString {
                appendLine("Global Font Manager Diagnostic Report")
                appendLine("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                appendLine("Android: ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})")
                appendLine("Root: ${environment.status} / ${environment.provider}")
                appendLine("SELinux: ${environment.selinuxMode}")
                appendLine("Module directory readable: ${environment.moduleDirectoryReadable}")
                appendLine("Module directory writable: ${environment.moduleDirectoryWritable}")
                appendLine("Module installed: $moduleInstalled")
                appendLine("Warnings: ${environment.warnings.joinToString(" | ")}")
                appendLine("MIUI: ${profile?.miuiVersion ?: "unknown"}")
                appendLine("HyperOS: ${profile?.hyperOsVersion ?: "unknown"}")
                appendLine("ROM flavor: ${profile?.romFlavor ?: RomFlavor.OTHER}")
                appendLine("System font paths: ${profile?.fontPaths?.size ?: 0}")
                appendLine("Font mappings: ${profile?.mappings?.size ?: 0}")
                appendLine("Root operation logs:")
                logs.forEach { log -> appendLine("${log.time} [${log.result}] ${log.command} ${log.error}") }
                appendLine("Crash log:")
                appendLine(context.filesDir.resolve("crash.log").takeIf { it.isFile }?.readText() ?: "无")
            })
        }
    }
}
