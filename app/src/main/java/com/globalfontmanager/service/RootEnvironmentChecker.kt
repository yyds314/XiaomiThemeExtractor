package com.globalfontmanager.service

class RootEnvironmentChecker(private val shell: RootShellManager) {
    suspend fun check(): RootEnvironment {
        val base = shell.detectEnvironment()
        if (!base.isGranted) return base.copy(warnings = listOf("Root 权限不可用，无法执行系统字体操作"))
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
