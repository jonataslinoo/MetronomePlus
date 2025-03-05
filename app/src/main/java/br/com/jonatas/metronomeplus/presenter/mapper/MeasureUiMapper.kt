package br.com.jonatas.metronomeplus.presenter.mapper

import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.Measure
import br.com.jonatas.metronomeplus.presenter.model.BeatUiModel
import br.com.jonatas.metronomeplus.presenter.model.MeasureUiModel

fun MeasureUiModel.toDomain(): Measure = Measure(
    bpm = bpm,
    beats = beats.toDomainList()
)

fun List<BeatUiModel>.toDomainList(): List<Beat> =
    map { it.toDomain() }.toList()

fun List<BeatUiModel>.toDomainArray(): Array<Beat> =
    map { it.toDomain() }.toTypedArray()

fun Measure.toUiModel() = MeasureUiModel(
    isPlaying = false,
    bpm = bpm,
    beats = beats.toUiModelList()
)

fun List<Beat>.toUiModelList(): List<BeatUiModel> =
    map { it.toUiModel() }.toList()
