package com.globalfontmanager.ui.screen

import android.graphics.Typeface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.globalfontmanager.data.model.FontFile

@Composable
fun SystemFontSimulationPreview(font: FontFile?, modifier: Modifier = Modifier) {
    val family = remember(font?.path) {
        font?.let { runCatching { FontFamily(Typeface.Builder(it.path).build()) }.getOrNull() }
    }
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("MIUI / HyperOS 字体模拟预览", style = MaterialTheme.typography.titleLarge)
            Text("状态栏  12:45  5G  100%", fontFamily = family)
            Text("设置界面  显示与亮度", fontFamily = family)
            Text("通知栏  Global Font Manager", fontFamily = family)
            Text("锁屏文字  你好，世界", fontFamily = family)
            Text("桌面图标  相册  设置  浏览器", fontFamily = family)
        }
    }
}
