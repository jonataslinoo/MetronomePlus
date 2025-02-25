package br.com.jonatas.metronomeplus.presenter.mapper

import br.com.jonatas.metronomeplus.data.mapper.toUiModel
import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.Measure
import br.com.jonatas.metronomeplus.presenter.model.BeatUiModel
import br.com.jonatas.metronomeplus.presenter.model.MeasureUiModel

fun MeasureUiModel.toDomain(): Measure = Measure(
    bpm = bpm,
    beats = beats.toDomain()
)

fun List<BeatUiModel>.toDomain(): List<Beat> = map { it.toDomain() }.toList()

fun Measure.toUiModel() = MeasureUiModel(
    isPlaying = false,
    bpm = bpm,
    beats = beats.toUiModel()
)

fun List<Beat>.toUiModel(): List<BeatUiModel> = map { it.toUiModel() }.toList()