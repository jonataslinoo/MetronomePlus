package br.com.jonatas.metronomeplus.domain.usecase

class IncreaseMeasureCounterImpl : IncreaseMeasureCounter {
    override fun invoke(index: Int, measureCount: Int): Int {
        var count = measureCount

        if (index == 0) {
            count += 1
        }

        return count
    }
}