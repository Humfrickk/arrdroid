package com.example.arrdroid.data

data class ArtistDto(
    val id: Int,
    val artistName: String?
)

data class AlbumDto(
    val id: Int,
    val title: String,
    val artistId: Int,
    val artist: ArtistDto?,
    val monitored: Boolean
)

data class PagedResult<T>(
    val page: Int,
    val pageSize: Int,
    val sortKey: String?,
    val sortDirection: String?,
    val totalRecords: Int,
    val records: List<T>
)

