package com.globalfontmanager.service

import com.globalfontmanager.data.local.RootOperationLogDao
import com.globalfontmanager.data.local.RootOperationLogEntity

class RootOperationLogger(
    private val dao: RootOperationLogDao,
    var enabled: Boolean = true,
) {
    suspend fun record(command: String, result: ShellResult) {
        if (!enabled) return
        dao.insert(
            RootOperationLogEntity(
                time = System.currentTimeMillis(),
                command = command,
                result = result.exitCode.toString(),
                error = result.stderr,
            ),
        )
    }
}
