package br.com.jonatas.metronomeplus.presenter.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.jonatas.metronomeplus.domain.usecase.folderform.GetFolderUseCase
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModel
import br.com.jonatas.metronomeplus.presenter.model.BeatStateUiModel
import br.com.jonatas.metronomeplus.presenter.model.BeatUiModel
import br.com.jonatas.metronomeplus.presenter.model.TimeSignatureUiModel
import br.com.jonatas.metronomeplus.presenter.model.folder.FolderFormTitleMode
import br.com.jonatas.metronomeplus.presenter.model.folder.FolderFormUiState
import br.com.jonatas.metronomeplus.presenter.model.song.SongUiModel
import br.com.jonatas.metronomeplus.presenter.model.states.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FolderFormViewModel @Inject constructor(
    val savedStateHandle: SavedStateHandle,
    private val getFolderUseCase: GetFolderUseCase,
) : ViewModel() {

    private val _isEditMode = MutableStateFlow<Boolean>(false)
    private val folderId: String? = savedStateHandle["id"]

    private val initialFolderFlow = flow {
        emit(getFolderUseCase(folderId = folderId))
    }

    val uiState: StateFlow<UiState<FolderFormUiState>> = combine(
        initialFolderFlow,
        _isEditMode
    ) { initialFolder, isEditing ->

        val (canEdit, barTitle) = folderFormUiStateInfo(isEditing)

        FolderFormUiState(
            folderUi = initialFolder.toUiModel(),
            isEditMode = canEdit,
            barTitle = barTitle,
            songsUi = mockSongs()
        )

    }.map { completeState ->
        UiState.Ready(result = completeState) as UiState<FolderFormUiState>
    }.catch { throwable ->
        emit(UiState.Error(error = throwable))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = UiState.Loading
    )

    private fun folderFormUiStateInfo(isEditing: Boolean): Pair<Boolean, FolderFormTitleMode> {
        return if (folderId != null) {
            if (isEditing) {
                true to FolderFormTitleMode.EditFolder
            } else {
                false to FolderFormTitleMode.ViewFolder
            }
        } else {
            true to FolderFormTitleMode.NewFolder
        }
    }

    fun onEditClicked() {
        _isEditMode.value = true
    }

    private fun mockSongs(): List<SongUiModel> {
        return listOf(
            SongUiModel(
                id = "1",
                title = "Marcado",
                artist = "Ronaldo Bezerra",
                bpm = 75,
                timeSignature = TimeSignatureUiModel(8, 4),
                beatPatterns = listOf(
                    BeatUiModel(BeatStateUiModel.Accent),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                )
            ),
            SongUiModel(
                id = "2",
                title = "Deus do impossível",
                artist = "Comunidade Evangélica Internacional Da Zona Sul",
                bpm = 600,
                timeSignature = TimeSignatureUiModel(4, 4),
                beatPatterns = listOf(
                    BeatUiModel(BeatStateUiModel.Accent),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                )
            ),
            SongUiModel(
                id = "3",
                title = "Aclame ao Senhor/Manancial/A Ele A Gloria/Te Agradeço",
                artist = "Ministério Sarando A Terra Ferida",
                bpm = 444,
                timeSignature = TimeSignatureUiModel(6, 4),
                beatPatterns = listOf(
                    BeatUiModel(BeatStateUiModel.Accent),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                )
            ),
            SongUiModel(
                id = "4",
                title = "Cruz",
                artist = "Comunidade Evangélica Internacional Da Zona Sul",
                bpm = 90,
                timeSignature = TimeSignatureUiModel(16, 4),
                beatPatterns = listOf(
                    BeatUiModel(BeatStateUiModel.Accent),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                )
            ),
            SongUiModel(
                id = "2",
                title = "Deus do impossível",
                artist = "Comunidade Evangélica Internacional Da Zona Sul",
                bpm = 600,
                timeSignature = TimeSignatureUiModel(4, 4),
                beatPatterns = listOf(
                    BeatUiModel(BeatStateUiModel.Accent),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                )
            ),
            SongUiModel(
                id = "2",
                title = "Deus do impossível",
                artist = "Comunidade Evangélica Internacional Da Zona Sul",
                bpm = 600,
                timeSignature = TimeSignatureUiModel(4, 4),
                beatPatterns = listOf(
                    BeatUiModel(BeatStateUiModel.Accent),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                )
            ),
            SongUiModel(
                id = "2",
                title = "Deus do impossível",
                artist = "Comunidade Evangélica Internacional Da Zona Sul",
                bpm = 600,
                timeSignature = TimeSignatureUiModel(4, 4),
                beatPatterns = listOf(
                    BeatUiModel(BeatStateUiModel.Accent),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                )
            ),
            SongUiModel(
                id = "2",
                title = "Deus do impossível",
                artist = "Comunidade Evangélica Internacional Da Zona Sul",
                bpm = 600,
                timeSignature = TimeSignatureUiModel(4, 4),
                beatPatterns = listOf(
                    BeatUiModel(BeatStateUiModel.Accent),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                )
            ),
            SongUiModel(
                id = "2",
                title = "Deus do impossível",
                artist = "Comunidade Evangélica Internacional Da Zona Sul",
                bpm = 600,
                timeSignature = TimeSignatureUiModel(4, 4),
                beatPatterns = listOf(
                    BeatUiModel(BeatStateUiModel.Accent),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                )
            ),
            SongUiModel(
                id = "2",
                title = "Deus do impossível",
                artist = "Comunidade Evangélica Internacional Da Zona Sul",
                bpm = 600,
                timeSignature = TimeSignatureUiModel(4, 4),
                beatPatterns = listOf(
                    BeatUiModel(BeatStateUiModel.Accent),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                )
            ),
            SongUiModel(
                id = "2",
                title = "Deus do impossível",
                artist = "Comunidade Evangélica Internacional Da Zona Sul",
                bpm = 600,
                timeSignature = TimeSignatureUiModel(4, 4),
                beatPatterns = listOf(
                    BeatUiModel(BeatStateUiModel.Accent),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                )
            ),
            SongUiModel(
                id = "2",
                title = "Deus do impossível",
                artist = "Comunidade Evangélica Internacional Da Zona Sul",
                bpm = 600,
                timeSignature = TimeSignatureUiModel(4, 4),
                beatPatterns = listOf(
                    BeatUiModel(BeatStateUiModel.Accent),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                    BeatUiModel(BeatStateUiModel.Normal),
                )
            ),
        )
    }
}
