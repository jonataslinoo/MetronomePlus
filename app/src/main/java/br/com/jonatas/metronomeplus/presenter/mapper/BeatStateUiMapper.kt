package br.com.jonatas.metronomeplus.presenter.mapper

import br.com.jonatas.metronomeplus.domain.model.BeatState
import br.com.jonatas.metronomeplus.presenter.model.BeatStateUiModel

fun BeatState.toUiModel(): BeatStateUiModel = when (this) {
    BeatState.Normal -> BeatStateUiModel.Normal
    BeatState.Silence -> BeatStateUiModel.Silence
    BeatState.Accent -> BeatStateUiModel.Accent
    BeatState.Medium -> BeatStateUiModel.Medium
}

fun BeatStateUiModel.toDomain(): BeatState = when (this) {
    BeatStateUiModel.Normal -> BeatState.Normal
    BeatStateUiModel.Silence -> BeatState.Silence
    BeatStateUiModel.Accent -> BeatState.Accent
    BeatStateUiModel.Medium -> BeatState.Medium
}
