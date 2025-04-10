package br.com.jonatas.metronomeplus.data.mapper

import br.com.jonatas.metronomeplus.data.model.FolderDto
import br.com.jonatas.metronomeplus.domain.model.Folder

fun Folder.toDto(): FolderDto = FolderDto(
    id = id,
    name = name,
    musics = musics,
    date = date
)

fun List<FolderDto>.toDomainList(): List<Folder> = map { it.toDomain() }

fun FolderDto.toDomain(): Folder = Folder(
    id = id,
    name = name,
    musics = musics,
    date = date
)