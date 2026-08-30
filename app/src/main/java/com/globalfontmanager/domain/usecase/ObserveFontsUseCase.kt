package com.globalfontmanager.domain.usecase

import com.globalfontmanager.data.model.FontFile
import com.globalfontmanager.domain.repository.FontRepository
import kotlinx.coroutines.flow.Flow

class ObserveFontsUseCase(private val repository: FontRepository) {
    operator fun invoke(): Flow<List<FontFile>> = repository.observeFonts()
}
