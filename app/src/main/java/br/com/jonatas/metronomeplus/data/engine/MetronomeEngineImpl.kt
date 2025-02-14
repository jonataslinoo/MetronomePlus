package br.com.jonatas.metronomeplus.data.engine

import android.content.res.AssetManager
import br.com.jonatas.metronomeplus.domain.engine.MetronomeEngine
import br.com.jonatas.metronomeplus.domain.model.Beat

private const val METRONOMEPLUS_LIB = "metronomeplus-lib"

class MetronomeEngineImpl : MetronomeEngine {
    override fun initialize(assetManager: AssetManager) = native_onInit(assetManager)
    override fun cleanup() = native_onEnd()
    override fun setBpm(bpm: Int) = native_SetBPM(bpm)
    override fun setBeats(beats: Array<Beat>) = native_SetBeats(beats)
    override fun startPlaying() = native_onStartPlaying()
    override fun stopPlaying() = native_onStopPlaying()
    override fun setDefaultStreamValues(sampleRate: Int, framesPerBurst: Int) =
        native_setDefaultStreamValues(sampleRate, framesPerBurst)

    private external fun native_onInit(assetManager: AssetManager)
    private external fun native_onEnd()
    private external fun native_SetBPM(bpm: Int)
    private external fun native_SetBeats(beats: Array<Beat>)
    private external fun native_onStartPlaying()
    private external fun native_onStopPlaying()
    private external fun native_setDefaultStreamValues(
        defaultSampleRate: Int, defaultFramesPerBurst: Int
    )

    companion object {
        init {
            System.loadLibrary(METRONOMEPLUS_LIB)
        }
    }
}