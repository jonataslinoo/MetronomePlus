package br.com.jonatas.metronomeplus.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FolderDto(
    val id: String,
    val name: String,
    val musics: Int,
    val date: Long,
    val isDefault: Boolean = false
)