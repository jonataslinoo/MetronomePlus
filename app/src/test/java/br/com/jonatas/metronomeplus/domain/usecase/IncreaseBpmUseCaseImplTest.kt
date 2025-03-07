package br.com.jonatas.metronomeplus.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class IncreaseBpmUseCaseImplTest {

    private val increaseBpmUseCase = IncreaseBpmUseCaseImpl()

    @Test
    fun `should increase the bpm when receive a value`() {
        val bpm = 120
        val increase = 20
        val expectedBpm = 140

        val actualBpm = increaseBpmUseCase(bpm, increase)

        assertEquals(expectedBpm, actualBpm)
    }

    @Test
    fun `should increase the bpm to the maximum value when receive a value greater than the maximum value`() {
        val bpm = 150
        val increase = 700
        val expectedBpm = 600

        val actualBpm = increaseBpmUseCase(bpm, increase)

        assertEquals(expectedBpm, actualBpm)
    }

    @Test
    fun `should not increase the bpm when receive a negative value`() {
        val bpm = 120
        val increase = -20
        val expectedBpm = 120

        val actualBpm = increaseBpmUseCase(bpm, increase)

        assertEquals(expectedBpm, actualBpm)
    }
}