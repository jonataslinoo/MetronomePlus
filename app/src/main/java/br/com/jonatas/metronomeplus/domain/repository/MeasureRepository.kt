package br.com.jonatas.metronomeplus.domain.repository

import br.com.jonatas.metronomeplus.domain.model.Measure

interface MeasureRepository {
    suspend fun getMeasure(): Measure
}