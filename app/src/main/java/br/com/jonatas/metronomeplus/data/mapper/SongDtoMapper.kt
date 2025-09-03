package br.com.jonatas.metronomeplus.data.mapper

import br.com.jonatas.metronomeplus.data.model.SongDto
import br.com.jonatas.metronomeplus.domain.model.Song

/** Dto for Domain */
fun SongDto.toDomain(): Song = Song(
    id = id,
    title = title,
    artist = artist,
    bpm = bpm,
    timeSignature = timeSignature.toDomain(),
    beatPatterns = beatPatterns.toDomainList(),
)

fun List<SongDto>.toDomainList(): List<Song> = map { it.toDomain() }