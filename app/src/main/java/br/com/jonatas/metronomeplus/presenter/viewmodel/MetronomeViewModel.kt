package br.com.jonatas.metronomeplus.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.jonatas.metronomeplus.data.mapper.toDto
import br.com.jonatas.metronomeplus.data.mapper.toDtoArray
import br.com.jonatas.metronomeplus.domain.engine.MetronomeEngine
import br.com.jonatas.metronomeplus.domain.usecase.GetMeasureUseCase
import br.com.jonatas.metronomeplus.presenter.mapper.toDomain
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModel
import br.com.jonatas.metronomeplus.presenter.model.BeatStateUiModel
import br.com.jonatas.metronomeplus.presenter.model.BeatUiModel
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
    private val getMeasureUseCase: GetMeasureUseCase,
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

    fun increaseBpm(value: Int) {
        if (value > 0) {
            setBpm(value)
        }
    }

    fun decreaseBpm(value: Int) {
        if (value < 0) {
            setBpm(value)
        }
    }

    private fun setBpm(value: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MetronomeState.Ready) {
                var newValue = currentState.measure.bpm.plus(value)
                if (newValue < 0) {
                    newValue = 0
                }
                _uiState.value =
                    currentState.copy(measure = currentState.measure.copy(bpm = newValue))

                metronomeEngine.setBpm(newValue)
            }
        }
    }

    fun addBeat() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MetronomeState.Ready) {
                if (currentState.measure.beats.size < 16) {
                    val newBeats = currentState.measure.beats + BeatUiModel(BeatStateUiModel.Normal)
                    _uiState.value =
                        currentState.copy(measure = currentState.measure.copy(beats = newBeats.toMutableList()))

                    val domainBeats = newBeats.map { it.toDomain() }
                    metronomeEngine.setBeats(domainBeats.toDtoArray())
                }
            }
        }
    }

    fun removeBeat() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MetronomeState.Ready) {
                val newBeats = currentState.measure.beats.toMutableList()
                if (newBeats.size > 1) {
                    newBeats.removeAt(newBeats.lastIndex)
                    _uiState.value =
                        currentState.copy(measure = currentState.measure.copy(beats = newBeats))

                    val domainBeats = newBeats.map { it.toDomain() }
                    metronomeEngine.setBeats(domainBeats.toDtoArray())
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
    private val getMeasureUseCase: GetMeasureUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MetronomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MetronomeViewModel(
                metronomeEngine = metronomeEngine,
                getMeasureUseCase = getMeasureUseCase,
                dispatcher = dispatcher
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
