package br.com.jonatas.metronomeplus.domain.usecase

import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.BeatState
import br.com.jonatas.metronomeplus.domain.model.Measure
import br.com.jonatas.metronomeplus.domain.repository.MeasureRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify


class GetMeasureUseCaseImplTest {

    private val mockMeasureRepository: MeasureRepository = mock()
    private val getMeasureUseCase = GetMeasureUseCaseImpl(mockMeasureRepository)

    @Test
    fun `should return a measure when it succeeds in returning the data`() = runTest {
        val expectedMeasure = Measure(
            bpm = 120,
            beats = listOf(
                Beat(BeatState.Accent),
                Beat(BeatState.Normal),
                Beat(BeatState.Normal),
                Beat(BeatState.Normal),
            )
        )
        `when`(mockMeasureRepository.getMeasure()).thenReturn(expectedMeasure)

        val actualMeasure = getMeasureUseCase()
        assertEquals(expectedMeasure, actualMeasure)

        verify(mockMeasureRepository).getMeasure()
    }

    @Test
    fun `should thrown an exception when it fails to return the data`() = runTest {
        val expectedMessageError = "Data Loading failure"
        `when`(mockMeasureRepository.getMeasure()).thenThrow(RuntimeException(expectedMessageError))

        try {
            getMeasureUseCase()
            fail("was supposed to throw an exception but failed")
        } catch (e: Exception) {
            assertEquals(expectedMessageError, e.message)
        }

        verify(mockMeasureRepository).getMeasure()
    }
}
