package br.com.jonatas.metronomeplus.presenter.mapper

import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.presenter.model.BeatUiModel

fun Beat.toUiModel(): BeatUiModel = BeatUiModel(stateUiModel = state.toUiModel())

fun BeatUiModel.toDomain(): Beat = Beat(state = stateUiModel.toDomain())
