package br.com.jonatas.metronomeplus.data.repository

import br.com.jonatas.metronomeplus.data.mapper.toDomain
import br.com.jonatas.metronomeplus.domain.model.Measure
import br.com.jonatas.metronomeplus.domain.repository.MeasureRepository
import br.com.jonatas.metronomeplus.domain.source.MeasureDataSource

class MeasureRepositoryImpl(
    private val measureDataSource: MeasureDataSource
) : MeasureRepository {
    override suspend fun getMeasure(): Measure {
        return measureDataSource.getMeasure().toDomain()
    }
}