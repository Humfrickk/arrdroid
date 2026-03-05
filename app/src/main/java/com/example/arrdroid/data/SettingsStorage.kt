package com.example.arrdroid.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

data class Settings(
    val baseUrl: String,
    val apiKey: String
)

class SettingsStorage(private val context: Context) {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "arrdroid_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun load(): Settings? {
        val baseUrl = prefs.getString("base_url", null)
        val apiKey = prefs.getString("api_key", null)
        return if (!baseUrl.isNullOrBlank() && !apiKey.isNullOrBlank()) {
            Settings(baseUrl, apiKey)
        } else {
            null
        }
    }

    fun save(settings: Settings) {
        prefs.edit()
            .putString("base_url", settings.baseUrl)
            .putString("api_key", settings.apiKey)
            .apply()
    }
}

