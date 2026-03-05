package com.example.arrdroid.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arrdroid.data.Settings
import com.example.arrdroid.data.SettingsStorage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val baseUrl: String = "",
    val apiKey: String = ""
)

class SettingsViewModel(
    private val storage: SettingsStorage
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
        if (!baseUrl.startsWith("https://")) {
            viewModelScope.launch {
                _messages.emit("Achtung: Für Schutz deines API-Keys solltest du HTTPS verwenden.")
            }
        }
        storage.save(Settings(baseUrl, apiKey))
        viewModelScope.launch {
            _messages.emit("Einstellungen gespeichert.")
        }
    }

    companion object {
        fun Factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return SettingsViewModel(
                            SettingsStorage(context.applicationContext)
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}


