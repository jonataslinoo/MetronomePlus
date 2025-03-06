package br.com.jonatas.metronomeplus.domain.usecase

interface TogglePlayPauseUseCase {
    operator fun invoke(isPlaying: Boolean): Boolean
}