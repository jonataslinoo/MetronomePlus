package br.com.jonatas.metronomeplus.domain.usecase

interface SetBpmUseCase {
    operator fun invoke(value: Int): Int
}