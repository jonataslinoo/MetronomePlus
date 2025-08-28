package br.com.jonatas.metronomeplus.presenter.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.jonatas.metronomeplus.domain.usecase.folderform.GetFolderUseCase
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModel
import br.com.jonatas.metronomeplus.presenter.model.FolderUiModel
import br.com.jonatas.metronomeplus.presenter.model.states.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FolderFormViewModel @Inject constructor(
    val savedStateHandle: SavedStateHandle,
    private val getFolderUseCase: GetFolderUseCase,
) : ViewModel() {

    private val folderId: String? = savedStateHandle["id"]

    val uiState: StateFlow<UiState<FolderUiModel>> = flow<UiState<FolderUiModel>> {
        val folder = getFolderUseCase(folderId = folderId)
        emit(UiState.Ready(result = folder.toUiModel()))
    }.catch { throwable ->
        emit(UiState.Error(error = throwable))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = UiState.Loading
    )
}