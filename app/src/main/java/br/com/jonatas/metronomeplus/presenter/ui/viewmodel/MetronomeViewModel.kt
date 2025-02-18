package br.com.jonatas.metronomeplus.presenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.jonatas.metronomeplus.data.provider.AssetProvider
import br.com.jonatas.metronomeplus.data.provider.AudioSettingsProvider
import br.com.jonatas.metronomeplus.data.provider.MeasureRepository
import br.com.jonatas.metronomeplus.domain.engine.MetronomeEngine
import br.com.jonatas.metronomeplus.domain.model.Measure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex

class MetronomeViewModel(
    private val metronomeEngine: MetronomeEngine,
    private val assetProvider: AssetProvider,
    private val audioSettingsProvider: AudioSettingsProvider,
    private val measureRepository: MeasureRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MetronomeState>(MetronomeState.Initial)
    val uiState: StateFlow<MetronomeState> get() = _uiState.asStateFlow()
    private val mutex = Mutex()

    sealed class MetronomeState {
        data class Ready(val measure: Measure) : MetronomeState()
        data class Error(val message: String) : MetronomeState()
        data object Initial : MetronomeState()
    }

    init {
        metronomeEngine.initialize(assetProvider.getAssets())

        metronomeEngine.setDefaultStreamValues(
            audioSettingsProvider.getSampleRate(),
            audioSettingsProvider.getFramesPerBurst()
        )

        _uiState.value = MetronomeState.Ready(
            measure = measureRepository.getMeasure
        )

        initNativeBpm()
        updateNativeBeats()
    }

    private fun initNativeBpm() {
        val currentState = _uiState.value
        if (currentState is MetronomeState.Ready) {
            metronomeEngine.setBpm(currentState.measure.bpm)
        }
    }

    private fun updateNativeBeats() {
        val currentState = _uiState.value
        if (currentState is MetronomeState.Ready) {
            metronomeEngine.setBeats(currentState.measure.beats.toTypedArray())
        }
    }

    fun togglePlayPause() {
        val currentState = _uiState.value
        if (currentState is MetronomeState.Ready) {
            val newIsPlaying = !currentState.measure.isPlaying
            _uiState.value =
                currentState.copy(measure = currentState.measure.copy(isPlaying = newIsPlaying))

            if (newIsPlaying) {
                metronomeEngine.startPlaying()
            } else {
                metronomeEngine.stopPlaying()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        metronomeEngine.cleanup()
    }
}

class MetronomeViewModelFactory(
    private val metronomeEngine: MetronomeEngine,
    private val assetProvider: AssetProvider,
    private val audioSettingsProvider: AudioSettingsProvider,
    private val measureRepository: MeasureRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MetronomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MetronomeViewModel(
                metronomeEngine,
                assetProvider,
                audioSettingsProvider,
                measureRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
