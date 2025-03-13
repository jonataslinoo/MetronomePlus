package br.com.jonatas.metronomeplus.domain.engine

interface BeatChangeListener {
    fun onBeatChanged(index: Int)
}