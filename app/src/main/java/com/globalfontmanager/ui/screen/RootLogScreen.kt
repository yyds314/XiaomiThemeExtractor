package com.globalfontmanager.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.globalfontmanager.data.local.RootOperationLogEntity
import java.text.DateFormat
import java.util.Date

@Composable
fun RootLogScreen(logs: List<RootOperationLogEntity>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { Text("Root 操作日志") }
        items(logs, key = { it.id }) { log ->
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(DateFormat.getDateTimeInstance().format(Date(log.time)))
                    Text(log.command)
                    Text("返回码：${log.result}")
                    if (log.error.isNotBlank()) Text(log.error)
                }
            }
        }
    }
}
