package com.example.arrdroid.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
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

    @GET("album/lookup")
    suspend fun searchAlbums(
        @Query("term") query: String
    ): List<AlbumDto>

    @POST("command")
    suspend fun sendCommand(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    )
}

object LidarrApiFactory {

    /**
     * baseUrl sollte idealerweise HTTPS sein, damit der API-Key
     * auf dem Transportweg nicht mitgelesen werden kann.
     */
    fun create(baseUrl: String, apiKey: String): LidarrApi {
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
            level = HttpLoggingInterceptor.Level.BASIC
        }
        clientBuilder.addInterceptor(logging)

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(clientBuilder.build())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        return retrofit.create(LidarrApi::class.java)
    }
}

