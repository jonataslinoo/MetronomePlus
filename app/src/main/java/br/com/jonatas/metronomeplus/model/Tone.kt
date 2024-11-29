package br.com.jonatas.metronomeplus.model

data class Tone(val state: ToneState)

enum class ToneState {
    Normal,
    Silence,
    Accent,
    Medium
}