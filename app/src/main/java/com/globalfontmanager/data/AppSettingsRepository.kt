package com.globalfontmanager.data

import android.content.Context

data class AppSettings(
    val autoBackup: Boolean = true,
    val autoDetectFonts: Boolean = true,
    val restartReminder: Boolean = true,
    val saveLogs: Boolean = true,
    val moduleSavePath: String = "modules/",
)

class AppSettingsRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    fun load(): AppSettings = AppSettings(
        autoBackup = preferences.getBoolean("auto_backup", true),
        autoDetectFonts = preferences.getBoolean("auto_detect_fonts", true),
        restartReminder = preferences.getBoolean("restart_reminder", true),
        saveLogs = preferences.getBoolean("save_logs", true),
        moduleSavePath = preferences.getString("module_save_path", "modules/").orEmpty(),
    )

    fun setBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    fun setModuleSavePath(value: String) {
        preferences.edit().putString("module_save_path", normalizePath(value)).apply()
    }

    private fun normalizePath(value: String): String {
        val path = value.trim().replace('\\', '/')
        return if (path.isBlank() || path.startsWith('/') || path.split('/').contains("..")) {
            "modules/"
        } else path.trim('/') + "/"
    }
}
