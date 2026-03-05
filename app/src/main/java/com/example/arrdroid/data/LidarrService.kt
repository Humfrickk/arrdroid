package com.example.arrdroid.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LidarrApi {

    @GET("wanted/missing")
    suspend fun getWantedMissing(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
        @Query("includeArtist") includeArtist: Boolean = true,
        @Query("sortKey") sortKey: String = "releaseDate",
        @Query("sortDirection") sortDirection: String = "ascending"
    ): PagedResult<AlbumDto>

    @GET("artist")
    suspend fun getArtists(): List<ArtistDto>

    @GET("artist/{id}")
    suspend fun getArtist(
        @Path("id") artistId: Int
    ): ArtistDto

    @GET("album")
    suspend fun getAlbumsByArtist(
        @Query("artistId") artistId: Int,
        @Query("includeAllArtistAlbums") includeAll: Boolean = false
    ): List<AlbumDto>

    @GET("album/lookup")
    suspend fun searchAlbums(
        @Query("term") query: String
    ): List<AlbumDto>

    @GET("queue")
    suspend fun getQueue(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
        @Query("includeArtist") includeArtist: Boolean = true,
        @Query("includeAlbum") includeAlbum: Boolean = true
    ): PagedResult<QueueItemDto>

    @DELETE("queue/{id}")
    suspend fun removeFromQueue(
        @Path("id") id: Int,
        @Query("removeFromClient") removeFromClient: Boolean = true,
        @Query("blocklist") blocklist: Boolean = false
    )

    @GET("rootfolder")
    suspend fun getRootFolders(): List<RootFolderDto>

    @GET("system/status")
    suspend fun getSystemStatus(): SystemStatusDto

    @POST("command")
    suspend fun sendCommand(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): CommandResponseDto
}

object LidarrApiFactory {

    /**
     * baseUrl sollte idealerweise HTTPS sein, damit der API-Key
     * auf dem Transportweg nicht mitgelesen werden kann.
     */
    fun create(baseUrl: String, apiKey: String): LidarrApi {
        // URL normalisieren: sicherstellen, dass /api/v1/ am Ende steht
        val normalizedUrl = normalizeBaseUrl(baseUrl)

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .addHeader("X-Api-Key", apiKey)
                    .build()
                chain.proceed(request)
            }

        // Für Produktion kannst du das Logging reduzieren oder entfernen
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        clientBuilder.addInterceptor(logging)

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(clientBuilder.build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(LidarrApi::class.java)
    }

    /**
     * Stellt sicher, dass die Base-URL mit /api/v1/ endet.
     * Der User kann eingeben:
     *   http://192.168.1.10:8686
     *   http://192.168.1.10:8686/
     *   http://192.168.1.10:8686/api/v1
     *   http://192.168.1.10:8686/api/v1/
     * Alle werden zu http://192.168.1.10:8686/api/v1/ normalisiert.
     */
    private fun normalizeBaseUrl(url: String): String {
        var u = url.trim()
        if (!u.endsWith("/")) u += "/"
        if (!u.contains("/api/v1/")) {
            // Entferne trailing slash, füge /api/v1/ hinzu
            u = u.trimEnd('/') + "/api/v1/"
        }
        return u
    }
}

