package com.example.arrdroid.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageDto(
    val coverType: String?,
    val url: String?,
    val remoteUrl: String?
)

@JsonClass(generateAdapter = true)
data class StatisticsDto(
    val albumCount: Int?,
    val trackFileCount: Int?,
    val trackCount: Int?,
    val totalTrackCount: Int?,
    val sizeOnDisk: Long?,
    val percentOfTracks: Double?
)

@JsonClass(generateAdapter = true)
data class ArtistDto(
    val id: Int,
    val artistName: String?,
    val status: String?,
    val overview: String?,
    val monitored: Boolean?,
    val qualityProfileId: Int?,
    val images: List<ImageDto>?,
    val statistics: StatisticsDto?
)

@JsonClass(generateAdapter = true)
data class AlbumDto(
    val id: Int,
    val title: String,
    val artistId: Int,
    val artist: ArtistDto?,
    val monitored: Boolean,
    val releaseDate: String?,
    val images: List<ImageDto>?
)

data class PagedResult<T>(
    val page: Int,
    val pageSize: Int,
    val sortKey: String?,
    val sortDirection: String?,
    val totalRecords: Int,
    val records: List<T>
)

@JsonClass(generateAdapter = true)
data class SystemStatusDto(
    val version: String?,
    val appName: String?,
    val startupPath: String?
)

@JsonClass(generateAdapter = true)
data class QueueItemDto(
    val id: Int,
    val artistId: Int?,
    val albumId: Int?,
    val title: String?,
    val status: String?,
    val trackedDownloadStatus: String?,
    val trackedDownloadState: String?,
    val statusMessages: List<StatusMessageDto>?,
    val sizeleft: Double?,
    val size: Double?,
    val timeleft: String?,
    val estimatedCompletionTime: String?,
    val protocol: String?,
    val downloadClient: String?,
    val outputPath: String?
)

@JsonClass(generateAdapter = true)
data class StatusMessageDto(
    val title: String?,
    val messages: List<String>?
)

@JsonClass(generateAdapter = true)
data class CommandResponseDto(
    val id: Int?,
    val name: String?,
    val commandName: String?,
    val status: String?,
    val started: String?
)

