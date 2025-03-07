package br.com.jonatas.metronomeplus.domain.usecase

import br.com.jonatas.metronomeplus.domain.model.Beat

interface AddBeatUseCase {
    operator fun invoke(beats: List<Beat>): List<Beat>
}