package br.com.jonatas.metronomeplus.presenter.viewmodel

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
import br.com.jonatas.metronomeplus.presenter.mapper.toDomain
import br.com.jonatas.metronomeplus.presenter.model.BeatStateUiModel
import br.com.jonatas.metronomeplus.presenter.model.BeatUiModel
import br.com.jonatas.metronomeplus.presenter.model.MeasureUiModel
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
import org.mockito.kotlin.never

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
    fun `should transition to Error state when data loading fails`() = runTest {
        `when`(mockDataSource.getMeasure()).thenThrow(RuntimeException("Data Loading failure"))

        val states = mutableListOf<MetronomeViewModel.MetronomeState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.toList(states)
        }

        advanceUntilIdle()
        assertEquals(
            MetronomeViewModel.MetronomeState.Loading,
            states.first()
        )

        advanceUntilIdle()
        assertEquals(
            MetronomeViewModel.MetronomeState.Error("Error: Data Loading failure"),
            states.last(),
        )
        job.cancel()
    }

    @Test
    fun `should cleanup metronome when the MetronomeViewModel is cleared`() = runTest {
        val method = MetronomeViewModel::class.java.getDeclaredMethod("onCleared")
        method.isAccessible = true
        method.invoke(viewModel)

        verify(mockMetronomeEngine).cleanup()
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

    @Test
    fun `should increase bpm when increaseBpm is called with a positive value`() = runTest {
        val measureDto = MeasureDto(
            bpm = 120,
            beats = mutableListOf()
        )
        `when`(mockDataSource.getMeasure()).thenReturn(measureDto)

        val states = mutableListOf<MetronomeViewModel.MetronomeState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.toList(states)
        }

        viewModel.increaseBpm(10)
        advanceUntilIdle()
        assertEquals(
            130,
            (states.last() as MetronomeViewModel.MetronomeState.Ready).measure.bpm
        )
        verify(mockMetronomeEngine).setBpm(130)
        job.cancel()
    }

    @Test
    fun `should not increase bpm when increaseBpm is called with a negative value`() = runTest {
        val measureDto = MeasureDto(
            bpm = 120,
            beats = mutableListOf()
        )
        `when`(mockDataSource.getMeasure()).thenReturn(measureDto)

        val states = mutableListOf<MetronomeViewModel.MetronomeState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.toList(states)
        }

        viewModel.increaseBpm(-10)
        advanceUntilIdle()
        assertEquals(
            120,
            (states.last() as MetronomeViewModel.MetronomeState.Ready).measure.bpm
        )
        verify(mockMetronomeEngine, never()).setBpm(120)
        job.cancel()
    }

    @Test
    fun `should decrease bpm when decreaseBpm is called with a negative value`() = runTest {
        val measureDto = MeasureDto(
            bpm = 120,
            beats = mutableListOf()
        )
        `when`(mockDataSource.getMeasure()).thenReturn(measureDto)

        val states = mutableListOf<MetronomeViewModel.MetronomeState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.toList(states)
        }

        viewModel.decreaseBpm(-10)
        advanceUntilIdle()
        assertEquals(
            110,
            (states.last() as MetronomeViewModel.MetronomeState.Ready).measure.bpm
        )
        verify(mockMetronomeEngine).setBpm(110)
        job.cancel()
    }

    @Test
    fun `should decrease bpm to zero when decreaseBpm is called with a value greater than the actual bpm `() =
        runTest {
            val measureDto = MeasureDto(
                bpm = 120,
                beats = mutableListOf()
            )
            `when`(mockDataSource.getMeasure()).thenReturn(measureDto)

            val states = mutableListOf<MetronomeViewModel.MetronomeState>()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.toList(states)
            }

            viewModel.decreaseBpm(-150)
            advanceUntilIdle()
            assertEquals(
                0,
                (states.last() as MetronomeViewModel.MetronomeState.Ready).measure.bpm
            )
            verify(mockMetronomeEngine).setBpm(0)
            job.cancel()
        }

    @Test
    fun `should not decrease bpm when decreaseBpm is called with a positive value `() = runTest {
        val measureDto = MeasureDto(
            bpm = 120,
            beats = mutableListOf()
        )
        `when`(mockDataSource.getMeasure()).thenReturn(measureDto)

        val states = mutableListOf<MetronomeViewModel.MetronomeState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.toList(states)
        }

        viewModel.decreaseBpm(10)
        advanceUntilIdle()
        assertEquals(
            120,
            (states.last() as MetronomeViewModel.MetronomeState.Ready).measure.bpm
        )
        verify(mockMetronomeEngine, never()).setBpm(120)
        job.cancel()
    }

    @Test
    fun `should add a normal beat when addBeat is called in the viewModel`() = runTest {
        val measureDto = MeasureDto(
            bpm = 0,
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

        val expectedBeats = mutableListOf(
            BeatUiModel(BeatStateUiModel.Accent),
            BeatUiModel(BeatStateUiModel.Normal),
            BeatUiModel(BeatStateUiModel.Normal),
            BeatUiModel(BeatStateUiModel.Normal),
            BeatUiModel(BeatStateUiModel.Normal),
        )
        viewModel.addBeat()
        advanceUntilIdle()
        assertEquals(
            expectedBeats,
            (states.last() as MetronomeViewModel.MetronomeState.Ready).measure.beats
        )
        assertEquals(
            5,
            (states.last() as MetronomeViewModel.MetronomeState.Ready).measure.beats.size
        )
        verify(mockMetronomeEngine).setBeats(expectedBeats.map { it.toDomain() }.toTypedArray())
        job.cancel()
    }

    @Test
    fun `should not add more than sixteen beats when addBeat is called in the viewModel`() =
        runTest {
            val measureDto = MeasureDto(
                bpm = 0,
                beats = mutableListOf(
                    BeatDto(BeatStateDto.Accent),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
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

            val expectedBeats = mutableListOf(
                BeatUiModel(BeatStateUiModel.Accent),
                BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal),
            )
            viewModel.addBeat()
            advanceUntilIdle()
            assertEquals(
                expectedBeats,
                (states.last() as MetronomeViewModel.MetronomeState.Ready).measure.beats
            )
            assertEquals(
                16,
                (states.last() as MetronomeViewModel.MetronomeState.Ready).measure.beats.size
            )
            verify(mockMetronomeEngine, never()).setBeats(expectedBeats.map { it.toDomain() }
                .toTypedArray())
            job.cancel()
        }

    @Test
    fun `should remove last index from the beat list when removeBeat is called in the viewModel`() =
        runTest {
            val measureDto = MeasureDto(
                bpm = 0,
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

            val expectedBeats = mutableListOf(
                BeatUiModel(BeatStateUiModel.Accent),
                BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal),
            )

            viewModel.removeBeat()
            advanceUntilIdle()
            assertEquals(
                expectedBeats,
                (states.last() as MetronomeViewModel.MetronomeState.Ready).measure.beats
            )
            assertEquals(
                3,
                (states.last() as MetronomeViewModel.MetronomeState.Ready).measure.beats.size
            )
            verify(mockMetronomeEngine).setBeats(expectedBeats.map { it.toDomain() }.toTypedArray())
            job.cancel()
        }

    @Test
    fun `should not remove the beat if there is only one beat when removeBeat is called int the viewModel `() =
        runTest {
            val measureDto = MeasureDto(
                bpm = 0,
                beats = mutableListOf(
                    BeatDto(BeatStateDto.Accent)
                )
            )
            `when`(mockDataSource.getMeasure()).thenReturn(measureDto)

            val states = mutableListOf<MetronomeViewModel.MetronomeState>()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.toList(states)
            }

            val expectedBeats = mutableListOf(BeatUiModel(BeatStateUiModel.Accent))

            viewModel.removeBeat()
            advanceUntilIdle()
            assertEquals(
                expectedBeats,
                (states.last() as MetronomeViewModel.MetronomeState.Ready).measure.beats
            )
            assertEquals(
                1,
                (states.last() as MetronomeViewModel.MetronomeState.Ready).measure.beats.size
            )
            verify(mockMetronomeEngine, never()).setBeats(expectedBeats.map { it.toDomain() }
                .toTypedArray())
            job.cancel()
        }
}