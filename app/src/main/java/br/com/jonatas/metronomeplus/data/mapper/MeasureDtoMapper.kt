package br.com.jonatas.metronomeplus.data.mapper

import br.com.jonatas.metronomeplus.data.model.BeatDto
import br.com.jonatas.metronomeplus.data.model.MeasureDto
import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.Measure

fun MeasureDto.toDomain() = Measure(
    bpm = bpm,
    beats = beats.toDomainList()
)

fun List<BeatDto>.toDomainList(): List<Beat> =
    map { it.toDomain() }.toList()

fun List<BeatDto>.toDomainArray(): Array<Beat> =
    map { it.toDomain() }.toTypedArray()

fun Measure.toDto() = MeasureDto(
    bpm = bpm,
    beats = beats.toDtoList()
)

fun List<Beat>.toDtoList(): List<BeatDto> = map { it.toDto() }.toList()