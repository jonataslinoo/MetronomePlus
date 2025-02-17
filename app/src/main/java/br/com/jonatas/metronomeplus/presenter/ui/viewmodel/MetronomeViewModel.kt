package br.com.jonatas.metronomeplus.presenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.jonatas.metronomeplus.data.provider.AssetProvider
import br.com.jonatas.metronomeplus.data.provider.AudioSettingsProvider
import br.com.jonatas.metronomeplus.domain.engine.MetronomeEngine
import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.BeatState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MetronomeViewModel(
    private val metronomeEngine: MetronomeEngine,
    private val assetProvider: AssetProvider,
    private val audioSettingsProvider: AudioSettingsProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<MetronomeState>(MetronomeState.Initial)
    val uiState: StateFlow<MetronomeState> get() = _uiState.asStateFlow()

    sealed class MetronomeState {
        data class Ready(
            val isPlaying: Boolean,
            val bpm: Int,
            val beats: List<Beat>
        ) : MetronomeState()

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
            isPlaying = false,
            bpm = 120,
            beats = mutableListOf(
                Beat(BeatState.Accent),
                Beat(BeatState.Normal),
                Beat(BeatState.Normal),
                Beat(BeatState.Normal)
            )
        )
    }

    private fun initNativeBpm() {
        val currentState = _uiState.value
        if (currentState is MetronomeState.Ready) {
            metronomeEngine.setBpm(currentState.bpm)
        }
    }

    private fun updateNativeBeats() {
        val currentState = _uiState.value
        if (currentState is MetronomeState.Ready) {
            metronomeEngine.setBeats(currentState.beats.toTypedArray())
        }
    }

    fun togglePlayPause() {
        val currentState = _uiState.value
        if (currentState is MetronomeState.Ready) {
            val newIsPlaying = !currentState.isPlaying
            _uiState.value = currentState.copy(isPlaying = newIsPlaying)
            if (newIsPlaying) {
                initNativeBpm()
                updateNativeBeats()
                metronomeEngine.startPlaying()
            } else metronomeEngine.stopPlaying()
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
    private val audioSettingsProvider: AudioSettingsProvider
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MetronomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MetronomeViewModel(
                metronomeEngine,
                assetProvider,
                audioSettingsProvider
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
