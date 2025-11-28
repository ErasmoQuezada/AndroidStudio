package com.example.amiot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.amiot.data.FirestoreNewsRepository
import com.example.amiot.data.NewsItemFirestore
import com.example.amiot.data.FirebaseAuthRepository
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
    private val repository = FirestoreNewsRepository()
    private val authRepository = FirebaseAuthRepository()

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private val _userNews = MutableStateFlow<List<NewsItemFirestore>>(emptyList())
    val userNews: StateFlow<List<NewsItemFirestore>> = _userNews.asStateFlow()
    
    init {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                repository.listenToUserNews(currentUser.uid).collect { news ->
                    _userNews.value = news
                }
            }
        }
    }

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

            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Debes estar autenticado para agregar noticias"
                )
                return@launch
            }

            repository.addNews(
                userId = currentUser.uid,
                title = title,
                shortContent = shortContent,
                fullContent = fullContent,
                category = category,
                date = date
            ).fold(
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
    
    suspend fun getAllNews(): List<NewsItemFirestore> {
        return repository.getAllNews()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

