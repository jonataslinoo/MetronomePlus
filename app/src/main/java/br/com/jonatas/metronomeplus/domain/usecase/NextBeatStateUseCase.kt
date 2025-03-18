package br.com.jonatas.metronomeplus.domain.usecase

import br.com.jonatas.metronomeplus.domain.model.Beat

interface NextBeatStateUseCase {
    operator fun invoke(index: Int, beatsUi: List<Beat>): List<Beat>
}