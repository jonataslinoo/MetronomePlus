package br.com.jonatas.metronomeplus.data.mapper

import br.com.jonatas.metronomeplus.data.model.BeatStateDto
import br.com.jonatas.metronomeplus.domain.model.BeatState

fun BeatState.toDto(): BeatStateDto = when (this) {
    BeatState.Normal -> BeatStateDto.Normal
    BeatState.Silence -> BeatStateDto.Silence
    BeatState.Accent -> BeatStateDto.Accent
    BeatState.Medium -> BeatStateDto.Medium
}

fun BeatStateDto.toDomain(): BeatState = when (this) {
    BeatStateDto.Normal -> BeatState.Normal
    BeatStateDto.Silence -> BeatState.Silence
    BeatStateDto.Accent -> BeatState.Accent
    BeatStateDto.Medium -> BeatState.Medium
}
