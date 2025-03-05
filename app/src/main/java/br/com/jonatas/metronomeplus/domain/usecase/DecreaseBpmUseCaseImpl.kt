package br.com.jonatas.metronomeplus.domain.usecase


class DecreaseBpmUseCaseImpl : DecreaseBpmUseCase {
    override fun invoke(actualBeatPerMinute: Int, value: Int): Int {
        var newBeatPerMinute = actualBeatPerMinute
        if (value > 0) {
            newBeatPerMinute =
                actualBeatPerMinute.minus(value)
                    .coerceAtLeast(MIN_BEAT_PER_MINUTE)

        }
        return newBeatPerMinute
    }

    companion object {
        const val MIN_BEAT_PER_MINUTE = 20
    }
}