package br.com.jonatas.metronomeplus.domain.source

import br.com.jonatas.metronomeplus.data.model.MeasureDto

interface MeasureDataSource {
    suspend fun getMeasure(): MeasureDto
}