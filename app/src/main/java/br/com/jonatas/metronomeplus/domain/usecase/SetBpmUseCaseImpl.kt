package br.com.jonatas.metronomeplus.domain.usecase

import br.com.jonatas.metronomeplus.domain.util.MAX_BPM_VALUE
import br.com.jonatas.metronomeplus.domain.util.MIN_BPM_VALUE

class SetBpmUseCaseImpl : SetBpmUseCase {
    override fun invoke(value: Int): Int {
        return value.coerceIn(MIN_BPM_VALUE, MAX_BPM_VALUE)
    }
}