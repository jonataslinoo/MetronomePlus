package br.com.jonatas.metronomeplus.presenter.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.jonatas.metronomeplus.di.app.IoDispatcher
import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.model.Song
import br.com.jonatas.metronomeplus.domain.usecase.folderform.GetFolderUseCase
import br.com.jonatas.metronomeplus.domain.usecase.folderform.song.GetSongsByFolderUseCase
import br.com.jonatas.metronomeplus.domain.util.extensions.filterSongs
import br.com.jonatas.metronomeplus.domain.util.extensions.selectSongs
import br.com.jonatas.metronomeplus.presenter.extension.addOrRemove
import br.com.jonatas.metronomeplus.presenter.extension.isDefaultOrEmptyId
import br.com.jonatas.metronomeplus.presenter.extension.swapItems
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModel
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModelList
import br.com.jonatas.metronomeplus.presenter.model.folder.FolderFormTitleMode
import br.com.jonatas.metronomeplus.presenter.model.folder.FolderFormUiState
import br.com.jonatas.metronomeplus.presenter.model.states.UiState
import br.com.jonatas.metronomeplus.presenter.ui.adapter.utils.EditableState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderFormViewModel @Inject constructor(
    val savedStateHandle: SavedStateHandle,
    private val getFolderUseCase: GetFolderUseCase,
    private val getSongsByFolderUseCase: GetSongsByFolderUseCase,
    @IoDispatcher
    private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _folderId: String? = savedStateHandle["id"]
    private val _folder = MutableStateFlow(Folder.empty())
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    private val _error = MutableStateFlow<Throwable?>(null)
    private val _searchSong = MutableStateFlow("")
    private val _editableState = MutableStateFlow(EditableState(isEditMode = _folderId == null))
    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())

    private data class UiInteraction(
        val searchSong: String = "",
        val editableState: EditableState = EditableState(),
        val selectedIds: Set<String> = emptySet(),
    )

    init {
        viewModelScope.launch(dispatcher) {
            try {
                val currentFolder = getFolderUseCase(folderId = _folderId)
                _folder.update { currentFolder }

                getSongsByFolderUseCase(folder = currentFolder).collect { foundSongs ->
                    _songs.update { currentSongs ->
                        currentSongs + foundSongs
                    }
                }
            } catch (e: Exception) {
                _error.update { e }
            }
        }
    }

    private val _uiInteractionState: Flow<UiInteraction> = combine(
        _searchSong,
        _editableState,
        _selectedIds,
    ) { searchSong, editableState, selectedIds ->
        UiInteraction(
            searchSong = searchSong,
            selectedIds = selectedIds,
            editableState =
                editableState.copy(
                    isListEditMode = if (selectedIds.isEmpty()) false
                    else editableState.isListEditMode,
                ),
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState<FolderFormUiState>> = combine(
        _folder,
        _songs,
        _error,
        _uiInteractionState,
    ) { folder, songs, error, uiInteraction ->

        if (error != null) return@combine UiState.Error(error)

        val (query, editableState, selectedIds) = uiInteraction
        val barTitle = getBarTitle(editableState.isEditMode)
        val selectedSongs = songs.selectSongs(selectedIds)
        val filteredList = selectedSongs.filterSongs(query)

        val formUiState = FolderFormUiState(
            folderUi = folder.toUiModel(),
            barTitle = barTitle,
            songsUi = filteredList.toUiModelList(),
            editableState = editableState.copy(
                isReorderingMode =
                    folder.toUiModel().isDefaultOrEmptyId() and
                            (query.isEmpty() and query.isBlank())
            )
        )

        UiState.Ready(result = formUiState) as UiState<FolderFormUiState>
    }.catch { throwable ->
        emit(UiState.Error(error = throwable))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = UiState.Loading
    )

    private fun getBarTitle(isEditing: Boolean): FolderFormTitleMode {
        return if (_folderId != null) {
            if (isEditing) FolderFormTitleMode.EditFolder
            else FolderFormTitleMode.ViewFolder
        } else {
            FolderFormTitleMode.NewFolder
        }
    }

    fun searchSongInfo(query: String) {
        _searchSong.value = query
    }

    fun enableEditMode() {
        _editableState.update { it.copy(isEditMode = true) }
    }

    fun enableListEditModeAndSelectSong(songId: String, enable: Boolean) {
        toggleItemSelection(songId)
        _editableState.update { it.copy(isListEditMode = enable) }
    }

    fun toggleItemSelection(songId: String) {
        _selectedIds.update { it.addOrRemove(songId) }
    }

    fun swapPositionItems(fromPosition: Int, toPosition: Int) {
        _songs.update { it.swapItems(fromPosition, toPosition) }
    }
}
