package com.globalfontmanager.domain.usecase

import com.globalfontmanager.domain.repository.FontRepository

class RefreshFontsUseCase(private val repository: FontRepository) {
    suspend operator fun invoke() = repository.refresh()
}
