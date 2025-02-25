package br.com.jonatas.metronomeplus.domain.usecase

import br.com.jonatas.metronomeplus.domain.model.Measure

interface GetMeasureUseCase {
    suspend operator fun invoke(): Measure
}