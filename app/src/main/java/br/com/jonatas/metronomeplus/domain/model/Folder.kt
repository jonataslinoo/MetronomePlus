package br.com.jonatas.metronomeplus.domain.model

data class Folder(
    val id: String,
    val name: String,
    val musics: Int,
    val date: Long,
    val isDefault: Boolean = false
)