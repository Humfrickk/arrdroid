package com.example.arrdroid.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arrdroid.data.SettingsStorage
import com.example.arrdroid.repository.LidarrRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WantedAlbumUi(
    val id: Int,
    val title: String,
    val artistName: String?
)

data class WantedUiState(
    val loading: Boolean = false,
    val albums: List<WantedAlbumUi> = emptyList(),
    val error: String? = null,
    val searchingIds: Set<Int> = emptySet()  // Album-IDs die gerade gesucht werden
)

class WantedViewModel(
    private val repository: LidarrRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WantedUiState())
    val uiState = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val result = repository.getWanted()
            _uiState.value = result.fold(
                onSuccess = { albums ->
                    WantedUiState(
                        loading = false,
                        albums = albums.map {
                            WantedAlbumUi(
                                id = it.id,
                                title = it.title,
                                artistName = it.artist?.artistName
                            )
                        }
                    )
                },
                onFailure = { e ->
                    WantedUiState(
                        loading = false,
                        error = e.message ?: "Unbekannter Fehler"
                    )
                }
            )
        }
    }

    fun triggerSearch(albumId: Int) {
        viewModelScope.launch {
            // Markiere dieses Album als "wird gesucht"
            _uiState.value = _uiState.value.copy(
                searchingIds = _uiState.value.searchingIds + albumId
            )

            val result = repository.triggerAlbumSearch(albumId)
            result.fold(
                onSuccess = { cmd ->
                    _messages.emit("✅ Suche gestartet (${cmd.commandName ?: "AlbumSearch"})")
                },
                onFailure = { e ->
                    _messages.emit("❌ Suche fehlgeschlagen: ${e.message}")
                }
            )

            // Entferne Markierung
            _uiState.value = _uiState.value.copy(
                searchingIds = _uiState.value.searchingIds - albumId
            )
        }
    }

    fun searchAllMissing() {
        viewModelScope.launch {
            _messages.emit("🔍 Suche alle fehlenden Alben…")
            val result = repository.triggerMissingAlbumSearch()
            result.fold(
                onSuccess = { _messages.emit("✅ Suche für alle fehlenden Alben gestartet") },
                onFailure = { e -> _messages.emit("❌ Fehler: ${e.message}") }
            )
        }
    }

    companion object {
        fun Factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(WantedViewModel::class.java)) {
                        val storage = SettingsStorage(context.applicationContext)
                        val repo = LidarrRepository(storage)
                        @Suppress("UNCHECKED_CAST")
                        return WantedViewModel(repo) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}

