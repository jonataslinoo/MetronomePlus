package br.com.jonatas.metronomeplus.domain.usecase

import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.BeatState

class AddBeatUseCaseImpl : AddBeatUseCase {
    override fun invoke(beats: List<Beat>): List<Beat> {
        if (beats.size >= MAX_BEATS) {
            return beats.toList()
        }

        val newBeats = beats.toMutableList()
        newBeats.add(Beat(BeatState.Normal))

        return newBeats
    }

    companion object {
        const val MAX_BEATS = 16
    }
}