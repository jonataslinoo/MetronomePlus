package br.com.jonatas.metronomeplus.data.provider

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager

class AudioSettingProviderImpl(private val context: Context) : AudioSettingsProvider {
    private val myAudioMgr = context.getSystemService(AUDIO_SERVICE) as AudioManager

    override fun getSampleRate(): Int {
        return myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE).toInt()
    }

    override fun getFramesPerBurst(): Int {
        return myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER).toInt()
    }
}
