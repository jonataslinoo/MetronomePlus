package br.com.jonatas.metronomeplus.data.provider

import br.com.jonatas.metronomeplus.domain.model.Measure

interface MeasureRepository {
    val getMeasure: Measure
}