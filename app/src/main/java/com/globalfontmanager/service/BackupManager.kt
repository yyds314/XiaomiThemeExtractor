package com.globalfontmanager.service

import android.os.Build
import com.globalfontmanager.data.local.FontBackupDao
import com.globalfontmanager.data.local.FontBackupEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class BackupManager(
    private val dao: FontBackupDao,
    private val shell: RootShellManager,
    private val backupDirectory: File,
) {
    val directory: File get() = backupDirectory

    fun observeBackups(): Flow<List<FontBackupEntity>> = dao.observeAll()

    suspend fun backup(targets: List<SystemFontTarget>): List<FontBackupEntity> = withContext(Dispatchers.IO) {
        backupDirectory.mkdirs()
        targets.map { target ->
            val id = UUID.randomUUID().toString()
            val destination = backupDirectory.resolve("$id-${target.fileName}")
            val copy = shell.runSu(
                "cp -p ${RootShellManager.quote(target.path)} ${RootShellManager.quote(destination.absolutePath)} " +
                    "&& chmod 644 ${RootShellManager.quote(destination.absolutePath)}",
            )
            check(copy.isSuccess) { "备份 ${target.fileName} 失败：${copy.stderr.ifBlank { copy.stdout }}" }
            FontBackupEntity(
                id = id,
                fontName = target.fileName,
                originalPath = target.path,
                backupPath = destination.absolutePath,
                createTime = System.currentTimeMillis(),
                deviceInfo = deviceInfo(),
            ).also { dao.insert(it) }
        }
    }

    suspend fun restoreLatest(): Boolean = withContext(Dispatchers.IO) {
        val backups = dao.getAll().groupBy { it.originalPath }.values.mapNotNull { it.maxByOrNull(FontBackupEntity::createTime) }
        // The overlay is the only system-side change. Removing it restores the original files.
        backups.isNotEmpty() && backups.all { backup ->
            shell.runSu("test -r ${RootShellManager.quote(backup.backupPath)}").isSuccess
        }
    }

    suspend fun delete(id: String): Boolean = withContext(Dispatchers.IO) {
        val backup = dao.getAll().firstOrNull { it.id == id } ?: return@withContext false
        val result = shell.runSu("rm -f ${RootShellManager.quote(backup.backupPath)}")
        if (!result.isSuccess) return@withContext false
        dao.delete(id)
        true
    }

    private fun deviceInfo(): String = listOf(
        "${Build.MANUFACTURER} ${Build.MODEL}",
        "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})",
        "${Build.DISPLAY} ${Build.ID}",
    ).joinToString(" | ")
}
