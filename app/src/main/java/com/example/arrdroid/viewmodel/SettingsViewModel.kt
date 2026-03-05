package com.example.arrdroid.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arrdroid.data.Settings
import com.example.arrdroid.data.SettingsStorage
import com.example.arrdroid.repository.LidarrRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val baseUrl: String = "",
    val apiKey: String = "",
    val testing: Boolean = false
)

class SettingsViewModel(
    private val storage: SettingsStorage,
    private val repository: LidarrRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    init {
        val settings = storage.load()
        if (settings != null) {
            _uiState.value = SettingsUiState(
                baseUrl = settings.baseUrl,
                apiKey = settings.apiKey
            )
        }
    }

    fun onBaseUrlChanged(value: String) {
        _uiState.value = _uiState.value.copy(baseUrl = value)
    }

    fun onApiKeyChanged(value: String) {
        _uiState.value = _uiState.value.copy(apiKey = value)
    }

    fun save() {
        val baseUrl = _uiState.value.baseUrl.trim()
        val apiKey = _uiState.value.apiKey.trim()
        if (baseUrl.isBlank() || apiKey.isBlank()) {
            viewModelScope.launch {
                _messages.emit("Bitte URL und API-Key ausfüllen.")
            }
            return
        }
        if (!baseUrl.startsWith("https://") && !baseUrl.startsWith("http://")) {
            viewModelScope.launch {
                _messages.emit("URL muss mit http:// oder https:// beginnen.")
            }
            return
        }
        storage.save(Settings(baseUrl, apiKey))
        viewModelScope.launch {
            _messages.emit("Einstellungen gespeichert.")
        }
    }

    fun testConnection() {
        save()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(testing = true)
            val result = repository.testConnection()
            _uiState.value = _uiState.value.copy(testing = false)
            result.fold(
                onSuccess = { status ->
                    _messages.emit("✅ Verbunden! Lidarr ${status.version ?: "unbekannt"}")
                },
                onFailure = { e ->
                    _messages.emit("❌ Verbindung fehlgeschlagen: ${e.message}")
                }
            )
        }
    }

    companion object {
        fun Factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                        val storage = SettingsStorage(context.applicationContext)
                        val repo = LidarrRepository(storage)
                        @Suppress("UNCHECKED_CAST")
                        return SettingsViewModel(storage, repo) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}


