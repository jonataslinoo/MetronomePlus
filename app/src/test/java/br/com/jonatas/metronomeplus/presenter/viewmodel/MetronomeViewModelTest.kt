package br.com.jonatas.metronomeplus.presenter.viewmodel

import android.content.res.AssetManager
import br.com.jonatas.metronomeplus.domain.engine.MetronomeEngine
import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.BeatState
import br.com.jonatas.metronomeplus.domain.model.Measure
import br.com.jonatas.metronomeplus.domain.provider.AssetProvider
import br.com.jonatas.metronomeplus.domain.provider.AudioSettingsProvider
import br.com.jonatas.metronomeplus.domain.usecase.GetMeasureUseCase
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
import org.mockito.ArgumentMatchers.anyInt
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
    private lateinit var mockAssetProvider: AssetProvider

    @Mock
    private lateinit var mockAudioSettingProvider: AudioSettingsProvider

    @Mock
    private lateinit var mockGetMeasureUseCase: GetMeasureUseCase

    private val viewModel by lazy {
        MetronomeViewModel(
            mockMetronomeEngine,
            mockAssetProvider,
            mockAudioSettingProvider,
            mockGetMeasureUseCase,
            testDispatcher
        )
    }
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        val mockAssetManager = mock(AssetManager::class.java)
        `when`(mockAssetProvider.getAssets()).thenReturn(mockAssetManager)
        `when`(mockAudioSettingProvider.getSampleRate()).thenReturn(48000)
        `when`(mockAudioSettingProvider.getFramesPerBurst()).thenReturn(256)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should start metronome in Loading state when MetronomeViewModel is initialized`() =
        runTest(testDispatcher) {
            val expectedState = MetronomeViewModel.MetronomeState.Loading
            assertEquals(expectedState, viewModel.uiState.first())

            verify(mockMetronomeEngine).initialize(mockAssetProvider.getAssets())
            verify(mockMetronomeEngine).setDefaultStreamValues(
                mockAudioSettingProvider.getSampleRate(),
                mockAudioSettingProvider.getFramesPerBurst()
            )
            verify(mockGetMeasureUseCase, never()).invoke()
            verify(mockMetronomeEngine, never()).setBpm(anyInt())
            verify(mockMetronomeEngine, never()).setBeats(anyList<Beat>().toTypedArray())
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

            verify(mockMetronomeEngine).initialize(mockAssetProvider.getAssets())
            verify(mockMetronomeEngine).setDefaultStreamValues(
                mockAudioSettingProvider.getSampleRate(),
                mockAudioSettingProvider.getFramesPerBurst()
            )
            verify(mockGetMeasureUseCase).invoke()
        }

    @Test
    fun `should transition to Error state when data loading fails`() = runTest(testDispatcher) {
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

        verify(mockMetronomeEngine).initialize(mockAssetProvider.getAssets())
        verify(mockMetronomeEngine).setDefaultStreamValues(
            mockAudioSettingProvider.getSampleRate(),
            mockAudioSettingProvider.getFramesPerBurst()
        )
        verify(mockGetMeasureUseCase).invoke()
        verify(mockMetronomeEngine, never()).setBpm(anyInt())
        verify(mockMetronomeEngine, never()).setBeats(anyList<Beat>().toTypedArray())
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
    fun `should increase bpm when increaseBpm is called with a positive value`() =
        runTest(testDispatcher) {
            val initialBpm = 120
            val expectedBpm = 130
            val increment = 10
            val initialMeasure = Measure(bpm = initialBpm, beats = listOf())
            `when`(mockGetMeasureUseCase()).thenReturn(initialMeasure)

            viewModel.increaseBpm(increment)
            advanceUntilIdle()

            val stateReady = viewModel.uiState.first()
            assertEquals(
                expectedBpm,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.bpm
            )

            verify(mockMetronomeEngine).setBpm(expectedBpm)
        }

    @Test
    fun `should not increase bpm when increaseBpm is called with a negative value`() =
        runTest(testDispatcher) {
            val initialBpm = 120
            val expectedBpm = 120
            val increment = -10
            val initialMeasure = Measure(bpm = initialBpm, beats = listOf())
            `when`(mockGetMeasureUseCase()).thenReturn(initialMeasure)

            viewModel.increaseBpm(increment)
            advanceUntilIdle()

            val stateReady = viewModel.uiState.first()
            assertEquals(
                expectedBpm,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.bpm
            )

            verify(mockMetronomeEngine, never()).setBpm(expectedBpm)
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
            val expectedMeasureUiModel =
                initialMeasure.copy(beats = initialMeasure.beats + Beat(BeatState.Normal))
                    .toUiModel()
            `when`(mockGetMeasureUseCase()).thenReturn(initialMeasure)

            viewModel.addBeat()
            advanceUntilIdle()

            val stateReady = viewModel.uiState.first()

            assertEquals(
                expectedMeasureUiModel.beats,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.beats
            )
            assertEquals(
                5,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.beats.size
            )

            verify(mockMetronomeEngine).setBeats(
                expectedMeasureUiModel.beats.toDomain().toTypedArray()
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
            val expectedMeasureUiModel = initialMeasure.copy().toUiModel()
            `when`(mockGetMeasureUseCase()).thenReturn(initialMeasure)

            viewModel.addBeat()
            advanceUntilIdle()

            val stateReady = viewModel.uiState.first()

            assertEquals(
                expectedMeasureUiModel.beats,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.beats
            )
            assertEquals(
                16,
                (stateReady as MetronomeViewModel.MetronomeState.Ready).measure.beats.size
            )

            verify(mockMetronomeEngine, never()).setBeats(anyList<Beat>().toTypedArray())
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
            val expectedBeats = initialMeasure.beats.toUiModel().toMutableList()
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
            verify(mockMetronomeEngine).setBeats(expectedBeats.toDomain().toTypedArray())
        }

    @Test
    fun `should not remove the beat if there is only one beat when removeBeat is called int the viewModel `() =
        runTest(testDispatcher) {
            val initialMeasure = Measure(
                bpm = 120, beats = listOf(Beat(BeatState.Accent))
            )
            val expectedBeats = initialMeasure.beats.toUiModel()
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
            verify(mockMetronomeEngine, never()).setBeats(expectedBeats.toDomain().toTypedArray())
        }
}