package br.com.jonatas.metronomeplus.presenter.mapper

import br.com.jonatas.metronomeplus.domain.model.BeatState
import br.com.jonatas.metronomeplus.presenter.model.BeatStateUiModel

fun BeatStateUiModel.toDomain(): BeatState = when (this) {
    BeatStateUiModel.Normal -> BeatState.Normal
    BeatStateUiModel.Silence -> BeatState.Silence
    BeatStateUiModel.Accent -> BeatState.Accent
    BeatStateUiModel.Medium -> BeatState.Medium
}

fun BeatState.toUiModel() = when (this) {
    BeatState.Normal -> BeatStateUiModel.Normal
    BeatState.Silence -> BeatStateUiModel.Silence
    BeatState.Accent -> BeatStateUiModel.Accent
    BeatState.Medium -> BeatStateUiModel.Medium
}