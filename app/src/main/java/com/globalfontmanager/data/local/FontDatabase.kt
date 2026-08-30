package com.globalfontmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FontEntity::class, FontBackupEntity::class, FontModuleEntity::class, RootOperationLogEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class FontDatabase : RoomDatabase() {
    abstract fun fontDao(): FontDao
    abstract fun fontBackupDao(): FontBackupDao
    abstract fun fontModuleDao(): FontModuleDao
    abstract fun rootOperationLogDao(): RootOperationLogDao
}
