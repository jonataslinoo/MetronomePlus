package br.com.jonatas.metronomeplus.presenter.mapper

import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.Measure
import br.com.jonatas.metronomeplus.presenter.model.BeatUiModel
import br.com.jonatas.metronomeplus.presenter.model.MeasureUiModel

/** Domain for UI */
fun Measure.toUiModel(): MeasureUiModel = MeasureUiModel(
    isPlaying = false,
    bpm = bpm,
    beats = beats.toUiModelList()
)

fun List<Beat>.toUiModelList(): List<BeatUiModel> = map { it.toUiModel() }

/** Ui for Domain */
fun MeasureUiModel.toDomain(): Measure = Measure(
    bpm = bpm,
    beats = beats.map { it.toDomain() }
)
