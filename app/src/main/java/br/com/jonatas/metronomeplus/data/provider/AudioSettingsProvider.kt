package br.com.jonatas.metronomeplus.data.provider

interface AudioSettingsProvider {
    fun getSampleRate(): Int
    fun getFramesPerBurst(): Int
}