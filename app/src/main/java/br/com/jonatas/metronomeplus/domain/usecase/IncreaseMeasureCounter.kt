package br.com.jonatas.metronomeplus.domain.usecase

interface IncreaseMeasureCounter {
    operator fun invoke(index: Int, measureCount: Int): Int
}