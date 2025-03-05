package br.com.jonatas.metronomeplus.presenter.mapper

import br.com.jonatas.metronomeplus.domain.model.Measure
import br.com.jonatas.metronomeplus.presenter.model.MeasureUiModel

/** Domain for UI */
fun Measure.toUiModel(): MeasureUiModel = MeasureUiModel(
    isPlaying = false,
    bpm = bpm,
    beats = beats.map { it.toUiModel() }.toList()
)

/** Ui for Domain */

