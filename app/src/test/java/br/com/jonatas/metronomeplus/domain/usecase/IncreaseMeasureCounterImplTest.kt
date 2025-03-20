package br.com.jonatas.metronomeplus.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class IncreaseMeasureCounterImplTest {

    private val increaseMeasureCounter = IncreaseMeasureCounterImpl()

    @Test
    fun `should increase measure counter when index is zero`() {
        val index = 0
        val initialMeasureCount = 0
        val expectedMeasureCount = 1

        val actualMeasureCount = increaseMeasureCounter(index, initialMeasureCount)

        assertEquals(expectedMeasureCount, actualMeasureCount)
    }

    @Test
    fun `should not increase measure counter when index is different from zero`() {
        val index = 1
        val initialMeasureCount = 0
        val expectedMeasureCount = 0

        val actualMeasureCount = increaseMeasureCounter(index, initialMeasureCount)

        assertEquals(expectedMeasureCount, actualMeasureCount)
    }

    @Test
    fun `should increase measure counter when index is zero and measure count is greater than zero`() {
        val index = 0
        val initialMeasureCount = 2
        val expectedMeasureCount = 3

        val actualMeasureCount = increaseMeasureCounter(index, initialMeasureCount)

        assertEquals(expectedMeasureCount, actualMeasureCount)
    }

    @Test
    fun `should not increase the measure counter when the index and measurement count are different from zero`() {
        val index = 1
        val measureCount = 5
        val expectedMeasureCount = 5

        val actualMeasureCount = increaseMeasureCounter(index, measureCount)

        assertEquals(expectedMeasureCount, actualMeasureCount)
    }
}