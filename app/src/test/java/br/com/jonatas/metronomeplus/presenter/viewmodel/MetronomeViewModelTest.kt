package br.com.jonatas.metronomeplus.presenter.viewmodel

import br.com.jonatas.metronomeplus.data.mapper.toDto
import br.com.jonatas.metronomeplus.data.mapper.toDtoArray
import br.com.jonatas.metronomeplus.data.model.BeatDto
import br.com.jonatas.metronomeplus.data.model.MeasureDto
import br.com.jonatas.metronomeplus.domain.engine.MetronomeEngine
import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.BeatState
import br.com.jonatas.metronomeplus.domain.model.Measure
import br.com.jonatas.metronomeplus.domain.usecase.GetMeasureUseCase
import br.com.jonatas.metronomeplus.domain.usecase.IncreaseBpmUseCase
import br.com.jonatas.metronomeplus.presenter.mapper.toDomain
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class MetronomeViewModelTest {

    @Mock
    private lateinit var mockMetronomeEngine: MetronomeEngine

    @Mock
    private lateinit var mockGetMeasureUseCase: GetMeasureUseCase

    @Mock
    private lateinit var mockIncreaseBpmUseCase: IncreaseBpmUseCase

    private val viewModel by lazy {
        MetronomeViewModel(
            mockMetronomeEngine,
            mockGetMeasureUseCase,
            mockIncreaseBpmUseCase,
            testDispatcher
        )
    }
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should start metronome in Loading state when MetronomeViewModel is initialized`() =
        runTest(testDispatcher) {
            val mockMeasureDto = mock(MeasureDto::class.java)
            val expectedState = MetronomeViewModel.MetronomeState.Loading
            assertEquals(expectedState, viewModel.uiState.first())

            verify(mockGetMeasureUseCase, never()).invoke()
            verify(mockMetronomeEngine, never()).initialize(mockMeasureDto)
        }

    @Test
    fun `should transition to Ready state when data loading is successful`() =
        runTest(testDispatcher) {
            val expectedMeasure = Measure(
                bpm = 120,
                beats = listOf(
                    Beat(BeatState.Accent),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                )
            )
            `when`(mockGetMeasureUseCase()).thenReturn(expectedMeasure)

            advanceUntilIdle()
            val stateLoading = viewModel.uiState.first()
            assertTrue(stateLoading is MetronomeViewModel.MetronomeState.Loading)

            advanceUntilIdle()
            val stateReady = viewModel.uiState.first()
            val expectedState =
                MetronomeViewModel.MetronomeState.Ready(measure = expectedMeasure.toUiModel())
            assertTrue(stateReady is MetronomeViewModel.MetronomeState.Ready)
            assertEquals(expectedState, stateReady)

            verify(mockMetronomeEngine).initialize(expectedMeasure.toDto())
            verify(mockGetMeasureUseCase).invoke()
        }

    @Test
    fun `should transition to Error state when data loading fails`() = runTest(testDispatcher) {
        val mockMeasureDto = mock(MeasureDto::class.java)
        val expectedErrorMessage = "Data Loading failure"
        `when`(mockGetMeasureUseCase()).thenThrow(RuntimeException(expectedErrorMessage))

        advanceUntilIdle()
        val stateLoading = viewModel.uiState.first()
        assertTrue(stateLoading is MetronomeViewModel.MetronomeState.Loading)

        advanceUntilIdle()
        val stateError = viewModel.uiState.first()
        val expectedState = MetronomeViewModel.MetronomeState.Error("Error: $expectedErrorMessage")
        assertTrue(stateError is MetronomeViewModel.MetronomeState.Error)
        assertEquals(expectedState, stateError)

        verify(mockGetMeasureUseCase).invoke()
        verify(mockMetronomeEngine, never()).initialize(mockMeasureDto)
    }

    @Test
    fun `should toggle isPlaying and play or pause the metronome engine when togglePlayPause is called`() =
        runTest(testDispatcher) {
            val initialMeasure = Measure(
                bpm = 120,
                beats = listOf(
                    Beat(BeatState.Accent),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                )
            )
            `when`(mockGetMeasureUseCase()).thenReturn(initialMeasure)

            viewModel.togglePlayPause()
            advanceUntilIdle()

            val stateReadyIsPlayingTrue = viewModel.uiState.first()
            assertTrue(stateReadyIsPlayingTrue is MetronomeViewModel.MetronomeState.Ready)
            assertEquals(
                true,
                (stateReadyIsPlayingTrue as MetronomeViewModel.MetronomeState.Ready).measure.isPlaying
            )

            verify(mockMetronomeEngine).startPlaying()

            viewModel.togglePlayPause()
            advanceUntilIdle()

            val stateReadyIsPlayingFalse = viewModel.uiState.first()
            assertTrue(stateReadyIsPlayingFalse is MetronomeViewModel.MetronomeState.Ready)
            assertEquals(
                false,
                (stateReadyIsPlayingFalse as MetronomeViewModel.MetronomeState.Ready).measure.isPlaying
            )

            verify(mockMetronomeEngine).stopPlaying()
        }

    @Test
    fun `should call increaseBpmUseCase when increaseBpm is called with a value`() =
        runTest(testDispatcher) {
            val initialBpm = 120
            val expectedBpm = 130
            val increment = 10

            val initialMeasure = Measure(bpm = initialBpm, beats = listOf())
            `when`(mockGetMeasureUseCase()).thenReturn(initialMeasure)
            `when`(mockIncreaseBpmUseCase(initialBpm, increment)).thenReturn(expectedBpm)

            viewModel.increaseBpm(increment)
            advanceUntilIdle()

            val stateReady = viewModel.uiState.first()
            assertTrue(stateReady is MetronomeViewModel.MetronomeState.Ready)
            assertEquals(
                expectedBpm,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.bpm
            )

            verify(mockIncreaseBpmUseCase).invoke(initialBpm, increment)
            verify(mockMetronomeEngine).setBpm(expectedBpm)
        }

    @Test
    fun `should decrease bpm when decreaseBpm is called with a negative value`() =
        runTest(testDispatcher) {
            val initialBpm = 120
            val expectedBpm = 110
            val increment = -10
            val initialMeasure = Measure(bpm = initialBpm, beats = listOf())
            `when`(mockGetMeasureUseCase()).thenReturn(initialMeasure)

            viewModel.decreaseBpm(increment)
            advanceUntilIdle()

            val stateReady = viewModel.uiState.first()
            assertEquals(
                expectedBpm,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.bpm
            )

            verify(mockMetronomeEngine).setBpm(expectedBpm)
        }

    @Test
    fun `should decrease bpm to zero when decreaseBpm is called with a value greater than the actual bpm `() =
        runTest(testDispatcher) {
            val initialBpm = 120
            val expectedBpm = 0
            val increment = -150
            val initialMeasure = Measure(bpm = initialBpm, beats = listOf())
            `when`(mockGetMeasureUseCase()).thenReturn(initialMeasure)

            viewModel.decreaseBpm(increment)
            advanceUntilIdle()

            val stateReady = viewModel.uiState.first()
            assertEquals(
                expectedBpm,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.bpm
            )

            verify(mockMetronomeEngine).setBpm(expectedBpm)
        }

    @Test
    fun `should not decrease bpm when decreaseBpm is called with a positive value `() =
        runTest(testDispatcher) {
            val initialBpm = 120
            val expectedBpm = 120
            val increment = 10
            val initialMeasure = Measure(bpm = initialBpm, beats = listOf())
            `when`(mockGetMeasureUseCase()).thenReturn(initialMeasure)

            viewModel.decreaseBpm(increment)
            advanceUntilIdle()

            val stateReady = viewModel.uiState.first()
            assertEquals(
                expectedBpm,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.bpm
            )

            verify(mockMetronomeEngine, never()).setBpm(expectedBpm)
        }

    @Test
    fun `should add a normal beat when addBeat is called in the viewModel`() =
        runTest(testDispatcher) {
            val initialMeasure = Measure(
                bpm = 120,
                beats = listOf(
                    Beat(BeatState.Accent),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                )
            )
            val expectedMeasure =
                initialMeasure.copy(beats = initialMeasure.beats + Beat(BeatState.Normal))
            `when`(mockGetMeasureUseCase()).thenReturn(initialMeasure)

            viewModel.addBeat()
            advanceUntilIdle()

            val stateReady = viewModel.uiState.first()

            assertEquals(
                expectedMeasure.toUiModel().beats,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.beats
            )
            assertEquals(
                5,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.beats.size
            )

            verify(mockMetronomeEngine).setBeats(
                expectedMeasure.toDto().beats.toTypedArray()
            )
        }

    @Test
    fun `should not add more than sixteen beats when addBeat is called in the viewModel`() =
        runTest(testDispatcher) {
            val initialMeasure = Measure(
                bpm = 0,
                beats = listOf(
                    Beat(BeatState.Accent),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                )
            )
            val expectedMeasure = initialMeasure.copy()
            `when`(mockGetMeasureUseCase()).thenReturn(initialMeasure)

            viewModel.addBeat()
            advanceUntilIdle()

            val stateReady = viewModel.uiState.first()

            assertEquals(
                expectedMeasure.toUiModel().beats,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.beats
            )
            assertEquals(
                16,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.beats.size
            )

            verify(mockMetronomeEngine, never()).setBeats(anyList<BeatDto>().toTypedArray())
        }

    @Test
    fun `should remove last index from the beat list when removeBeat is called in the viewModel`() =
        runTest(testDispatcher) {
            val initialMeasure = Measure(
                bpm = 120,
                beats = listOf(
                    Beat(BeatState.Accent),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                )
            )
            val expectedBeats = initialMeasure.toUiModel().beats.toMutableList()
            `when`(mockGetMeasureUseCase()).thenReturn(initialMeasure)

            viewModel.removeBeat()
            advanceUntilIdle()

            val stateReady = viewModel.uiState.first()
            expectedBeats.removeAt(initialMeasure.beats.lastIndex)

            assertEquals(
                expectedBeats,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.beats
            )
            assertEquals(
                3,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.beats.size
            )

            val domainBeats = expectedBeats.map { it.toDomain() }
            verify(mockMetronomeEngine).setBeats(domainBeats.toDtoArray())
        }

    @Test
    fun `should not remove the beat if there is only one beat when removeBeat is called int the viewModel `() =
        runTest(testDispatcher) {
            val initialMeasure = Measure(
                bpm = 120, beats = listOf(Beat(BeatState.Accent))
            )
            val expectedBeats = initialMeasure.toUiModel().beats
            `when`(mockGetMeasureUseCase()).thenReturn(initialMeasure)

            viewModel.removeBeat()
            advanceUntilIdle()

            val stateReady = viewModel.uiState.first()

            assertEquals(
                expectedBeats,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.beats
            )
            assertEquals(
                1,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.beats.size
            )

            val domainBeats = expectedBeats.map { it.toDomain() }
            verify(mockMetronomeEngine, never()).setBeats(domainBeats.toDtoArray())
        }
}