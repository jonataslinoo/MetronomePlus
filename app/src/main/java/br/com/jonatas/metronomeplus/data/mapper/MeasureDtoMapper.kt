package br.com.jonatas.metronomeplus.data.mapper

import br.com.jonatas.metronomeplus.data.model.BeatDto
import br.com.jonatas.metronomeplus.data.model.MeasureDto
import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.Measure

fun MeasureDto.toDomain() = Measure(
    bpm = bpm,
    beats = beats.toDomain()
)

fun List<BeatDto>.toDomain(): List<Beat> = map { it.toDomain() }.toList()