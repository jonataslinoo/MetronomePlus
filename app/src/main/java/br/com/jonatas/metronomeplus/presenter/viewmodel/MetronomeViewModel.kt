package br.com.jonatas.metronomeplus.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.jonatas.metronomeplus.domain.engine.MetronomeEngine
import br.com.jonatas.metronomeplus.domain.provider.AssetProvider
import br.com.jonatas.metronomeplus.domain.provider.AudioSettingsProvider
import br.com.jonatas.metronomeplus.domain.repository.MeasureRepository
import br.com.jonatas.metronomeplus.presenter.mapper.toDomain
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModel
import br.com.jonatas.metronomeplus.presenter.model.MeasureUiModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class MetronomeViewModel(
    private val metronomeEngine: MetronomeEngine,
    private val assetProvider: AssetProvider,
    private val audioSettingsProvider: AudioSettingsProvider,
    private val measureRepository: MeasureRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow<MetronomeState>(MetronomeState.Loading)
    val uiState: StateFlow<MetronomeState> get() = _uiState.asStateFlow()
    private val mutex = Mutex()

    sealed class MetronomeState {
        data object Loading : MetronomeState()
        data class Ready(val measure: MeasureUiModel) : MetronomeState()
        data class Error(val message: String) : MetronomeState()
    }

    init {
        metronomeEngine.initialize(assetProvider.getAssets())

        metronomeEngine.setDefaultStreamValues(
            audioSettingsProvider.getSampleRate(),
            audioSettingsProvider.getFramesPerBurst()
        )

        loadData()

        initNativeBpm()
        updateNativeBeats()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val measure = measureRepository.getMeasure().toUiModel()
                _uiState.value = MetronomeState.Ready(measure)
            } catch (ex: Exception) {
                _uiState.value = MetronomeState.Error("Error: ${ex.message}")
            }
        }
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
            metronomeEngine.setBeats(currentState.measure.beats.toDomain().toTypedArray())
        }
    }

    fun togglePlayPause() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MetronomeState.Ready) {
                val newIsPlaying = !currentState.measure.isPlaying
                _uiState.value =
                    currentState.copy(measure = currentState.measure.copy(isPlaying = newIsPlaying))

                withContext(dispatcher) {
                    mutex.withLock {
                        if (newIsPlaying) {
                            metronomeEngine.startPlaying()
                        } else {
                            metronomeEngine.stopPlaying()
                        }
                    }
                }
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
    private val measureRepository: MeasureRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MetronomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MetronomeViewModel(
                metronomeEngine,
                assetProvider,
                audioSettingsProvider,
                measureRepository,
                dispatcher
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
