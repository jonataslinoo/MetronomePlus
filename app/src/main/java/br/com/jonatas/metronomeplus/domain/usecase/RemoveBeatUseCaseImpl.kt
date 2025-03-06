package br.com.jonatas.metronomeplus.domain.usecase

import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.BeatState

class RemoveBeatUseCaseImpl : RemoveBeatUseCase {
    override fun invoke(beats: List<Beat>): List<Beat> {
        if (beats.size == MIN_BEATS) {
            return beats.toList()
        }

        val newBeats = beats.toMutableList()
        newBeats.removeAt(newBeats.lastIndex)

        return newBeats
    }

    companion object {
        const val MIN_BEATS = 1
    }
}