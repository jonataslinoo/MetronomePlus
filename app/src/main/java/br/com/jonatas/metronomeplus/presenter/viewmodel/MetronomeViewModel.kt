package br.com.jonatas.metronomeplus.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.jonatas.metronomeplus.data.mapper.toDto
import br.com.jonatas.metronomeplus.data.mapper.toDtoArray
import br.com.jonatas.metronomeplus.domain.engine.BeatChangeListener
import br.com.jonatas.metronomeplus.domain.engine.MetronomeEngine
import br.com.jonatas.metronomeplus.domain.usecase.AddBeatUseCase
import br.com.jonatas.metronomeplus.domain.usecase.DecreaseBpmUseCase
import br.com.jonatas.metronomeplus.domain.usecase.GetMeasureUseCase
import br.com.jonatas.metronomeplus.domain.usecase.IncreaseBpmUseCase
import br.com.jonatas.metronomeplus.domain.usecase.IncreaseMeasureCounter
import br.com.jonatas.metronomeplus.domain.usecase.NextBeatStateUseCase
import br.com.jonatas.metronomeplus.domain.usecase.RemoveBeatUseCase
import br.com.jonatas.metronomeplus.domain.usecase.TogglePlayPauseUseCase
import br.com.jonatas.metronomeplus.presenter.mapper.toDomain
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModel
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModelList
import br.com.jonatas.metronomeplus.presenter.model.MeasureProgressUiModel
import br.com.jonatas.metronomeplus.presenter.model.MeasureUiModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MetronomeViewModel(
    private val metronomeEngine: MetronomeEngine,
    private val getMeasureUseCase: GetMeasureUseCase,
    private val increaseBpmUseCase: IncreaseBpmUseCase,
    private val decreaseBpmUseCase: DecreaseBpmUseCase,
    private val addBeatUseCase: AddBeatUseCase,
    private val removeBeatUseCase: RemoveBeatUseCase,
    private val togglePlayPauseUseCase: TogglePlayPauseUseCase,
    private val increaseMeasureCounter: IncreaseMeasureCounter,
    private val nextBeatStateUseCase: NextBeatStateUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel(), BeatChangeListener {

    private val _uiState = MutableStateFlow<MetronomeState>(MetronomeState.Loading)
    val uiState: StateFlow<MetronomeState> get() = _uiState.asStateFlow()

    private val _measureProgressUiState = MutableStateFlow(MeasureProgressUiModel())
    val measureProgressUiState: StateFlow<MeasureProgressUiModel> get() = _measureProgressUiState.asStateFlow()

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
                metronomeEngine.setOnBeatChangeListener(this@MetronomeViewModel)

                _uiState.value = MetronomeState.Ready(measure.toUiModel())
            } catch (ex: Exception) {
                _uiState.value = MetronomeState.Error("Error: ${ex.message}")
            }
        }
    }

    fun togglePlayPause() {
        viewModelScope.launch {
            withState<MetronomeState.Ready> {
                val newIsPlaying = togglePlayPauseUseCase(measure.isPlaying)

                if (newIsPlaying) {
                    metronomeEngine.startPlaying()
                } else {
                    metronomeEngine.stopPlaying()
                }

                val newMeasure = measure.copy(isPlaying = newIsPlaying)
                _uiState.value = copy(measure = newMeasure)
            }
        }
    }

    fun increaseBpm(value: Int) {
        viewModelScope.launch {
            withState<MetronomeState.Ready> {
                val newBpm = increaseBpmUseCase(measure.bpm, value)

                metronomeEngine.setBpm(newBpm)

                val newMeasure = measure.copy(bpm = newBpm)
                _uiState.value = copy(measure = newMeasure)
            }
        }
    }

    fun decreaseBpm(value: Int) {
        viewModelScope.launch {
            withState<MetronomeState.Ready> {
                val newBpm = decreaseBpmUseCase(measure.bpm, value)

                metronomeEngine.setBpm(newBpm)

                val newMeasure = measure.copy(bpm = newBpm)
                _uiState.value = copy(measure = newMeasure)
            }
        }
    }

    fun addBeat() {
        viewModelScope.launch {
            withState<MetronomeState.Ready> {
                val newBeats = addBeatUseCase(measure.toDomain().beats)

                metronomeEngine.setBeats(newBeats.toDtoArray())

                val newMeasure = measure.copy(beats = newBeats.toUiModelList())
                _uiState.value = copy(measure = newMeasure)
            }
        }
    }

    fun removeBeat() {
        viewModelScope.launch {
            withState<MetronomeState.Ready> {
                val newBeats = removeBeatUseCase(measure.toDomain().beats)

                metronomeEngine.setBeats(newBeats.toDtoArray())

                val newMeasure = measure.copy(beats = newBeats.toUiModelList())
                _uiState.value = copy(measure = newMeasure)
            }
        }
    }

    fun changeBeatState(index: Int) {
        viewModelScope.launch {
            withState<MetronomeState.Ready> {
                val newBeats = nextBeatStateUseCase(index, measure.toDomain().beats)

                metronomeEngine.setBeats(newBeats.toDtoArray())

                val newMeasureUi = measure.copy(beats = newBeats.toUiModelList())
                _uiState.value = copy(measure = newMeasureUi)
            }
        }
    }

    override fun onBeatChanged(index: Int) {
        viewModelScope.launch {
            val currentPair = _measureProgressUiState.value
            val measureCount = increaseMeasureCounter(index, currentPair.measureCount)

            _measureProgressUiState.value = currentPair.copy(
                currentBeat = index,
                measureCount = measureCount
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        metronomeEngine.cleanup()
    }

    private inline fun <reified T : MetronomeState> withState(block: T.() -> Unit) {
        val currentState = _uiState.value
        if (currentState is T) {
            block(currentState)
        }
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
    private val increaseMeasureCounter: IncreaseMeasureCounter,
    private val nextBeatStateUseCase: NextBeatStateUseCase,
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
                increaseMeasureCounter = increaseMeasureCounter,
                nextBeatStateUseCase = nextBeatStateUseCase,
                dispatcher = dispatcher
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}