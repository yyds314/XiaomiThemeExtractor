package com.globalfontmanager.ui.screen

import android.graphics.Typeface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.globalfontmanager.data.model.FontFile
import java.text.DateFormat
import java.util.Date

@Composable
fun FontDetailsScreen(font: FontFile, modifier: Modifier = Modifier, onBack: () -> Unit) {
    val typeface = remember(font.path) { runCatching { Typeface.Builder(font.path).build() }.getOrNull() }
    val family = typeface?.let { FontFamily(it) }
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(font.displayName, style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onBack) { Text("返回字体库") }
        Text("字体预览", style = MaterialTheme.typography.titleLarge)
        Text("你好，世界\n小米科技\n\nHello Android\n\n123456789", fontFamily = family, style = MaterialTheme.typography.headlineSmall)
        Text("家族：${font.familyName}")
        Text("作者：${font.author}")
        Text("版本：${font.version}")
        Text("类型：${font.format.name} · 文件大小：${font.fileSizeBytes} bytes")
        Text("导入时间：${DateFormat.getDateTimeInstance().format(Date(font.createTime))}")
        Text("支持语言：${font.supportedLanguages.joinToString("、")}")
    }
}
