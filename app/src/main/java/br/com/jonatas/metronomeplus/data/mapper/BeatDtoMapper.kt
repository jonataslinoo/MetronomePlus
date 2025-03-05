package br.com.jonatas.metronomeplus.data.mapper

import br.com.jonatas.metronomeplus.data.model.BeatDto
import br.com.jonatas.metronomeplus.domain.model.Beat

fun Beat.toDto(): BeatDto = BeatDto(stateDto = state.toDto())

fun BeatDto.toDomain(): Beat = Beat(state = stateDto.toDomain())
