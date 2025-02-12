package br.com.jonatas.metronomeplus.domain.model

data class Beat(val state: BeatState)

enum class BeatState {
    Normal,
    Silence,
    Accent,
    Medium
}