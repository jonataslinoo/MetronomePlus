package br.com.jonatas.metronomeplus.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.jonatas.metronomeplus.data.repository.FolderRepositoryImpl
import br.com.jonatas.metronomeplus.data.source.FolderDataSourceImpl
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModelList
import br.com.jonatas.metronomeplus.presenter.model.FolderUiModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow<LibraryState>(LibraryState.Loading)
    val uiState: StateFlow<LibraryState> get() = _uiState.asStateFlow()

    sealed class LibraryState {
        data object Loading : LibraryState()
        data class Ready(val foldersUi: List<FolderUiModel>) : LibraryState()
        data class Error(val message: String) : LibraryState()
    }

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val folderDataSource = FolderDataSourceImpl()
                val folderRepository = FolderRepositoryImpl(folderDataSource)
                val folders = folderRepository.getFolders()

                _uiState.value = LibraryState.Ready(foldersUi = folders.toUiModelList())

            } catch (ex: Exception) {
                _uiState.value = LibraryState.Error(message = "Error: ${ex.message}")
            }
        }
    }

    fun addFolder() {

    }

    fun removeFolder() {

    }

    private inline fun <reified state : LibraryState> withState(execute: state.() -> Unit) {
        try {
            val currentState = _uiState.value
            if (currentState is state) {
                currentState.execute()
            }
        } catch (ex: Exception) {
            _uiState.value = LibraryState.Error(message = "Error: ${ex.message}")
        }
    }
}

class LibraryVieModelFactory(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LibraryViewModel(
                dispatcher = dispatcher
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}