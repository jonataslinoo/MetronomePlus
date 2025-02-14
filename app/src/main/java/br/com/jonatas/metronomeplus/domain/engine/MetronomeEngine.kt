package br.com.jonatas.metronomeplus.domain.engine

import android.content.res.AssetManager
import br.com.jonatas.metronomeplus.domain.model.Beat

interface MetronomeEngine {
    fun initialize(assetManager: AssetManager)
    fun setBpm(bpm: Int)
    fun setBeats(beats: Array<Beat>)
    fun startPlaying()
    fun stopPlaying()
    fun setDefaultStreamValues(sampleRate: Int, framesPerBurst: Int)
    fun cleanup()
}