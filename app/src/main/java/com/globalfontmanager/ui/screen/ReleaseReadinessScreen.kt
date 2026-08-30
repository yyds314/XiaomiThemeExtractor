package com.globalfontmanager.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.globalfontmanager.BuildConfig
import com.globalfontmanager.service.RootEnvironment

@Composable
fun ReleaseReadinessScreen(
    modifier: Modifier = Modifier,
    environment: RootEnvironment,
    fontCount: Int,
    moduleInstalled: Boolean,
    logCount: Int,
    crashLogExists: Boolean,
    onBack: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("发布前检查", style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onBack) { Text("返回") }
        }
        ReadinessCard("应用版本", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        ReadinessCard("系统", "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} / Android ${android.os.Build.VERSION.RELEASE}")
        ReadinessCard("Root", "${environment.status} / ${environment.provider}")
        ReadinessCard("SELinux", environment.selinuxMode.name)
        ReadinessCard("模块目录", if (environment.moduleDirectoryReadable && environment.moduleDirectoryWritable) "可读写" else "需要 Root 检查")
        ReadinessCard("字体目标", "$fontCount 个")
        ReadinessCard("当前模块", if (moduleInstalled) "已安装" else "未安装")
        ReadinessCard("操作日志", "$logCount 条")
        ReadinessCard("崩溃日志", if (crashLogExists) "存在待查看记录" else "无")
        environment.warnings.forEach { warning ->
            Text("风险：$warning", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun ReadinessCard(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(value, modifier = Modifier.weight(1f))
        }
    }
}
