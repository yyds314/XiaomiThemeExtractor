package com.globalfontmanager.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.globalfontmanager.data.model.FontFile
import java.text.DateFormat
import java.util.Date

private enum class FontSort(val label: String) {
    NAME("名称"), TIME("时间"), TYPE("类型"),
}

@Composable
fun FontLibraryScreen(
    modifier: Modifier = Modifier,
    fonts: List<FontFile>,
    onRefresh: () -> Unit,
    onImport: () -> Unit,
    onOpenDetails: (FontFile) -> Unit,
    errorMessage: String?,
    onDismissError: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var sort by rememberSaveable { mutableStateOf(FontSort.TIME) }
    val visibleFonts = fonts.filter {
        query.isBlank() || it.displayName.contains(query, true) || it.familyName.contains(query, true)
    }.let { list ->
        when (sort) {
            FontSort.NAME -> list.sortedBy { it.displayName.lowercase() }
            FontSort.TIME -> list.sortedByDescending { it.createTime }
            FontSort.TYPE -> list.sortedBy { it.format.name }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("字体库", style = MaterialTheme.typography.headlineMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onImport) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Text("导入")
                }
                TextButton(onClick = onRefresh) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null)
                    Text("扫描")
                }
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("搜索字体名称或家族") },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FontSort.entries.forEach { option ->
                FilterChip(
                    selected = sort == option,
                    onClick = { sort = option },
                    label = { Text(option.label) },
                )
            }
        }
        if (errorMessage != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)) {
                    Text(errorMessage, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = onDismissError) { Text("关闭") }
                }
            }
        }
        if (visibleFonts.isEmpty()) {
            Text("暂无字体，请从系统文件选择器导入 TTF、OTF 或 TTC 文件。")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(visibleFonts, key = { it.id }) { font ->
                    FontCard(font = font, onClick = { onOpenDetails(font) })
                }
            }
        }
    }
}

@Composable
private fun FontCard(font: FontFile, onClick: () -> Unit) {
    val previewFamily = runCatching { FontFamily(android.graphics.Typeface.Builder(font.path).build()) }.getOrNull()
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("你好，世界  Hello Android  123456789", fontFamily = previewFamily)
            Text(font.displayName, style = MaterialTheme.typography.titleMedium)
            Text("${font.format.name} · ${formatBytes(font.fileSizeBytes)}")
            Text("导入时间：${DateFormat.getDateTimeInstance().format(Date(font.createTime))}")
            Text(if (font.isApplied) "应用状态：已应用" else "应用状态：未应用")
        }
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / 1024f / 1024f)
    bytes >= 1024 -> "%.1f KB".format(bytes / 1024f)
    else -> "$bytes B"
}
