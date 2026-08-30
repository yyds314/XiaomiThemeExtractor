package com.globalfontmanager.service

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

class ModuleInstaller(private val context: Context) {
    fun installIntent(zipPath: String, provider: RootProvider): Intent? {
        val packageName = when (provider) {
            RootProvider.MAGISK -> "com.topjohnwu.magisk"
            RootProvider.KERNEL_SU -> "me.weishu.kernelsu"
            RootProvider.APATCH -> "me.bmax.apatch"
            RootProvider.UNKNOWN -> return null
        }
        if (!isPackageInstalled(packageName)) return null
        return viewIntent(zipPath, packageName)
    }

    fun shareIntent(zipPath: String): Intent {
        val uri = uriFor(zipPath)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun openFileIntent(zipPath: String): Intent = viewIntent(zipPath, null)

    private fun viewIntent(zipPath: String, packageName: String?): Intent {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = uriFor(zipPath)
            type = "application/zip"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (packageName != null) intent.setPackage(packageName)
        return intent
    }

    private fun uriFor(zipPath: String) = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        File(zipPath),
    )

    private fun isPackageInstalled(packageName: String): Boolean = runCatching {
        context.packageManager.getApplicationInfo(packageName, 0)
    }.isSuccess
}
