package com.globalfontmanager.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    autoBackup: Boolean,
    autoDetectFonts: Boolean,
    restartReminder: Boolean,
    saveLogs: Boolean,
    moduleSavePath: String,
    onAutoBackupChanged: (Boolean) -> Unit,
    onAutoDetectFontsChanged: (Boolean) -> Unit,
    onRestartReminderChanged: (Boolean) -> Unit,
    onSaveLogsChanged: (Boolean) -> Unit,
    onModuleSavePathChanged: (String) -> Unit,
    onExportDiagnosticReport: () -> Unit,
    onOpenReleaseCheck: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("设置", style = MaterialTheme.typography.headlineMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("深色模式", style = MaterialTheme.typography.titleMedium)
                Text("切换应用界面主题", style = MaterialTheme.typography.bodyMedium)
            }
            Switch(checked = isDarkMode, onCheckedChange = { onToggleTheme() })
        }
        Text("高级设置", style = MaterialTheme.typography.titleLarge)
        SettingSwitch("自动备份", "应用字体前保存系统字体", autoBackup, onAutoBackupChanged)
        SettingSwitch("自动检测字体", "进入 Root 页面时读取系统字体", autoDetectFonts, onAutoDetectFontsChanged)
        SettingSwitch("重启提醒", "安装模块后提示重启设备", restartReminder, onRestartReminderChanged)
        SettingSwitch("保存日志", "保留 Root 操作和崩溃日志", saveLogs, onSaveLogsChanged)
        OutlinedTextField(
            value = moduleSavePath,
            onValueChange = onModuleSavePathChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("模块保存路径") },
            singleLine = true,
        )
        Button(onClick = onExportDiagnosticReport, modifier = Modifier.fillMaxWidth()) {
            Text("导出诊断报告")
        }
        Button(onClick = onOpenReleaseCheck, modifier = Modifier.fillMaxWidth()) {
            Text("打开发布前检查")
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onChanged: (Boolean) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}
