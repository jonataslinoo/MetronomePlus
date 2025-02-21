package br.com.jonatas.metronomeplus.data.mapper

import br.com.jonatas.metronomeplus.data.model.BeatStateDto
import br.com.jonatas.metronomeplus.domain.model.BeatState
import br.com.jonatas.metronomeplus.presenter.model.BeatStateUiModel

fun BeatStateDto.toDomain() = when (this) {
    BeatStateDto.Normal -> BeatState.Normal
    BeatStateDto.Silence -> BeatState.Silence
    BeatStateDto.Accent -> BeatState.Accent
    BeatStateDto.Medium -> BeatState.Medium
}

fun BeatState.toUiModel() = when (this) {
    BeatState.Normal -> BeatStateUiModel.Normal
    BeatState.Silence -> BeatStateUiModel.Silence
    BeatState.Accent -> BeatStateUiModel.Accent
    BeatState.Medium -> BeatStateUiModel.Medium
}