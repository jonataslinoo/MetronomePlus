package br.com.jonatas.metronomeplus.domain.usecase

import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.BeatState

class NextBeatStateUseCaseImpl : NextBeatStateUseCase {
    override fun invoke(index: Int, beats: List<Beat>): List<Beat> {
        val beat = beats[index]

        val nextState = when (beat.state) {
            BeatState.Normal -> {
                BeatState.Silence
            }

            BeatState.Silence -> {
                BeatState.Accent
            }

            BeatState.Accent -> {
                BeatState.Medium
            }

            BeatState.Medium -> {
                BeatState.Normal
            }
        }

        val newList = beats.toMutableList().apply {
            this[index] = beat.copy(state = nextState)
        }

        return newList
    }
}