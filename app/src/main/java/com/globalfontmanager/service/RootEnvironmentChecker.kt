package com.globalfontmanager.service

class RootEnvironmentChecker(private val shell: RootShellManager) {
    suspend fun check(): RootEnvironment {
        val base = shell.detectEnvironment()
        if (!base.isGranted) {
            val warning = when (base.status) {
                RootStatus.DENIED -> "Root 请求已被拒绝。请在 Magisk / KernelSU / APatch 中允许 Global Font Manager，然后点刷新"
                else -> "未检测到可用 su。请确认已安装 KernelSU、Magisk 或 APatch，首次打开时允许 Root 授权"
            }
            return base.copy(warnings = listOf(warning))
        }
        val modulePath = RootShellManager.quote(RootShellManager.MODULE_PATH)
        val moduleReadable = shell.runSu("test -d $modulePath && test -r $modulePath").isSuccess
        val moduleWritable = shell.runSu("test -d $modulePath && test -w $modulePath && test -x $modulePath").isSuccess
        val selinux = when (shell.runSu("getenforce").stdout.trim().uppercase()) {
            "ENFORCING" -> SelinuxMode.ENFORCING
            "PERMISSIVE" -> SelinuxMode.PERMISSIVE
            else -> SelinuxMode.UNKNOWN
        }
        val warnings = buildList {
            if (!moduleReadable) add("Root 模块目录不存在或不可读")
            if (moduleReadable && !moduleWritable) add("Root 模块目录不可写或不可执行")
            if (selinux == SelinuxMode.PERMISSIVE) add("SELinux 处于 Permissive，系统安全性已降低")
            if (selinux == SelinuxMode.UNKNOWN) add("无法确认 SELinux 状态")
        }
        return base.copy(
            moduleDirectoryReadable = moduleReadable,
            moduleDirectoryWritable = moduleWritable,
            selinuxMode = selinux,
            warnings = warnings,
        )
    }
}
