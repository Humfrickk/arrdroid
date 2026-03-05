package com.example.arrdroid.repository

import com.example.arrdroid.data.AlbumDto
import com.example.arrdroid.data.LidarrApi
import com.example.arrdroid.data.LidarrApiFactory
import com.example.arrdroid.data.Settings
import com.example.arrdroid.data.SettingsStorage

class LidarrRepository(
    private val settingsStorage: SettingsStorage
) {

    private fun apiOrNull(): LidarrApi? {
        val settings = settingsStorage.load() ?: return null
        return LidarrApiFactory.create(settings.baseUrl, settings.apiKey)
    }

    suspend fun getWanted(): Result<List<AlbumDto>> {
        val api = apiOrNull() ?: return Result.failure(IllegalStateException("Bitte zuerst NAS-URL und API-Key in den Einstellungen speichern."))
        return try {
            val result = api.getWantedMissing()
            Result.success(result.records)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    suspend fun triggerMissingSearch(albumId: Int): Result<Unit> {
        val api = apiOrNull() ?: return Result.failure(IllegalStateException("Bitte zuerst NAS-URL und API-Key in den Einstellungen speichern."))
        return try {
            val body = mapOf(
                "name" to "MissingAlbumSearch",
                "albumIds" to listOf(albumId)
            )
            api.sendCommand(body)
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    fun loadSettings(): Settings? = settingsStorage.load()

    fun saveSettings(settings: Settings) = settingsStorage.save(settings)
}

