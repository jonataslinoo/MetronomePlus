package br.com.jonatas.metronomeplus.data.repository

import br.com.jonatas.metronomeplus.data.model.BeatDto
import br.com.jonatas.metronomeplus.data.model.BeatStateDto
import br.com.jonatas.metronomeplus.data.model.MeasureDto
import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.BeatState
import br.com.jonatas.metronomeplus.domain.model.Measure
import br.com.jonatas.metronomeplus.domain.source.MeasureDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify

class MeasureRepositoryImplTest {

    @Mock
    private lateinit var mockDataSource: MeasureDataSource
    private val measureRepository by lazy {
        MeasureRepositoryImpl(mockDataSource)
    }

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should return measure when getMeasure is called`() = runTest {
        val measureDto = MeasureDto(
            bpm = 120,
            mutableListOf(
                BeatDto(BeatStateDto.Accent),
                BeatDto(BeatStateDto.Normal),
                BeatDto(BeatStateDto.Normal),
                BeatDto(BeatStateDto.Normal),
            )
        )
        val expectedMeasure = Measure(
            bpm = 120,
            beats = mutableListOf(
                Beat(BeatState.Accent),
                Beat(BeatState.Normal),
                Beat(BeatState.Normal),
                Beat(BeatState.Normal),
            )
        )

        `when`(mockDataSource.getMeasure()).thenReturn(measureDto)

        val actualMeasure = measureRepository.getMeasure()

        assertEquals(expectedMeasure, actualMeasure)
        verify(mockDataSource).getMeasure()
    }

    @Test
    fun `should propagate exception from DataSource when getMeasure fails`() = runTest {
        val expectedError = "Data loading failure"
        `when`(mockDataSource.getMeasure()).thenThrow(RuntimeException("Data loading failure"))

        var caughtException: Throwable? = null
        try {
            measureRepository.getMeasure()
        } catch (e: Throwable) {
            caughtException = e
        }

        val exception = assertThrows(RuntimeException::class.java) {
            caughtException?.let { throw it }
        }

        assertEquals(expectedError, exception.message)
        verify(mockDataSource).getMeasure()
    }
}