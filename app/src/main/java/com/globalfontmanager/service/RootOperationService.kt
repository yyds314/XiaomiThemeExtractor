package com.globalfontmanager.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class RootStatus {
    GRANTED,
    UNAVAILABLE,
    DENIED,
}

enum class RootProvider {
    MAGISK,
    KERNEL_SU,
    APATCH,
    UNKNOWN,
}

enum class SelinuxMode {
    ENFORCING,
    PERMISSIVE,
    UNKNOWN,
}

data class RootEnvironment(
    val status: RootStatus,
    val provider: RootProvider,
    val modulePath: String?,
    val suPath: String?,
    val moduleDirectoryReadable: Boolean = false,
    val moduleDirectoryWritable: Boolean = false,
    val selinuxMode: SelinuxMode = SelinuxMode.UNKNOWN,
    val warnings: List<String> = emptyList(),
) {
    val isGranted: Boolean get() = status == RootStatus.GRANTED
}

data class ShellResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
) {
    val isSuccess: Boolean get() = exitCode == 0
}

data class SystemFontTarget(
    val path: String,
    val fileName: String,
    val fontIndex: Int = 0,
)

interface RootOperationService {
    suspend fun detectEnvironment(): RootEnvironment
    suspend fun detectSystemFonts(): List<SystemFontTarget>
    suspend fun installModule(moduleDirectory: String): ShellResult
    suspend fun disableModule(): ShellResult
    suspend fun removeModule(): ShellResult
    suspend fun emergencyRestore(): ShellResult
    suspend fun isModuleInstalled(): Boolean
}

class RootShellManager(private val logger: RootOperationLogger? = null) : RootOperationService {
    @Volatile
    private var cachedSuPath: String? = null
    @Volatile
    private var suResolved = false

    override suspend fun detectEnvironment(): RootEnvironment = withContext(Dispatchers.IO) {
        val suPath = resolveSuPath()
        val result = runSu("id")
        val granted = result.isSuccess && result.stdout.contains("uid=0")
        if (!granted) {
            val status = if (suPath == null && result.exitCode == -1 && result.stderr.contains("无法启动 su")) {
                RootStatus.UNAVAILABLE
            } else {
                RootStatus.DENIED
            }
            return@withContext RootEnvironment(status, RootProvider.UNKNOWN, null, suPath)
        }
        val provider = when {
            runSu("test -d /data/adb/magisk -o -f /data/adb/magisk.db").isSuccess -> RootProvider.MAGISK
            runSu("test -d /data/adb/ksu -o -f /data/adb/ksud").isSuccess -> RootProvider.KERNEL_SU
            runSu("test -d /data/adb/ap -o -d /data/adb/ap/bin").isSuccess -> RootProvider.APATCH
            else -> RootProvider.UNKNOWN
        }
        val modulePath = when (provider) {
            RootProvider.MAGISK, RootProvider.KERNEL_SU, RootProvider.APATCH, RootProvider.UNKNOWN -> MODULE_PATH
        }
        RootEnvironment(RootStatus.GRANTED, provider, modulePath, suPath)
    }

    override suspend fun detectSystemFonts(): List<SystemFontTarget> = withContext(Dispatchers.IO) {
        FONT_ROOTS.flatMap { root -> listFiles(root) }
            .filter { path -> path.substringAfterLast('/').substringAfterLast('.').lowercase() in SUPPORTED_EXTENSIONS }
            .map { path -> SystemFontTarget(path, path.substringAfterLast('/')) }
            .distinctBy { it.path }
    }

    override suspend fun installModule(moduleDirectory: String): ShellResult = withContext(Dispatchers.IO) {
        require(java.io.File(moduleDirectory).isDirectory) { "模块目录不存在" }
        val source = quote(moduleDirectory)
        val target = quote(MODULE_PATH)
        runSu(
            "rm -rf $target && mkdir -p $target && cp -a $source/. $target/ && " +
                "chown -R 0:0 $target && find $target -type d -exec chmod 755 {} \\; && " +
                "find $target -type f -exec chmod 644 {} \\; && chmod 755 $target/*.sh",
        )
    }

    override suspend fun disableModule(): ShellResult = withContext(Dispatchers.IO) {
        runSu("touch ${quote("$MODULE_PATH/disable")}")
    }

