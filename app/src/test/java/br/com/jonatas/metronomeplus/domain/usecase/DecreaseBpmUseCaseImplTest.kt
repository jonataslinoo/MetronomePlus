package br.com.jonatas.metronomeplus.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class DecreaseBpmUseCaseImplTest {

    private val decreaseBpmUseCase = DecreaseBpmUseCaseImpl()

    @Test
    fun `should decrease the bpm when receive a value`() {
        val initialBpm = 120
        val decrease = 20
        val expectedBpm = 100

        val actualBpm = decreaseBpmUseCase(initialBpm, decrease)

        assertEquals(expectedBpm, actualBpm)
    }

    @Test
    fun `should lower the bpm to the minimum value when receive a value higher than the current value`() {
        val initialBpm = 120
        val decrease = 150
        val expectedBpm = 20

        val actualBpm = decreaseBpmUseCase(initialBpm, decrease)

        assertEquals(expectedBpm, actualBpm)
    }

    @Test
    fun `should do nothing when it receives a value of zero`() {
        val initialBpm = 120
        val decrease = 0
        val expectedBpm = 120

        val actualBpm = decreaseBpmUseCase(initialBpm, decrease)

        assertEquals(expectedBpm, actualBpm)
    }

    @Test
    fun `should not decrease bpm when receive a negative value`() {
        val bpm = 120
        val increase = -20
        val expectedBpm = 120

        val actualBpm = decreaseBpmUseCase(bpm, increase)

        assertEquals(expectedBpm, actualBpm)
    }
}