package br.com.jonatas.metronomeplus.presenter.viewmodel

import androidx.lifecycle.SavedStateHandle
import br.com.jonatas.metronomeplus.MainCoroutineRule
import br.com.jonatas.metronomeplus.data.mapper.toDomainList
import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.model.Song
import br.com.jonatas.metronomeplus.domain.usecase.folderform.GetFolderUseCase
import br.com.jonatas.metronomeplus.domain.usecase.folderform.song.GetSongsByFolderUseCase
import br.com.jonatas.metronomeplus.domain.util.extensions.filterSongs
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
import java.util.Collections

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
            getSongsByFolderUseCase = mockGetSongsByFolderUseCase,
            dispatcher = mainCoroutineRule.testDispatcher
        )
    }

    @Test
    fun `should be initialized in the Loading state when FolderFormViewModel is called`() =
        runTest {
            val folderId = null
            val emptyFolder = Folder.empty()

            //Act
            createViewModel(
                folderId = folderId,
                folderToReturn = emptyFolder,
                songsToReturn = emptyList()
            )
            collectUiStates { states ->
                val stateLoading = states.filterIsInstance<UiState.Loading>().last()

                //Assert
                assertEquals(1, states.size)
                assertTrue("Expected a Loading State", stateLoading is UiState.Loading)
                coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folderId) }
                coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = emptyFolder) }
            }
        }

    @Test
    fun `should transition to Error state when data loading fails`() =
        runTest {
            val folderId = null
            val expectedException = RuntimeException("Data loading error")

            //Act
            createViewModel(exceptionToThrow = expectedException)
            collectUiStates { states ->
                mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

                val stateError = states.filterIsInstance<UiState.Error>().last()

                //Assert
                assertEquals(2, states.size)
                assertTrue(
                    "Error property should be a RuntimeException",
                    stateError.error is RuntimeException
                )
                coVerify(exactly = 1) { mockGetFolderUseCase(folderId) }
                coVerify { mockGetSongsByFolderUseCase wasNot Called }
            }
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
            val folder = Folder.empty()
            val songs = emptyList<Song>()
            val expectedFolderFormUiState = FolderFormUiState(
                folderUi = folder.toUiModel(),
                barTitle = FolderFormTitleMode.NewFolder,
                songsUi = songs.toUiModelList(),
                editableState = EditableState(isEditMode = true)
            )
            //Act
            createViewModel(
                folderId = folderId,
                folderToReturn = folder,
                songsToReturn = songs
            )
            collectUiStates { states ->
                mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

                val stateReady = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()

                //Assert
                assertEquals(2, states.size)
                assertEquals(UiState.Ready(expectedFolderFormUiState), stateReady)
                coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folderId) }
                coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }
            }
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
                editableState = EditableState(isEditMode = false, isReorderingMode = true)
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
                editableState = EditableState(isEditMode = true, isReorderingMode = true)
            )
            createViewModel(
                folderId = folder.id,
                folderToReturn = folder,
                songsToReturn = songs
            )
            collectUiStates { states ->
                //Act
                viewModel.enableEditMode()
                mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

                val stateReady = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()

                //Assert
                assertEquals(2, states.size)
                assertEquals(UiState.Ready(expectedFolderFormUiState), stateReady)
                coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
                coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }
            }
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
            val songId = "song1"
            createViewModel(
                folderId = folder.id,
                folderToReturn = folder,
                songsToReturn = songs
            )
            collectUiStates { states ->
                viewModel.enableListEditModeAndSelectSong(songId, true)
                mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()
                val editEnabled = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()

                viewModel.enableListEditModeAndSelectSong(songId, false)
                mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()
                val editDisabled =
                    states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()

                assertTrue(
                    "Expected isListEditMode enabled",
                    editEnabled.result.editableState.isListEditMode
                )
                assertFalse(
                    "Expected isListEditMode disabled",
                    editDisabled.result.editableState.isListEditMode
                )
                coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
                coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }
            }
        }

    @Test
    fun `should select song when enableListEditModeAndSelectSong is called with a valid songId`() =
        runTest {
            val folder = Folder("Folder1", "Folder 1", 5, 123L)
            val songs = Fixtures.mockAllSongsDto().toDomainList()
            val songId = songs[0].id
            val expectedSongs = songs.map { song ->
                if (song.id == songId) song.copy(selected = !song.selected)
                else song
            }
            createViewModel(
                folderId = folder.id,
                folderToReturn = folder,
                songsToReturn = songs
            )
            collectUiStates { states ->

                viewModel.enableListEditModeAndSelectSong(songId, true)
                mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

                val stateReady = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()
                assertEquals(expectedSongs.toUiModelList(), stateReady.result.songsUi)
                coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
                coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }
            }
        }

    @Test
    fun `should disable list edit mode when enableListEditModeAndSelectSong receives the same last songId`() =
        runTest {
            val folder = Folder("Folder1", "Folder 1", 5, 123L)
            val songs = Fixtures.mockAllSongsDto().toDomainList()
            val songId = songs[0].id
            val expectedSongsSelected = songs.map { song ->
                if (song.id == songId) song.copy(selected = true)
                else song
            }
            createViewModel(
                folderId = folder.id,
                folderToReturn = folder,
                songsToReturn = songs
            )
            collectUiStates { states ->

                viewModel.enableListEditModeAndSelectSong(songId, true)
                mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()
                val stateReady = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()

                viewModel.enableListEditModeAndSelectSong(songId, true)
                mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

                val stateReady2 = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()
                assertEquals(expectedSongsSelected.toUiModelList(), stateReady.result.songsUi)
                assertTrue(
                    "Expected listEditMode enabled",
                    stateReady.result.editableState.isListEditMode
                )
                assertEquals(songs.toUiModelList(), stateReady2.result.songsUi)
                assertFalse(
                    "Expected listEditMode disabled",
                    stateReady2.result.editableState.isListEditMode
                )
                coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
                coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }
            }
        }

    @Test
    fun `should enable reordering mode when it is not the default folder and the folderId is not empty`() =
        runTest {
            val folder = Folder("Folder1", "Folder 1", 0, 123L)

            createViewModel(
                folderId = folder.id,
                folderToReturn = folder,
                songsToReturn = emptyList()
            )

            val states = mutableListOf<UiState<FolderFormUiState>>()
            val collectionJob = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.toList(states)
            }

            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

            val stateReady = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()
            val actualReorderingMode = stateReady.result.editableState.isReorderingMode

            assertTrue("Expected isReorderingMode enabled", actualReorderingMode)

            coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
            coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }

            collectionJob.cancel()
        }

    @Test
    fun `should not enable reordering mode when it is the default folder`() = runTest {
        val folder = Folder("DefaultFolder", "All Songs", 0, 123L, true)

        createViewModel(
            folderId = folder.id,
            folderToReturn = folder,
            songsToReturn = emptyList()
        )

        val states = mutableListOf<UiState<FolderFormUiState>>()
        val collectionJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.toList(states)
        }

        mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val stateReady = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()
        val actualReorderingMode = stateReady.result.editableState.isReorderingMode

        assertFalse("Expected isReorderingMode disabled", actualReorderingMode)

        coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
        coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }

        collectionJob.cancel()
    }

    @Test
    fun `should not enable reordering mode when it is the empty folderId`() = runTest {
        val folder = Folder("", "", 0, 0L, false)

        createViewModel(
            folderId = folder.id,
            folderToReturn = folder,
            songsToReturn = emptyList()
        )

        val states = mutableListOf<UiState<FolderFormUiState>>()
        val collectionJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.toList(states)
        }

        mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val stateReady = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()
        val actualReorderingMode = stateReady.result.editableState.isReorderingMode

        assertFalse("Expected isReorderingMode false", actualReorderingMode)

        coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
        coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }

        collectionJob.cancel()
    }

    @Test
    fun `should not enabled reordering and swapping items when filtering songs`() = runTest {
        val folder = Fixtures.mockFoldersDto.toDomainList()[1]
        val crossRef = Fixtures.mockCrossRefList.filter { it.folderId == folder.id }
        val songs = Fixtures.mockAllSongsDto().filter { songDto ->
            crossRef.any { it.songId == songDto.id }
        }.toDomainList().filterSongs("m")
        createViewModel(
            folderId = folder.id,
            folderToReturn = folder,
            songsToReturn = songs
        )
        collectUiStates { states ->
            //Act
            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()
            viewModel.searchSongInfo("M")
            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

            val stateReady = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().first()
            val stateReady2 = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()

            //Assert
            assertTrue(stateReady.result.editableState.isReorderingMode)
            assertFalse(stateReady2.result.editableState.isReorderingMode)
            coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
            coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }
        }
    }

    @Test
    fun `should not enabled reordering and swapping items when filtering songs with whitespaces`() =
        runTest {
            val folder = Fixtures.mockFoldersDto.toDomainList()[1]
            val position = 0
            val position2 = 2
            val crossRef = Fixtures.mockCrossRefList.filter { it.folderId == folder.id }
            val songs = Fixtures.mockAllSongsDto().filter { songDto ->
                crossRef.any { it.songId == songDto.id }
            }.toDomainList().filterSongs(" ")
            createViewModel(
                folderId = folder.id,
                folderToReturn = folder,
                songsToReturn = songs
            )
            collectUiStates { states ->
                //Act
                mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()
                viewModel.searchSongInfo(" ")
                mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()
                viewModel.swapPositionItems(position, position2)
                mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

                val stateReady = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().first()
                val stateReady2 = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()

                //Assert
                assertTrue(stateReady.result.editableState.isReorderingMode)
                assertFalse(stateReady2.result.editableState.isReorderingMode)
                coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
                coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }
            }
        }

    @Test
    fun `should select a song when receive a valid songId`() = runTest {
        val folder = Folder("Folder1", "Folder 1", 0, 123L)
        val songs = Fixtures.mockAllSongsDto().toDomainList()
        val songId = songs[0].id
        val expectedSongs = songs.map { song ->
            if (song.id == songId) song.copy(selected = !song.selected)
            else song
        }
        createViewModel(
            folderId = folder.id,
            folderToReturn = folder,
            songsToReturn = songs
        )
        collectUiStates { states ->

            viewModel.toggleItemSelection(songId)
            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

            val stateReady = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()
            assertEquals(expectedSongs.toUiModelList(), stateReady.result.songsUi)

            coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
            coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }
        }
    }

    @Test
    fun `should toggle the selection state of song when receiving the same songId`() = runTest {
        val folder = Folder("Folder1", "Folder 1", 0, 123L)
        val songs = Fixtures.mockAllSongsDto().toDomainList()
        val songId = songs[0].id
        val expectedSongs = songs.map { song ->
            if (song.id == songId) song.copy(selected = true)
            else song
        }
        val expectedSongs2 = songs.map { song ->
            if (song.id == songId) song.copy(selected = false)
            else song
        }

        createViewModel(
            folderId = folder.id,
            folderToReturn = folder,
            songsToReturn = songs
        )
        collectUiStates { states ->

            viewModel.toggleItemSelection(songId)
            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

            val stateReady = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()
            assertEquals(expectedSongs.toUiModelList(), stateReady.result.songsUi)

            viewModel.toggleItemSelection(songId)
            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

            val stateReady2 = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()
            assertEquals(expectedSongs2.toUiModelList(), stateReady2.result.songsUi)

            coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
            coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }
        }
    }

    @Test
    fun `should do nothing when receiving a non-existing songId`() = runTest {
        val folder = Folder("Folder1", "Folder 1", 0, 123L)
        val songs = Fixtures.mockAllSongsDto().toDomainList()
        val songId = ""
        createViewModel(
            folderId = folder.id,
            folderToReturn = folder,
            songsToReturn = songs
        )
        collectUiStates { states ->

            viewModel.toggleItemSelection(songId)
            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

            val stateReady = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()
            assertEquals(songs.toUiModelList(), stateReady.result.songsUi)

            coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
            coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }
        }
    }

    @Test
    fun `should disable list edit mode when toggleItemSelection receives the same last songId`() =
        runTest {
            val folder = Folder("Folder1", "Folder 1", 0, 123L)
            val songs = Fixtures.mockAllSongsDto().toDomainList()
            val songId = songs[0].id
            val expectedSongsSelected = songs.map { song ->
                if (song.id == songId) song.copy(selected = true)
                else song
            }
            createViewModel(
                folderId = folder.id,
                folderToReturn = folder,
                songsToReturn = songs
            )
            collectUiStates { states ->

                viewModel.enableListEditModeAndSelectSong(songId, true)
                mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()
                val stateReady = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()

                viewModel.toggleItemSelection(songId)
                mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

                val stateReady2 = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()
                assertEquals(expectedSongsSelected.toUiModelList(), stateReady.result.songsUi)
                assertTrue(
                    "Expected listEditMode enabled",
                    stateReady.result.editableState.isListEditMode
                )
                assertEquals(songs.toUiModelList(), stateReady2.result.songsUi)
                assertFalse(
                    "Expected listEditMode disabled",
                    stateReady2.result.editableState.isListEditMode
                )
                coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
                coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }
            }
        }

    @Test
    fun `should swap the position items when receiving two different positions in swapPositionItems`() =
        runTest {
            val folder = Folder("Folder1", "Folder 1", 5, 123L)
            val fromPosition = 0
            val toPosition = 4
            val songs = Fixtures.mockAllSongsDto().toDomainList()
            val expectedSongs = songs.toMutableList().apply {
                Collections.swap(this, fromPosition, toPosition)
            }
            createViewModel(
                folderId = folder.id,
                folderToReturn = folder,
                songsToReturn = songs
            )
            collectUiStates { states ->
                //Act
                viewModel.swapPositionItems(fromPosition, toPosition)
                mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()
                viewModel.swapPositionItems(toPosition, fromPosition)
                mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

                val stateReady = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().first()
                val stateReady2 = states.filterIsInstance<UiState.Ready<FolderFormUiState>>().last()

                //Assert
                assertEquals(3, states.size)
                assertEquals(expectedSongs.toUiModelList(), stateReady.result.songsUi)
                assertEquals(songs.toUiModelList(), stateReady2.result.songsUi)
                coVerify(exactly = 1) { mockGetFolderUseCase(folderId = folder.id) }
                coVerify(exactly = 1) { mockGetSongsByFolderUseCase(folder = folder) }
            }
        }

    private fun collectUiStates(execute: (states: MutableList<UiState<FolderFormUiState>>) -> Unit) =
        runTest {
            val states = mutableListOf<UiState<FolderFormUiState>>()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.collect(states::add)
            }

            execute(states)

            job.cancel()
        }
}