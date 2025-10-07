package br.com.jonatas.metronomeplus.presenter.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.jonatas.metronomeplus.domain.usecase.folderform.GetFolderUseCase
import br.com.jonatas.metronomeplus.domain.usecase.folderform.song.GetSongsByFolderUseCase
import br.com.jonatas.metronomeplus.domain.util.filter.filterSongs
import br.com.jonatas.metronomeplus.presenter.extension.isDefaultOrEmptyId
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModel
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModelList
import br.com.jonatas.metronomeplus.presenter.model.folder.FolderFormTitleMode
import br.com.jonatas.metronomeplus.presenter.model.folder.FolderFormUiState
import br.com.jonatas.metronomeplus.presenter.model.states.UiState
import br.com.jonatas.metronomeplus.presenter.ui.adapter.utils.EditableState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class FolderFormViewModel @Inject constructor(
    val savedStateHandle: SavedStateHandle,
    private val getFolderUseCase: GetFolderUseCase,
    private val getSongsByFolderUseCase: GetSongsByFolderUseCase,
) : ViewModel() {

    private val folderId: String? = savedStateHandle["id"]
    private val _searchSongInfo = MutableStateFlow<String>("")
    private val _editableState = MutableStateFlow(EditableState(isEditMode = folderId == null))

    private val initialFolderFlow = flow {
        emit(getFolderUseCase(folderId = folderId))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState<FolderFormUiState>> =
        initialFolderFlow.flatMapLatest { folder ->
            combine(
                getSongsByFolderUseCase(folder = folder),
                _searchSongInfo,
                _editableState,
            ) { songs, query, editableState ->

                val barTitle = getBarTitle(editableState.isEditMode)

                val filteredList = songs.filterSongs(query)

                FolderFormUiState(
                    folderUi = folder.toUiModel(),
                    barTitle = barTitle,
                    songsUi = filteredList.toUiModelList(),
                    editableState = editableState.copy(
                        isReorderingMode = folder.toUiModel().isDefaultOrEmptyId()
                    )
                )
            }
        }.map { completeState ->
            UiState.Ready(result = completeState) as UiState<FolderFormUiState>
        }.catch { throwable ->
            emit(UiState.Error(error = throwable))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = UiState.Loading
        )

    private fun getBarTitle(isEditing: Boolean): FolderFormTitleMode {
        return if (folderId != null) {
            if (isEditing) FolderFormTitleMode.EditFolder
            else FolderFormTitleMode.ViewFolder
        } else {
            FolderFormTitleMode.NewFolder
        }
    }

    fun searchSongInfo(query: String) {
        _searchSongInfo.value = query
    }

    fun enableEditMode() {
        _editableState.update { it.copy(isEditMode = true) }
    }

    fun enableListEditMode(enable: Boolean) {
        _editableState.update { it.copy(isListEditMode = enable) }
    }
}
