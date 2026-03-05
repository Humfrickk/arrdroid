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

data class ArtistUi(
    val id: Int,
    val name: String,
    val status: String?,
    val monitored: Boolean,
    val albumCount: Int?,
    val trackFileCount: Int?,
    val totalTrackCount: Int?,
    val percentOfTracks: Double?,
    val posterUrl: String?
)

data class AlbumUi(
    val id: Int,
    val title: String,
    val releaseDate: String?,
    val monitored: Boolean,
    val coverUrl: String?
)

data class ArtistListUiState(
    val loading: Boolean = false,
    val artists: List<ArtistUi> = emptyList(),
    val error: String? = null
)

data class ArtistDetailUiState(
    val loading: Boolean = false,
    val artist: ArtistUi? = null,
    val albums: List<AlbumUi> = emptyList(),
    val error: String? = null
)

class ArtistViewModel(
    private val repository: LidarrRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(ArtistListUiState())
    val listState = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(ArtistDetailUiState())
    val detailState = _detailState.asStateFlow()

    init {
        refreshArtists()
    }

    fun refreshArtists() {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(loading = true, error = null)
            val result = repository.getArtists()
            _listState.value = result.fold(
                onSuccess = { artists ->
                    ArtistListUiState(
                        loading = false,
                        artists = artists.map { a ->
                            val poster = a.images?.firstOrNull { it.coverType == "poster" }
                            ArtistUi(
                                id = a.id,
                                name = a.artistName ?: "Unbekannt",
                                status = a.status,
                                monitored = a.monitored ?: false,
                                albumCount = a.statistics?.albumCount,
                                trackFileCount = a.statistics?.trackFileCount,
                                totalTrackCount = a.statistics?.totalTrackCount,
                                percentOfTracks = a.statistics?.percentOfTracks,
                                posterUrl = poster?.remoteUrl ?: poster?.url
                            )
                        }.sortedBy { it.name.lowercase() }
                    )
                },
                onFailure = { e ->
                    ArtistListUiState(loading = false, error = e.message ?: "Unbekannter Fehler")
                }
            )
        }
    }

    fun loadArtistDetail(artistId: Int) {
        viewModelScope.launch {
            _detailState.value = ArtistDetailUiState(loading = true)
            val artistResult = repository.getArtist(artistId)
            val albumsResult = repository.getAlbumsForArtist(artistId)

            val artist = artistResult.getOrNull()
            val albums = albumsResult.getOrNull()

            if (artist != null) {
                val poster = artist.images?.firstOrNull { it.coverType == "poster" }
                _detailState.value = ArtistDetailUiState(
                    loading = false,
                    artist = ArtistUi(
                        id = artist.id,
                        name = artist.artistName ?: "Unbekannt",
                        status = artist.status,
                        monitored = artist.monitored ?: false,
                        albumCount = artist.statistics?.albumCount,
                        trackFileCount = artist.statistics?.trackFileCount,
                        totalTrackCount = artist.statistics?.totalTrackCount,
                        percentOfTracks = artist.statistics?.percentOfTracks,
                        posterUrl = poster?.remoteUrl ?: poster?.url
                    ),
                    albums = albums?.map { album ->
                        val cover = album.images?.firstOrNull { it.coverType == "cover" }
                        AlbumUi(
                            id = album.id,
                            title = album.title,
                            releaseDate = album.releaseDate,
                            monitored = album.monitored,
                            coverUrl = cover?.remoteUrl ?: cover?.url
                        )
                    } ?: emptyList()
                )
            } else {
                _detailState.value = ArtistDetailUiState(
                    loading = false,
                    error = artistResult.exceptionOrNull()?.message ?: "Fehler beim Laden"
                )
            }
        }
    }

    fun triggerAlbumSearch(albumId: Int) {
        viewModelScope.launch {
            repository.triggerAlbumSearch(albumId)
        }
    }

    companion object {
        fun Factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ArtistViewModel::class.java)) {
                        val storage = SettingsStorage(context.applicationContext)
                        val repo = LidarrRepository(storage)
                        @Suppress("UNCHECKED_CAST")
                        return ArtistViewModel(repo) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}

