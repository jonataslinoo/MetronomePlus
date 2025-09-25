package br.com.jonatas.metronomeplus.presenter.viewmodel

import androidx.lifecycle.SavedStateHandle
import br.com.jonatas.metronomeplus.MainCoroutineRule
import br.com.jonatas.metronomeplus.data.mapper.toDomainList
import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.model.Song
import br.com.jonatas.metronomeplus.domain.usecase.folderform.GetFolderUseCase
import br.com.jonatas.metronomeplus.domain.usecase.folderform.song.GetSongsByFolderUseCase
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModel
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModelList
import br.com.jonatas.metronomeplus.presenter.model.folder.FolderFormTitleMode
import br.com.jonatas.metronomeplus.presenter.model.folder.FolderFormUiState
import br.com.jonatas.metronomeplus.presenter.model.states.UiState
import br.com.jonatas.metronomeplus.presenter.ui.adapter.utils.EditableState
import br.com.jonatas.metronomeplus.util.Fixtures
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FolderFormViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @MockK
    private lateinit var mockSavedStateHandle: SavedStateHandle

    @MockK
    private lateinit var mockGetFolderUseCase: GetFolderUseCase

    @MockK
    private lateinit var mockGetSongsByFolderUseCase: GetSongsByFolderUseCase

    private lateinit var viewModel: FolderFormViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    private fun createViewModel(
        folderId: String? = null,
        folderToReturn: Folder? = null,
        exceptionToThrow: Exception? = null,
        songsToReturn: List<Song>? = null,
    ) {
        every { mockSavedStateHandle.get<String>("id") } returns folderId

        if (exceptionToThrow != null) {
            coEvery { mockGetFolderUseCase(folderId) } throws exceptionToThrow
        }

        if (folderToReturn != null) {
            coEvery { mockGetFolderUseCase(folderId) } returns folderToReturn
        }

        if (songsToReturn != null && folderToReturn != null) {
            coEvery { mockGetSongsByFolderUseCase(folderToReturn) } returns flowOf(songsToReturn)
        }

        viewModel = FolderFormViewModel(
            savedStateHandle = mockSavedStateHandle,
            getFolderUseCase = mockGetFolderUseCase,
            getSongsByFolderUseCase = mockGetSongsByFolderUseCase
        )
    }

    @Test
    fun `should be initialized in the Loading state when FolderFormViewModel is called`() =
        runTest {
            val folderId = null

            createViewModel(folderId = folderId)

            val state = viewModel.uiState.value

            assertEquals(UiState.Loading, state)

            coVerify { mockGetFolderUseCase wasNot Called }
        }

    @Test
    fun `should transition to Error state when data loading fails`() =
        runTest {
            val folderId = null
            val expectedException = RuntimeException("Data loading error")

            createViewModel(
                folderId = folderId,
                exceptionToThrow = expectedException
            )

            val states = viewModel.uiState.take(2).toList()
            val errorState = states[1] as UiState.Error

            assertTrue("Expected Loading state", states[0] is UiState.Loading)
            assertTrue("Expected Error state", states[1] is UiState.Error)
            assertTrue(
                "Error property should be a RuntimeException",
                errorState.error is RuntimeException
            )
            assertEquals(expectedException.message, errorState.error.message)

            coVerify(exactly = 1) { mockGetFolderUseCase(folderId) }
            coVerify { mockGetSongsByFolderUseCase wasNot Called }
        }

    @Test
    fun `should transition to Ready state when data loading is successful`() =
        runTest {
            val folder =
                Folder(id = "folder1", name = "Default", musics = 1, date = 123L, isDefault = true)
            val expectedSongs = Fixtures.mockAllSongsDto().toDomainList()
            val expectedFolderFormUiState = FolderFormUiState(
                folderUi = folder.toUiModel(),
                barTitle = FolderFormTitleMode.ViewFolder,
                songsUi = expectedSongs.toUiModelList(),
                editableState = EditableState(isEditMode = false)
            )

            createViewModel(
                folderId = folder.id,
                folderToReturn = folder,
                songsToReturn = expectedSongs
            )

            val states = mutableListOf<UiState<FolderFormUiState>>()
            val collectionJob = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.toList(states)
            }
            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

            assertTrue("Expected Loading state", states[0] is UiState.Loading)
            assertTrue("Expected Ready state", states[1] is UiState.Ready)
            assertEquals(
                UiState.Ready<FolderFormUiState>(result = expectedFolderFormUiState),
                states[1]
            )

            coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
            coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }

            collectionJob.cancel()
        }

    @Test
    fun `should set the title to NewFolder and activate edit mode when receiving a invalid folderId`() =
        runTest {
            val folderId = null
            val folder = Folder(id = "", name = "", musics = 0, date = 0L)
            val songs = emptyList<Song>()
            val expectedFolderFormUiState = FolderFormUiState(
                folderUi = folder.toUiModel(),
                barTitle = FolderFormTitleMode.NewFolder,
                songsUi = songs.toUiModelList(),
                editableState = EditableState(isEditMode = true)
            )

            createViewModel(
                folderId = folderId,
                folderToReturn = folder,
                songsToReturn = songs
            )

            val states = mutableListOf<UiState<FolderFormUiState>>()
            val collectionJob = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.toList(states)
            }

            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

            assertTrue("Expected Ready state", states[1] is UiState.Ready)
            assertEquals(
                UiState.Ready<FolderFormUiState>(expectedFolderFormUiState),
                states[1]
            )

            coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folderId) }
            coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }

            collectionJob.cancel()
        }

    @Test
    fun `should set the title to ViewFolder and not activate edit mode when receiving a valid folderId`() =
        runTest {
            val folder = Folder(id = "folder2", name = "Folder", musics = 2, date = 123L)
            val songs = emptyList<Song>()
            val expectedFolderFormUiState = FolderFormUiState(
                folderUi = folder.toUiModel(),
                barTitle = FolderFormTitleMode.ViewFolder,
                songsUi = songs.toUiModelList(),
                editableState = EditableState(isEditMode = false)
            )

            createViewModel(
                folderId = folder.id,
                folderToReturn = folder,
                songsToReturn = songs
            )

            val states = mutableListOf<UiState<FolderFormUiState>>()
            val collectionJob = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.toList(states)
            }
            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

            assertTrue("Expected Ready state", states[1] is UiState.Ready)
            assertEquals(
                UiState.Ready<FolderFormUiState>(expectedFolderFormUiState),
                states[1]
            )

            coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
            coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }

            collectionJob.cancel()
        }

    @Test
    fun `should set the title to EditFolder and activate edit mode when clicking on the edit menu with a valid folderId`() =
        runTest {
            val folder = Folder(id = "folder3", name = "Folder 3", musics = 3, date = 123L)
            val songs = emptyList<Song>()
            val expectedFolderFormUiState = FolderFormUiState(
                folderUi = folder.toUiModel(),
                barTitle = FolderFormTitleMode.EditFolder,
                songsUi = songs.toUiModelList(),
                editableState = EditableState(isEditMode = true)
            )

            createViewModel(
                folderId = folder.id,
                folderToReturn = folder,
                songsToReturn = songs
            )

            val states = mutableListOf<UiState<FolderFormUiState>>()
            val collectionJob = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.toList(states)
            }

            viewModel.enableEditMode()

            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

            assertTrue("Expected Ready state", states[1] is UiState.Ready)
            assertEquals(
                UiState.Ready<FolderFormUiState>(expectedFolderFormUiState),
                states[1]
            )

            coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
            coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }

            collectionJob.cancel()
        }

    @Test
    fun `should filter songs by title or artist when searchSongInfo is called with query data`() =
        runTest {
            val songs = Fixtures.mockAllSongsDto().toDomainList()
            val title = "Atos 2"
            val artist = "ronaldo"
            val folder =
                Folder(id = "folder1", name = "Folder", musics = 5, date = 123L, isDefault = true)

            createViewModel(
                folderId = folder.id,
                folderToReturn = folder,
                songsToReturn = songs
            )

            val states = mutableListOf<UiState<FolderFormUiState>>()
            val collectionJob = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.toList(states)
            }
            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

            val actualSongs = (states[1] as UiState.Ready).result.songsUi

            assertTrue("Expected Ready state", states[1] is UiState.Ready)
            assertEquals(songs.toUiModelList(), actualSongs)

            viewModel.searchSongInfo(title)
            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

            val expectedSongByTitle = songs.first { it.title.contains(title, ignoreCase = true) }
            val actualSongByTitle = (states[2] as UiState.Ready).result.songsUi.first {
                it.title.contains(title, ignoreCase = true)
            }
            assertEquals(3, states.size)
            assertEquals(expectedSongByTitle.toUiModel(), actualSongByTitle)

            viewModel.searchSongInfo(artist)
            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

            val expectedSongByArtist = songs.first { it.artist.contains(artist, ignoreCase = true) }
            val actualSongByArtists = (states[3] as UiState.Ready).result.songsUi.first {
                it.artist.contains(artist, ignoreCase = true)
            }
            assertEquals(4, states.size)
            assertEquals(expectedSongByArtist.toUiModel(), actualSongByArtists)

            coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
            coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }

            collectionJob.cancel()
        }

    @Test
    fun `should toggle the list edit mode when enableListEditMode is called`() =
        runTest {
            val folder = Folder(id = "folder2", name = "Folder 2", musics = 2, date = 123L)
            val songs = emptyList<Song>()

            createViewModel(
                folderId = folder.id,
                folderToReturn = folder,
                songsToReturn = songs
            )

            val states = mutableListOf<UiState<FolderFormUiState>>()
            val collectionJob = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.toList(states)
            }

            viewModel.enableListEditMode(true)
            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

            val stateWithEditEnabled =
                states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()
            assertTrue(
                "Expected isListEditMode enabled",
                stateWithEditEnabled.result.editableState.isListEditMode
            )

            viewModel.enableListEditMode(false)
            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

            val stateWithEditDisabled =
                states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()
            assertFalse(
                "Expected isListEditMode disabled",
                stateWithEditDisabled.result.editableState.isListEditMode
            )

            coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
            coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }

            collectionJob.cancel()
        }
}