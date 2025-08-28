package br.com.jonatas.metronomeplus.presenter.model.states

sealed interface UiState<out T : Any> {
    object Loading : UiState<Nothing>
    data class Ready<out T : Any>(val result: T) : UiState<T>
    data class Error(val error: Throwable) : UiState<Nothing>
}