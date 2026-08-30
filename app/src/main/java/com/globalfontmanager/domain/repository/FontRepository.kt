package com.globalfontmanager.domain.repository

import android.net.Uri
import com.globalfontmanager.data.model.FontFile
import kotlinx.coroutines.flow.Flow

interface FontRepository {
    fun observeFonts(): Flow<List<FontFile>>
    suspend fun refresh()
    suspend fun importFonts(uris: List<Uri>)
}
