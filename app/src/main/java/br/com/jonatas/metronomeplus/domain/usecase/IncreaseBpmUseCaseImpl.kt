package br.com.jonatas.metronomeplus.domain.usecase

class IncreaseBpmUseCaseImpl : IncreaseBpmUseCase {
    override fun invoke(actualBeatPerMinute: Int, value: Int): Int {
        var newBeatPerMinute = actualBeatPerMinute
        if (value > 0) {
            newBeatPerMinute =
                actualBeatPerMinute.plus(value)
                    .coerceAtMost(MAX_BEAT_PER_MINUTE)

        }
        return newBeatPerMinute
    }

    companion object {
        const val MAX_BEAT_PER_MINUTE = 600
    }
}