    override suspend fun removeModule(): ShellResult = withContext(Dispatchers.IO) {
        runSu("rm -rf ${quote(MODULE_PATH)}")
    }

    override suspend fun emergencyRestore(): ShellResult = withContext(Dispatchers.IO) {
        runSu("touch ${quote("$MODULE_PATH/disable")} && rm -rf ${quote(MODULE_PATH)}")
    }

    override suspend fun isModuleInstalled(): Boolean = withContext(Dispatchers.IO) {
        runSu("test -f ${quote("$MODULE_PATH/module.prop")}").isSuccess
    }

    suspend fun checkFreeSpace(path: String, requiredBytes: Long): Boolean {
        val result = runSu("df -Pk ${quote(path)}")
        val availableKb = result.stdout.lineSequence()
            .drop(1)
            .mapNotNull { line -> line.trim().split(Regex("\\s+")) .getOrNull(3)?.toLongOrNull() }
            .firstOrNull()
            ?: return false
        return availableKb * 1024 >= requiredBytes
    }

    suspend fun runSu(command: String, timeoutMillis: Long = 30_000): ShellResult {
        val result = try {
            val process = ProcessBuilder(*suCommand(command)).redirectErrorStream(false).start()
            val finished = process.waitFor(timeoutMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
            val stdout = process.inputStream.bufferedReader().use { it.readText() }
            val stderr = process.errorStream.bufferedReader().use { it.readText() }
            if (!finished) {
                process.destroyForcibly()
                ShellResult(-1, stdout, "Root 命令超时")
            } else {
                ShellResult(process.exitValue(), stdout, stderr)
            }
        } catch (error: Exception) {
            ShellResult(-1, "", error.message ?: "无法启动 su")
        }
        logger?.record(command, result)
        return result
    }

    private fun resolveSuPath(): String? {
        if (suResolved) return cachedSuPath
        cachedSuPath = SU_PATHS.firstOrNull { path ->
            val file = java.io.File(path)
            file.isFile && (file.canExecute() || file.exists())
        } ?: runCatching {
            val process = ProcessBuilder("sh", "-c", "command -v su || which su").redirectErrorStream(true).start()
            val finished = process.waitFor(5_000, java.util.concurrent.TimeUnit.MILLISECONDS)
            if (!finished) {
                process.destroyForcibly()
                return@runCatching null
            }
            process.inputStream.bufferedReader().use { it.readText() }
                .lineSequence()
                .map(String::trim)
                .firstOrNull { it.isNotBlank() && !it.contains("not found", ignoreCase = true) }
        }.getOrNull()
        suResolved = true
        return cachedSuPath
    }

    private fun suCommand(command: String): Array<String> {
        val suPath = resolveSuPath()
        return if (suPath.isNullOrBlank()) {
            arrayOf("su", "-c", command)
        } else {
            arrayOf(suPath, "-c", command)
        }
    }

    suspend fun readFile(path: String): String = runSu("cat ${quote(path)}").stdout

    suspend fun fileExists(path: String): Boolean = runSu("test -f ${quote(path)}").isSuccess

    suspend fun listFiles(path: String): List<String> {
        val result = runSu("find ${quote(path)} -maxdepth 1 -type f -print")
        return if (result.isSuccess) result.stdout.lineSequence().filter(String::isNotBlank).toList() else emptyList()
    }

    suspend fun readProperty(name: String): String = runSu("getprop ${quote(name)}").stdout.trim()

    companion object {
        const val MODULE_ID = "global.font.manager"
        const val MODULE_PATH = "/data/adb/modules/$MODULE_ID"
        val FONT_ROOTS = listOf("/system/fonts", "/product/fonts", "/system_ext/fonts", "/vendor/fonts")
        private val SUPPORTED_EXTENSIONS = setOf("ttf", "otf", "ttc")
        val SU_PATHS = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/bin/.ext/.su",
            "/debug_ramdisk/su",
            "/debug_ramdisk/bin/su",
            "/debug_ramdisk/ksu/bin/su",
            "/dev/ksu/bin/su",
            "/data/adb/ksu/bin/su",
            "/data/adb/magisk/su",
            "/data/adb/ap/bin/su",
        )

        fun quote(value: String): String = "'${value.replace("'", "'\\''")}'"
    }
}
