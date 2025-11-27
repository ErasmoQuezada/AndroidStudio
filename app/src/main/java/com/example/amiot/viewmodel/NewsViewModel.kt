package com.example.amiot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.amiot.data.NewsRepository
import com.example.amiot.data.NewsItemSerializable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

data class NewsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NewsRepository(application)

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    val userNews: StateFlow<List<NewsItemSerializable>> = repository.userNews.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addNews(
        title: String,
        shortContent: String,
        fullContent: String,
        category: String,
        date: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            if (title.isBlank() || shortContent.isBlank() || fullContent.isBlank() || category.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Todos los campos son requeridos"
                )
                return@launch
            }

            val news = NewsItemSerializable(
                id = UUID.randomUUID().toString(),
                title = title,
                content = shortContent,
                fullContent = fullContent,
                date = date,
                category = category,
                isUserCreated = true
            )

            repository.addNews(news).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al agregar noticia"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

