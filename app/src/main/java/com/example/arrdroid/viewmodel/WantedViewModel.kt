package com.example.arrdroid.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arrdroid.data.SettingsStorage
import com.example.arrdroid.repository.LidarrRepository
import kotlinx.coroutines.flow.MutableStateFlow
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
    val error: String? = null
)

class WantedViewModel(
    private val repository: LidarrRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WantedUiState())
    val uiState = _uiState.asStateFlow()

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
            repository.triggerMissingSearch(albumId)
            // optional: danach neu laden
            refresh()
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

