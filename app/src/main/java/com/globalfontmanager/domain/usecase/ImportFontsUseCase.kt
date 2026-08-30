package com.globalfontmanager.domain.usecase

import android.net.Uri
import com.globalfontmanager.domain.repository.FontRepository

class ImportFontsUseCase(private val repository: FontRepository) {
    suspend operator fun invoke(uris: List<Uri>) = repository.importFonts(uris)
}
