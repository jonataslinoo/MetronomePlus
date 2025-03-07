package br.com.jonatas.metronomeplus.domain.usecase

interface IncreaseBpmUseCase {
    operator fun invoke(actualBeatPerMinute: Int, value: Int): Int
}