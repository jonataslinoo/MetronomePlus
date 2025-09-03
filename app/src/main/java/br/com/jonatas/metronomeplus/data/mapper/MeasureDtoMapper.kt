package br.com.jonatas.metronomeplus.data.mapper

import br.com.jonatas.metronomeplus.data.model.MeasureDto
import br.com.jonatas.metronomeplus.domain.model.Measure

/** Domain for Dto */
fun Measure.toDto(): MeasureDto = MeasureDto(
    bpm = bpm,
    beats = beats.map { it.toDto() }
)

/** Dto for Domain */
fun MeasureDto.toDomain(): Measure = Measure(
    bpm = bpm,
    beats = beats.map { it.toDomain() }
)
