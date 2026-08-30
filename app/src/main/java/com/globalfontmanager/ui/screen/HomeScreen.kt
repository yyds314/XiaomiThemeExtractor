package com.globalfontmanager.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    fontCount: Int,
    onOpenLibrary: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Global Font Manager", style = MaterialTheme.typography.headlineMedium)
        Text(
            "为 Android 系统界面管理字体，统一体验 MIUI 与 HyperOS 设备。",
            style = MaterialTheme.typography.bodyLarge,
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.FontDownload, contentDescription = null)
                Text("字体库", style = MaterialTheme.typography.titleLarge)
                Text("已导入 $fontCount 款字体")
            }
        }
        Button(onClick = onOpenLibrary, modifier = Modifier.fillMaxWidth()) {
            Text("浏览字体库")
            Icon(Icons.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.padding(start = 8.dp))
        }
    }
}
