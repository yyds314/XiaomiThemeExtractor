package com.globalfontmanager.repository

import android.net.Uri
import com.globalfontmanager.data.importer.FontImporter
import com.globalfontmanager.data.local.FontDao
import com.globalfontmanager.data.local.toDomain
import com.globalfontmanager.data.model.FontFile
import com.globalfontmanager.data.source.FontFileScanner
import com.globalfontmanager.domain.repository.FontRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultFontRepository(
    private val dao: FontDao,
    private val importer: FontImporter,
    private val scanner: FontFileScanner,
) : FontRepository {
    override fun observeFonts(): Flow<List<FontFile>> = dao.observeAll().map { items ->
        items.map { it.toDomain() }
    }

    override suspend fun refresh() {
        withContext(Dispatchers.IO) {
            scanner.scan().forEach { entity ->
                if (dao.findById(entity.id) == null) dao.upsert(entity)
            }
        }
    }

    override suspend fun importFonts(uris: List<Uri>) {
        withContext(Dispatchers.IO) {
            uris.forEach { dao.upsert(importer.importFont(it)) }
        }
    }
}
