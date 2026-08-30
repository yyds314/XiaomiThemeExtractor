package com.globalfontmanager.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS font_modules (
                moduleId TEXT NOT NULL PRIMARY KEY,
                moduleName TEXT NOT NULL,
                version TEXT NOT NULL,
                fontName TEXT NOT NULL,
                createdTime INTEGER NOT NULL,
                zipPath TEXT NOT NULL,
                installedStatus INTEGER NOT NULL
            )""".trimIndent(),
        )
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS root_operation_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                time INTEGER NOT NULL,
                command TEXT NOT NULL,
                result TEXT NOT NULL,
                error TEXT NOT NULL
            )""".trimIndent(),
        )
    }
}
