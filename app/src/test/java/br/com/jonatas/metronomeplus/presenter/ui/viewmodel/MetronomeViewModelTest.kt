package br.com.jonatas.metronomeplus.presenter.ui.viewmodel

import android.content.res.AssetManager
import br.com.jonatas.metronomeplus.data.provider.AssetProvider
import br.com.jonatas.metronomeplus.data.provider.AudioSettingsProvider
import br.com.jonatas.metronomeplus.data.repository.MeasureRepository
import br.com.jonatas.metronomeplus.domain.engine.MetronomeEngine
import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.BeatState
import br.com.jonatas.metronomeplus.domain.model.Measure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
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
    private lateinit var mockMeasureRepository: MeasureRepository
    private lateinit var viewModel: MetronomeViewModel
    private val testDispatchers = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatchers)

        val mockAssetManager = mock(AssetManager::class.java)
        `when`(mockAssetProvider.getAssets()).thenReturn(mockAssetManager)
        `when`(mockAudioSettingProvider.getSampleRate()).thenReturn(48000)
        `when`(mockAudioSettingProvider.getFramesPerBurst()).thenReturn(256)
        `when`(mockMeasureRepository.getMeasure).thenReturn(
            Measure(
                isPlaying = false,
                bpm = 120,
                beats = mutableListOf(
                    Beat(state = BeatState.Accent),
                    Beat(state = BeatState.Normal),
                    Beat(state = BeatState.Normal),
                    Beat(state = BeatState.Normal)
                )
            )
        )

        viewModel = MetronomeViewModel(
            mockMetronomeEngine,
            mockAssetProvider,
            mockAudioSettingProvider,
            mockMeasureRepository,
            UnconfinedTestDispatcher(testDispatchers.scheduler)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `togglePlayPause should toggle isPlaying and call metronome engine`() = runTest {
        val states = mutableListOf<MetronomeViewModel.MetronomeState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { states.add(it) }
        }

        viewModel.togglePlayPause()
        testDispatchers.scheduler.advanceUntilIdle()
        assertEquals(
            true,
            (states.last() as MetronomeViewModel.MetronomeState.Ready).measure.isPlaying
        )
        verify(mockMetronomeEngine).startPlaying()

        viewModel.togglePlayPause()
        testDispatchers.scheduler.advanceUntilIdle()
        assertEquals(
            false,
            (states.last() as MetronomeViewModel.MetronomeState.Ready).measure.isPlaying
        )
        verify(mockMetronomeEngine).stopPlaying()
        job.cancel()
    }
}