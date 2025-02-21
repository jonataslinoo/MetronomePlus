package br.com.jonatas.metronomeplus.presenter.ui.viewmodel

import android.content.res.AssetManager
import br.com.jonatas.metronomeplus.data.model.BeatDto
import br.com.jonatas.metronomeplus.data.model.BeatStateDto
import br.com.jonatas.metronomeplus.data.model.MeasureDto
import br.com.jonatas.metronomeplus.data.repository.MeasureRepositoryImpl
import br.com.jonatas.metronomeplus.domain.engine.MetronomeEngine
import br.com.jonatas.metronomeplus.domain.provider.AssetProvider
import br.com.jonatas.metronomeplus.domain.provider.AudioSettingsProvider
import br.com.jonatas.metronomeplus.domain.repository.MeasureRepository
import br.com.jonatas.metronomeplus.domain.source.MeasureDataSource
import br.com.jonatas.metronomeplus.presenter.model.BeatStateUiModel
import br.com.jonatas.metronomeplus.presenter.model.BeatUiModel
import br.com.jonatas.metronomeplus.presenter.model.MeasureUiModel
import br.com.jonatas.metronomeplus.presenter.viewmodel.MetronomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
    private lateinit var mockDataSource: MeasureDataSource
    private val measureRepository: MeasureRepository by lazy {
        MeasureRepositoryImpl(mockDataSource)
    }
    private val viewModel by lazy {
        MetronomeViewModel(
            mockMetronomeEngine,
            mockAssetProvider,
            mockAudioSettingProvider,
            measureRepository,
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
        runTest {
            val measureDto = MeasureDto(
                bpm = 120,
                beats = mutableListOf(
                    BeatDto(BeatStateDto.Accent),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                )
            )
            `when`(mockDataSource.getMeasure()).thenReturn(measureDto)

            val states = mutableListOf<MetronomeViewModel.MetronomeState>()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.toList(states)
            }

            assertEquals(
                MetronomeViewModel.MetronomeState.Loading,
                states.first()
            )

            job.cancel()
        }

    @Test
    fun `shuould transition to Ready state when data loading is successful`() = runTest {
        val measureDto = MeasureDto(
            bpm = 120,
            beats = mutableListOf(
                BeatDto(BeatStateDto.Accent),
                BeatDto(BeatStateDto.Normal),
                BeatDto(BeatStateDto.Normal),
                BeatDto(BeatStateDto.Normal)
            )
        )
        `when`(mockDataSource.getMeasure()).thenReturn(measureDto)

        val states = mutableListOf<MetronomeViewModel.MetronomeState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.toList(states)
        }

        assertEquals(
            MetronomeViewModel.MetronomeState.Loading,
            states.first()
        )

        advanceUntilIdle()
        assertEquals(
            MetronomeViewModel.MetronomeState.Ready(
                MeasureUiModel(
                    isPlaying = false,
                    bpm = 120,
                    beats = mutableListOf(
                        BeatUiModel(BeatStateUiModel.Accent),
                        BeatUiModel(BeatStateUiModel.Normal),
                        BeatUiModel(BeatStateUiModel.Normal),
                        BeatUiModel(BeatStateUiModel.Normal)
                    )
                )
            ),
            states.last()
        )

        job.cancel()
    }

    @Test
    fun `should toggle isPlaying and play or pause the metronome engine when togglePlayPause is called`() =
        runTest {
            val measureDto = MeasureDto(
                bpm = 120,
                beats = mutableListOf(
                    BeatDto(BeatStateDto.Accent),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                )
            )
            `when`(mockDataSource.getMeasure()).thenReturn(measureDto)

            val states = mutableListOf<MetronomeViewModel.MetronomeState>()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.toList(states)
            }

            assertEquals(
                states.first(),
                MetronomeViewModel.MetronomeState.Loading
            )

            viewModel.togglePlayPause()
            advanceUntilIdle()
            assertEquals(
                true,
                (states.last() as MetronomeViewModel.MetronomeState.Ready).measure.isPlaying
            )
            verify(mockMetronomeEngine).startPlaying()

            viewModel.togglePlayPause()
            advanceUntilIdle()
            assertEquals(
                false,
                (states.last() as MetronomeViewModel.MetronomeState.Ready).measure.isPlaying
            )
            verify(mockMetronomeEngine).stopPlaying()
            job.cancel()
        }
}