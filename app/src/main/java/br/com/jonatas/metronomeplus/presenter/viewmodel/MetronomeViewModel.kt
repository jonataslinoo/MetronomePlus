package br.com.jonatas.metronomeplus.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.jonatas.metronomeplus.data.mapper.toDto
import br.com.jonatas.metronomeplus.data.mapper.toDtoArray
import br.com.jonatas.metronomeplus.domain.engine.MetronomeEngine
import br.com.jonatas.metronomeplus.domain.usecase.AddBeatUseCase
import br.com.jonatas.metronomeplus.domain.usecase.DecreaseBpmUseCase
import br.com.jonatas.metronomeplus.domain.usecase.GetMeasureUseCase
import br.com.jonatas.metronomeplus.domain.usecase.IncreaseBpmUseCase
import br.com.jonatas.metronomeplus.domain.usecase.RemoveBeatUseCase
import br.com.jonatas.metronomeplus.domain.usecase.TogglePlayPauseUseCase
import br.com.jonatas.metronomeplus.presenter.mapper.toDomain
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModel
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModelList
import br.com.jonatas.metronomeplus.presenter.model.MeasureUiModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class MetronomeViewModel(
    private val metronomeEngine: MetronomeEngine,
    private val getMeasureUseCase: GetMeasureUseCase,
    private val increaseBpmUseCase: IncreaseBpmUseCase,
    private val decreaseBpmUseCase: DecreaseBpmUseCase,
    private val addBeatUseCase: AddBeatUseCase,
    private val removeBeatUseCase: RemoveBeatUseCase,
    private val togglePlayPauseUseCase: TogglePlayPauseUseCase,
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
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val measure = getMeasureUseCase()
                metronomeEngine.initialize(measure.toDto())

                _uiState.value = MetronomeState.Ready(measure.toUiModel())
            } catch (ex: Exception) {
                _uiState.value = MetronomeState.Error("Error: ${ex.message}")
            }
        }
    }

    fun togglePlayPause() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MetronomeState.Ready) {
                val newIsPlaying = togglePlayPauseUseCase(currentState.measure.isPlaying)

                if (newIsPlaying) {
                    metronomeEngine.startPlaying()
                } else {
                    metronomeEngine.stopPlaying()
                }

                val newMeasure = currentState.measure.copy(isPlaying = newIsPlaying)
                _uiState.value = currentState.copy(measure = newMeasure)
            }
        }
    }

    fun increaseBpm(value: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MetronomeState.Ready) {
                val newBpm = increaseBpmUseCase(currentState.measure.bpm, value)

                metronomeEngine.setBpm(newBpm)

                val newMeasure = currentState.measure.copy(bpm = newBpm)
                _uiState.value = currentState.copy(measure = newMeasure)
            }
        }
    }

    fun decreaseBpm(value: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MetronomeState.Ready) {
                val newBpm = decreaseBpmUseCase(currentState.measure.bpm, value)

                metronomeEngine.setBpm(newBpm)

                val newMeasure = currentState.measure.copy(bpm = newBpm)
                _uiState.value = currentState.copy(measure = newMeasure)
            }
        }
    }

    fun addBeat() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MetronomeState.Ready) {
                val newBeats = addBeatUseCase(currentState.measure.toDomain().beats)

                metronomeEngine.setBeats(newBeats.toDtoArray())

                val newMeasure = currentState.measure.copy(beats = newBeats.toUiModelList())
                _uiState.value = currentState.copy(measure = newMeasure)
            }
        }
    }

    fun removeBeat() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MetronomeState.Ready) {
                val newBeats = removeBeatUseCase(currentState.measure.toDomain().beats)

                metronomeEngine.setBeats(newBeats.toDtoArray())

                val newMeasure = currentState.measure.copy(beats = newBeats.toUiModelList())
                _uiState.value = currentState.copy(measure = newMeasure)
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
    private val getMeasureUseCase: GetMeasureUseCase,
    private val increaseBpmUseCase: IncreaseBpmUseCase,
    private val decreaseBpmUseCase: DecreaseBpmUseCase,
    private val addBeatUseCase: AddBeatUseCase,
    private val removeBeatUseCase: RemoveBeatUseCase,
    private val togglePlayPauseUseCase: TogglePlayPauseUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MetronomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MetronomeViewModel(
                metronomeEngine = metronomeEngine,
                getMeasureUseCase = getMeasureUseCase,
                increaseBpmUseCase = increaseBpmUseCase,
                decreaseBpmUseCase = decreaseBpmUseCase,
                addBeatUseCase = addBeatUseCase,
                removeBeatUseCase = removeBeatUseCase,
                togglePlayPauseUseCase = togglePlayPauseUseCase,
                dispatcher = dispatcher
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
