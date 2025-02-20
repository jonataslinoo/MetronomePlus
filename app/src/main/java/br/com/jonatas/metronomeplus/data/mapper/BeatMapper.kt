package br.com.jonatas.metronomeplus.data.mapper

import br.com.jonatas.metronomeplus.data.model.BeatDto
import br.com.jonatas.metronomeplus.domain.model.Beat

fun BeatDto.toDomain() =
    Beat(state = this.stateDto.toDomain())