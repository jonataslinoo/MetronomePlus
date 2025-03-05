package br.com.jonatas.metronomeplus.domain.usecase

interface DecreaseBpmUseCase {
    operator fun invoke(actualBeatPerMinute: Int, value: Int): Int
}