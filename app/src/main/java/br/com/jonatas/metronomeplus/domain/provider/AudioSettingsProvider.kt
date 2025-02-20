package br.com.jonatas.metronomeplus.domain.provider

interface AudioSettingsProvider {
    fun getSampleRate(): Int
    fun getFramesPerBurst(): Int
}