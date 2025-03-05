package br.com.jonatas.metronomeplus.data.engine

import android.content.res.AssetManager
import br.com.jonatas.metronomeplus.data.model.BeatDto
import br.com.jonatas.metronomeplus.data.model.MeasureDto
import br.com.jonatas.metronomeplus.domain.engine.MetronomeEngine
import br.com.jonatas.metronomeplus.domain.provider.AssetProvider
import br.com.jonatas.metronomeplus.domain.provider.AudioSettingsProvider

class MetronomeEngineImpl(
    private val assetProvider: AssetProvider,
    private val audioSettingsProvider: AudioSettingsProvider
) : MetronomeEngine {

    override fun initialize(measureDto: MeasureDto) {
        native_onInit(assetManager = assetProvider.getAssets())
        native_setDefaultStreamValues(
            defaultSampleRate = audioSettingsProvider.getSampleRate(),
            defaultFramesPerBurst = audioSettingsProvider.getFramesPerBurst()
        )

        native_SetBPM(measureDto.bpm)
        native_SetBeats(measureDto.beats.toTypedArray())
    }

    override fun cleanup() = native_onEnd()
    override fun setBpm(bpm: Int) = native_SetBPM(bpm)
    override fun setBeats(beats: Array<BeatDto>) = native_SetBeats(beats)
    override fun startPlaying() = native_onStartPlaying()
    override fun stopPlaying() = native_onStopPlaying()

    private external fun native_onInit(assetManager: AssetManager)
    private external fun native_onEnd()
    private external fun native_SetBPM(bpm: Int)
    private external fun native_SetBeats(beats: Array<BeatDto>)
    private external fun native_onStartPlaying()
    private external fun native_onStopPlaying()
    private external fun native_setDefaultStreamValues(
        defaultSampleRate: Int,
        defaultFramesPerBurst: Int
    )

    companion object {
        private const val METRONOMEPLUS_LIB = "metronomeplus-lib"

        init {
            System.loadLibrary(METRONOMEPLUS_LIB)
        }
    }
}