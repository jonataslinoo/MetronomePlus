package br.com.jonatas.metronomeplus.data.mapper

import br.com.jonatas.metronomeplus.data.model.BeatDto
import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.presenter.model.BeatUiModel

fun BeatDto.toDomain() =
    Beat(state = this.stateDto.toDomain())

fun Beat.toUiModel() =
    BeatUiModel(stateUiModel = this.state.toUiModel())