package br.com.jonatas.metronomeplus.data.repository

import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.BeatState
import br.com.jonatas.metronomeplus.domain.model.Measure

class MeasureRepositoryImpl() : MeasureRepository {
    override val getMeasure: Measure
        get() = measure

    private var measure: Measure = Measure(
        isPlaying = false,
        bpm = 120,
        beats = mutableListOf(
            Beat(BeatState.Accent),
            Beat(BeatState.Normal),
            Beat(BeatState.Normal),
            Beat(BeatState.Normal),
        )
    )
}