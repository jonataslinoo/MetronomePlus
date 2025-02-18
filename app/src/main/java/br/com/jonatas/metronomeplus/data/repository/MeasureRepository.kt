package br.com.jonatas.metronomeplus.data.repository

import br.com.jonatas.metronomeplus.domain.model.Measure

interface MeasureRepository {
    val getMeasure: Measure
}