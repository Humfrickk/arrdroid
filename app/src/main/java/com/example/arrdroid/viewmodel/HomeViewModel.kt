package com.example.arrdroid.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arrdroid.data.QueueItemDto
import com.example.arrdroid.data.SettingsStorage
import com.example.arrdroid.repository.LidarrRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class QueueItemUi(
    val id: Int,
    val title: String,
    val artistName: String?,
    val status: String,
    val progress: Float,       // 0.0 – 1.0
    val sizeTotal: Double,
    val sizeRemaining: Double,
    val timeLeft: String?,
    val downloadClient: String?
)

data class DiskSpaceUi(
    val path: String,
    val freeGb: Double,
    val totalGb: Double,
    val usedPercent: Float     // 0.0 – 1.0
)

data class LibraryStatsUi(
    val artistCount: Int = 0,
    val wantedCount: Int = 0
)

data class HomeUiState(
    val loading: Boolean = false,
    val configured: Boolean = true,
    val error: String? = null,
    val serverVersion: String? = null,
    val queue: List<QueueItemUi> = emptyList(),
    val diskSpaces: List<DiskSpaceUi> = emptyList(),
    val libraryStats: LibraryStatsUi = LibraryStatsUi()
)

class HomeViewModel(
    private val repository: LidarrRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
        startQueuePolling()
    }

    fun refresh() {
        viewModelScope.launch {
            if (repository.loadSettings() == null) {
                _uiState.value = HomeUiState(configured = false)
                return@launch
            }

            _uiState.value = _uiState.value.copy(loading = true, error = null)

            // Lade alles parallel
            val statusResult = repository.getSystemStatus()
            val queueResult = repository.getQueue()
            val artistsResult = repository.getArtists()
            val wantedResult = repository.getWanted()
            val rootFolderResult = repository.getRootFolders()

            val error = statusResult.exceptionOrNull()?.message

            _uiState.value = HomeUiState(
                loading = false,
                configured = true,
                error = error,
                serverVersion = statusResult.getOrNull()?.version,
                queue = queueResult.getOrDefault(emptyList()).map { it.toUi() },
                diskSpaces = rootFolderResult.getOrDefault(emptyList()).mapNotNull { folder ->
                    val free = folder.freeSpace ?: return@mapNotNull null
                    val total = folder.totalSpace ?: return@mapNotNull null
                    if (total <= 0) return@mapNotNull null
                    DiskSpaceUi(
                        path = folder.path ?: "?",
                        freeGb = free / 1_073_741_824.0,
                        totalGb = total / 1_073_741_824.0,
                        usedPercent = ((total - free).toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    )
                },
                libraryStats = LibraryStatsUi(
                    artistCount = artistsResult.getOrDefault(emptyList()).size,
                    wantedCount = wantedResult.getOrDefault(emptyList()).size
                )
            )
        }
    }

    private fun startQueuePolling() {
        viewModelScope.launch {
            while (isActive) {
                delay(5_000)
                if (repository.loadSettings() == null) continue
                val queueResult = repository.getQueue()
                queueResult.onSuccess { items ->
                    _uiState.value = _uiState.value.copy(
                        queue = items.map { it.toUi() }
                    )
                }
            }
        }
    }

    fun removeFromQueue(id: Int) {
        viewModelScope.launch {
            repository.removeFromQueue(id)
            // Sofort aktualisieren
            val queueResult = repository.getQueue()
            queueResult.onSuccess { items ->
                _uiState.value = _uiState.value.copy(
                    queue = items.map { it.toUi() }
                )
            }
        }
    }

    private fun QueueItemDto.toUi(): QueueItemUi {
        val total = size ?: 0.0
        val remaining = sizeleft ?: 0.0
        val progress = if (total > 0) ((total - remaining) / total).toFloat().coerceIn(0f, 1f) else 0f

        val displayStatus = when (trackedDownloadState) {
            "downloading" -> "Wird heruntergeladen"
            "importPending" -> "Import ausstehend"
            "importing" -> "Wird importiert"
            "imported" -> "Importiert"
            "failedPending" -> "Fehlgeschlagen"
            "failed" -> "Fehlgeschlagen"
            else -> status ?: "Unbekannt"
        }

        return QueueItemUi(
            id = id,
            title = title ?: "Unbekannt",
            artistName = null,   // Artist-Name kommt nicht direkt, wir zeigen den Titel
            status = displayStatus,
            progress = progress,
            sizeTotal = total,
            sizeRemaining = remaining,
            timeLeft = timeleft,
            downloadClient = downloadClient
        )
    }

    companion object {
        fun Factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                        val storage = SettingsStorage(context.applicationContext)
                        val repo = LidarrRepository(storage)
                        @Suppress("UNCHECKED_CAST")
                        return HomeViewModel(repo) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}

