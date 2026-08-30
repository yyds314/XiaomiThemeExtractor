package com.globalfontmanager.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS font_backups (
                id TEXT NOT NULL PRIMARY KEY,
                fontName TEXT NOT NULL,
                originalPath TEXT NOT NULL,
                backupPath TEXT NOT NULL,
                createTime INTEGER NOT NULL,
                deviceInfo TEXT NOT NULL
            )""".trimIndent(),
        )
    }
}
