package br.com.jonatas.metronomeplus.presenter.viewmodel

import br.com.jonatas.metronomeplus.data.mapper.toDto
import br.com.jonatas.metronomeplus.data.mapper.toDtoArray
import br.com.jonatas.metronomeplus.data.model.MeasureDto
import br.com.jonatas.metronomeplus.domain.engine.MetronomeEngine
import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.BeatState
import br.com.jonatas.metronomeplus.domain.model.Measure
import br.com.jonatas.metronomeplus.domain.usecase.AddBeatUseCase
import br.com.jonatas.metronomeplus.domain.usecase.DecreaseBpmUseCase
import br.com.jonatas.metronomeplus.domain.usecase.GetMeasureUseCase
import br.com.jonatas.metronomeplus.domain.usecase.IncreaseBpmUseCase
import br.com.jonatas.metronomeplus.domain.usecase.RemoveBeatUseCase
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModel
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModelList
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

    @Mock
    private lateinit var mockDecreaseBpmUseCase: DecreaseBpmUseCase

    @Mock
    private lateinit var mockAddBeatUseCase: AddBeatUseCase

    @Mock
    private lateinit var mockRemoveBeatUseCase: RemoveBeatUseCase
    private val testDispatcher = StandardTestDispatcher()

    private val viewModel by lazy {
        MetronomeViewModel(
            mockMetronomeEngine,
            mockGetMeasureUseCase,
            mockIncreaseBpmUseCase,
            mockDecreaseBpmUseCase,
            mockAddBeatUseCase,
            mockRemoveBeatUseCase,
            testDispatcher
        )
    }

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
    fun `should call decreaseBpmUseCase when decreaseBpm is called with a value`() =
        runTest(testDispatcher) {
            val initialBpm = 120
            val expectedBpm = 100
            val decrement = 20

            val initialMeasure = Measure(bpm = initialBpm, beats = listOf())
            `when`(mockGetMeasureUseCase()).thenReturn(initialMeasure)
            `when`(mockDecreaseBpmUseCase(initialBpm, decrement)).thenReturn(expectedBpm)

            viewModel.decreaseBpm(decrement)
            advanceUntilIdle()

            val stateReady = viewModel.uiState.first()
            assertTrue(stateReady is MetronomeViewModel.MetronomeState.Ready)
            assertEquals(
                expectedBpm,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.bpm
            )

            verify(mockDecreaseBpmUseCase).invoke(initialBpm, decrement)
            verify(mockMetronomeEngine).setBpm(expectedBpm)
        }

    @Test
    fun `should call AddBeatUseCase when addBeat is called`() =
        runTest(testDispatcher) {
            val initialMeasure = Measure(
                bpm = 120,
                beats = listOf(Beat(BeatState.Accent))
            )
            val expectedMeasure =
                initialMeasure.copy(beats = initialMeasure.beats + Beat(BeatState.Normal))
            `when`(mockGetMeasureUseCase()).thenReturn(initialMeasure)
            `when`(mockAddBeatUseCase(initialMeasure.beats)).thenReturn(expectedMeasure.beats)

            viewModel.addBeat()
            advanceUntilIdle()

            val stateReady = viewModel.uiState.first()
            assertTrue(stateReady is MetronomeViewModel.MetronomeState.Ready)
            assertEquals(
                expectedMeasure.toUiModel().beats.size,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.beats.size
            )
            assertEquals(
                expectedMeasure.toUiModel().beats,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.beats
            )

            verify(mockAddBeatUseCase).invoke(initialMeasure.beats)
            verify(mockMetronomeEngine).setBeats(expectedMeasure.beats.toDtoArray())
        }

    @Test
    fun `should remove last index from the beat list when removeBeat is called`() =
        runTest(testDispatcher) {
            val initialMeasure = Measure(
                bpm = 120,
                beats = listOf(
                    Beat(BeatState.Accent),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal),
                    Beat(BeatState.Normal)
                )
            )
            val expectedBeats =
                initialMeasure.beats.toMutableList().apply {
                    removeAt(initialMeasure.beats.lastIndex)
                }
            `when`(mockGetMeasureUseCase()).thenReturn(initialMeasure)
            `when`(mockRemoveBeatUseCase(initialMeasure.beats)).thenReturn(expectedBeats)

            viewModel.removeBeat()
            advanceUntilIdle()

            val stateReady = viewModel.uiState.first()
            assertTrue(stateReady is MetronomeViewModel.MetronomeState.Ready)
            assertEquals(
                expectedBeats.toUiModelList(),
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.beats
            )
            assertEquals(
                expectedBeats.size,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.beats.size
            )

            verify(mockRemoveBeatUseCase).invoke(initialMeasure.beats)
            verify(mockMetronomeEngine).setBeats(expectedBeats.toDtoArray())
        }
}