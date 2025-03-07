package br.com.jonatas.metronomeplus.domain.usecase

import br.com.jonatas.metronomeplus.domain.model.Measure
import br.com.jonatas.metronomeplus.domain.repository.MeasureRepository

class GetMeasureUseCaseImpl(
    private val repository: MeasureRepository
) : GetMeasureUseCase {
    override suspend fun invoke(): Measure {
        return repository.getMeasure()
    }
}