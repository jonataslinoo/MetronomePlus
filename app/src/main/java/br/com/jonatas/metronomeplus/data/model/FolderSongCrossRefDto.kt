package br.com.jonatas.metronomeplus.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FolderSongCrossRefDto(
    val folderId: String,
    val songId: String
)