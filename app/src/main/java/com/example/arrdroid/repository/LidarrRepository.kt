package com.example.arrdroid.repository

import com.example.arrdroid.data.AlbumDto
import com.example.arrdroid.data.ArtistDto
import com.example.arrdroid.data.CommandResponseDto
import com.example.arrdroid.data.LidarrApi
import com.example.arrdroid.data.LidarrApiFactory
import com.example.arrdroid.data.QueueItemDto
import com.example.arrdroid.data.RootFolderDto
import com.example.arrdroid.data.Settings
import com.example.arrdroid.data.SettingsStorage
import com.example.arrdroid.data.SystemStatusDto

class LidarrRepository(
    private val settingsStorage: SettingsStorage
) {

    private fun apiOrNull(): LidarrApi? {
        val settings = settingsStorage.load() ?: return null
        return LidarrApiFactory.create(settings.baseUrl, settings.apiKey)
    }

    private fun requireApi(): Result<LidarrApi> {
        val api = apiOrNull()
            ?: return Result.failure(IllegalStateException("Bitte zuerst URL und API-Key in den Einstellungen speichern."))
        return Result.success(api)
    }

    suspend fun getWanted(): Result<List<AlbumDto>> = runCatching {
        requireApi().getOrThrow().getWantedMissing().records
    }

    suspend fun getArtists(): Result<List<ArtistDto>> = runCatching {
        requireApi().getOrThrow().getArtists()
    }

    suspend fun getArtist(id: Int): Result<ArtistDto> = runCatching {
        requireApi().getOrThrow().getArtist(id)
    }

    suspend fun getAlbumsForArtist(artistId: Int): Result<List<AlbumDto>> = runCatching {
        requireApi().getOrThrow().getAlbumsByArtist(artistId)
    }

    suspend fun triggerAlbumSearch(albumId: Int): Result<CommandResponseDto> = runCatching {
        val body = mapOf<String, Any>(
            "name" to "AlbumSearch",
            "albumIds" to listOf(albumId)
        )
        requireApi().getOrThrow().sendCommand(body)
    }

    suspend fun triggerMissingAlbumSearch(): Result<CommandResponseDto> = runCatching {
        val body = mapOf<String, Any>(
            "name" to "MissingAlbumSearch"
        )
        requireApi().getOrThrow().sendCommand(body)
    }

    suspend fun getQueue(): Result<List<QueueItemDto>> = runCatching {
        requireApi().getOrThrow().getQueue().records
    }

    suspend fun removeFromQueue(id: Int): Result<Unit> = runCatching {
        requireApi().getOrThrow().removeFromQueue(id)
    }

    suspend fun getRootFolders(): Result<List<RootFolderDto>> = runCatching {
        requireApi().getOrThrow().getRootFolders()
    }

    suspend fun getSystemStatus(): Result<SystemStatusDto> = runCatching {
        requireApi().getOrThrow().getSystemStatus()
    }

    suspend fun testConnection(): Result<SystemStatusDto> {
        return try {
            val status = requireApi().getOrThrow().getSystemStatus()
            Result.success(status)
        } catch (e: com.squareup.moshi.JsonDataException) {
            Result.failure(Exception("Server antwortet nicht mit JSON. Prüfe die URL – sie sollte z.B. http://192.168.1.10:8686 sein."))
        } catch (e: com.squareup.moshi.JsonEncodingException) {
            Result.failure(Exception("Server antwortet mit ungültigem JSON (vermutlich HTML). Prüfe die URL."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun loadSettings(): Settings? = settingsStorage.load()

    fun saveSettings(settings: Settings) = settingsStorage.save(settings)
}

