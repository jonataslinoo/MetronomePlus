package br.com.jonatas.metronomeplus.domain.engine

import br.com.jonatas.metronomeplus.data.model.BeatDto
import br.com.jonatas.metronomeplus.data.model.MeasureDto

interface MetronomeEngine {
    fun initialize(measureDto: MeasureDto)
    fun setBpm(bpm: Int)
    fun setBeats(beats: Array<BeatDto>)
    fun startPlaying()
    fun stopPlaying()
    fun cleanup()
}