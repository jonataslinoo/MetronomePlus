package br.com.jonatas.metronomeplus.data.mapper

import br.com.jonatas.metronomeplus.data.model.BeatDto
import br.com.jonatas.metronomeplus.domain.model.Beat

/** Domain for Dto */
fun Beat.toDto(): BeatDto = BeatDto(stateDto = state.toDto())

fun List<Beat>.toDtoArray(): Array<BeatDto> = map { it.toDto() }.toTypedArray()

/** Dto for Domain */
fun BeatDto.toDomain(): Beat = Beat(state = stateDto.toDomain())

fun List<BeatDto>.toDomainList(): List<Beat> = map { it.toDomain() }
