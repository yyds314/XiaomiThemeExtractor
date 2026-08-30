package com.globalfontmanager.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.globalfontmanager.data.model.FontFile
import com.globalfontmanager.data.local.FontBackupEntity
import com.globalfontmanager.data.local.FontModuleEntity
import com.globalfontmanager.service.RootEnvironment
import com.globalfontmanager.service.RootProvider
import com.globalfontmanager.service.RootStatus
import com.globalfontmanager.service.SystemFontTarget

@Composable
fun RootManagementScreen(
    modifier: Modifier = Modifier,
    environment: RootEnvironment,
    fonts: List<FontFile>,
    backupCount: Int,
    backups: List<FontBackupEntity>,
    modules: List<FontModuleEntity>,
    currentTargets: List<SystemFontTarget>,
    moduleInstalled: Boolean,
    busy: Boolean,
    message: String?,
    onRefresh: () -> Unit,
    onApply: (FontFile) -> Unit,
    onGenerate: (FontFile) -> Unit,
    onRestore: () -> Unit,
    onDeleteModule: () -> Unit,
    onDeleteBackup: (String) -> Unit,
    onExport: (FontFile) -> Unit,
    onInstall: (FontModuleEntity) -> Unit,
    onShare: (FontModuleEntity) -> Unit,
    onOpenFile: (FontModuleEntity) -> Unit,
    onDeleteGeneratedModule: (FontModuleEntity) -> Unit,
    onOpenLogs: () -> Unit,
    onExportReport: (FontFile) -> Unit,
    onDismissMessage: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Root 管理", style = MaterialTheme.typography.headlineMedium)
                Row {
                    TextButton(onClick = onOpenLogs) { Text("日志") }
                    TextButton(onClick = onRefresh, enabled = !busy) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "刷新 Root 状态")
                        Text("刷新")
                    }
                }
            }
        }
        item { RootStatusCard(environment, moduleInstalled, backupCount, currentTargets) }
        item { SystemFontSimulationPreview(fonts.firstOrNull()) }
        if (message != null) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Text(message, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary)
                        TextButton(onClick = onDismissMessage) { Text("关闭") }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRestore, enabled = environment.isGranted && !busy) { Text("恢复默认字体") }
                OutlinedButton(onClick = onDeleteModule, enabled = environment.isGranted && moduleInstalled && !busy) { Text("删除模块") }
            }
        }
        if (backups.isNotEmpty()) {
            item { Text("字体备份", style = MaterialTheme.typography.titleLarge) }
            items(backups, key = { it.id }) { backup ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(backup.fontName, style = MaterialTheme.typography.titleMedium)
                            Text(backup.originalPath, style = MaterialTheme.typography.bodySmall)
                            Text(backup.deviceInfo, style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = { onDeleteBackup(backup.id) }, enabled = !busy) { Text("删除") }
                    }
                }
            }
        }
        if (busy) item { CircularProgressIndicator() }
        item { Text("选择字体", style = MaterialTheme.typography.titleLarge) }
        items(fonts, key = { it.id }) { font ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(font.displayName, style = MaterialTheme.typography.titleMedium)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onApply(font) }, enabled = environment.isGranted && !busy) { Text("备份并应用") }
                            OutlinedButton(onClick = { onGenerate(font) }, enabled = environment.isGranted && !busy) { Text("生成模块") }
                        }
                            OutlinedButton(onClick = { onExport(font) }, enabled = environment.isGranted && !busy) { Text("导出 ZIP") }
                            TextButton(onClick = { onExportReport(font) }, enabled = environment.isGranted && !busy) { Text("导出报告") }
                    }
                }
            }
        }
        if (modules.isNotEmpty()) {
            item { Text("已生成模块", style = MaterialTheme.typography.titleLarge) }
            items(modules, key = { it.moduleId }) { module ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(module.fontName, style = MaterialTheme.typography.titleMedium)
                        Text("${module.moduleName} ${module.version}")
                        Text("安装状态：${if (module.installedStatus) "已安装" else "未安装"}")
                        Text("更新时间：${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(module.createdTime))}")
                        Text(module.zipPath, style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { onInstall(module) }, enabled = !busy) { Text("安装引导") }
                            TextButton(onClick = { onShare(module) }, enabled = !busy) { Text("分享") }
                            TextButton(onClick = { onOpenFile(module) }, enabled = !busy) { Text("打开位置") }
                            TextButton(onClick = { onDeleteGeneratedModule(module) }, enabled = !busy) { Text("删除") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RootStatusCard(
    environment: RootEnvironment,
    moduleInstalled: Boolean,
    backupCount: Int,
    currentTargets: List<SystemFontTarget>,
) {
    val status = when (environment.status) {
        RootStatus.GRANTED -> "已获取"
        RootStatus.DENIED -> "权限被拒绝"
        RootStatus.UNAVAILABLE -> "未获取"
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Root 状态：$status")
            Text("Root 方案：${providerLabel(environment.provider)}")
            Text("当前系统：${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} · Android ${android.os.Build.VERSION.RELEASE}")
            Text("当前字体：${currentTargets.joinToString { it.fileName }.ifBlank { "未检测到" }}")
            Text("模块状态：${if (moduleInstalled) "已安装" else "未安装"}")
            Text("备份数量：$backupCount")
            Text("模块路径：${environment.modulePath ?: "不可用"}")
            Text("模块目录：${if (environment.moduleDirectoryReadable) "可读" else "不可读或未创建"}")
            Text("模块写入权限：${if (environment.moduleDirectoryWritable) "可写" else "不可写或未创建"}")
            Text("SELinux：${environment.selinuxMode}")
            environment.warnings.forEach { warning ->
                Text("风险提示：$warning", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun providerLabel(provider: RootProvider): String = when (provider) {
    RootProvider.MAGISK -> "Magisk"
    RootProvider.KERNEL_SU -> "KernelSU"
    RootProvider.APATCH -> "APatch"
    RootProvider.UNKNOWN -> "未知"
}
