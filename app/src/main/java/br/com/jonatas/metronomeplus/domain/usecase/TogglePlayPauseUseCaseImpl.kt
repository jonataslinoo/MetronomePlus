package br.com.jonatas.metronomeplus.domain.usecase

class TogglePlayPauseUseCaseImpl : TogglePlayPauseUseCase {
    override fun invoke(isPlaying: Boolean): Boolean {
        return !isPlaying
    }
}