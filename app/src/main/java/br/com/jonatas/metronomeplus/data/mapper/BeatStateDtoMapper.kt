package br.com.jonatas.metronomeplus.data.mapper

import br.com.jonatas.metronomeplus.data.model.BeatStateDto
import br.com.jonatas.metronomeplus.domain.model.BeatState

fun BeatStateDto.toDomain() = when (this) {
    BeatStateDto.Normal -> BeatState.Normal
    BeatStateDto.Silence -> BeatState.Silence
    BeatStateDto.Accent -> BeatState.Accent
    BeatStateDto.Medium -> BeatState.Medium
}

fun BeatState.toDto() = when (this) {
    BeatState.Normal -> BeatStateDto.Normal
    BeatState.Silence -> BeatStateDto.Silence
    BeatState.Accent -> BeatStateDto.Accent
    BeatState.Medium -> BeatStateDto.Medium
}
