package br.com.jonatas.metronomeplus.presenter.viewmodel

import androidx.lifecycle.SavedStateHandle
import br.com.jonatas.metronomeplus.MainCoroutineRule
import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.usecase.folderform.GetFolderUseCase
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModel
import br.com.jonatas.metronomeplus.presenter.model.folder.FolderFormTitleMode
import br.com.jonatas.metronomeplus.presenter.model.folder.FolderFormUiState
import br.com.jonatas.metronomeplus.presenter.model.states.UiState
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
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
        exceptionToThrow: Exception? = null
    ) {
        every { mockSavedStateHandle.get<String>("id") } returns folderId

        if (exceptionToThrow != null) {
            coEvery { mockGetFolderUseCase(folderId) } throws exceptionToThrow
        }

        if (folderToReturn != null) {
            coEvery { mockGetFolderUseCase(folderId) } returns folderToReturn
        }

        viewModel = FolderFormViewModel(
            savedStateHandle = mockSavedStateHandle,
            getFolderUseCase = mockGetFolderUseCase
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
        }

    @Test
    fun `should transition to Ready state when data loading is successful`() =
        runTest {
            val folderId = "1"
            val folder =
                Folder(id = "1", name = "Default", musics = 1, date = 123L, isDefault = true)
            val expectedFolderFormUiState = FolderFormUiState(
                folderUi = folder.toUiModel(),
                isEditMode = false,
                barTitle = FolderFormTitleMode.ViewFolder
            )

            createViewModel(
                folderId = folderId,
                folderToReturn = folder
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

            coVerify(exactly = 1) { mockGetFolderUseCase(folderId) }

            collectionJob.cancel()
        }

    @Test
    fun `should set the title to NewFolder and activate edit mode when receiving a invalid folderId`() =
        runTest {
            val folderId = null
            val folder = Folder(id = "", name = "", musics = 0, date = 0L)
            val expectedFolderFormUiState = FolderFormUiState(
                folderUi = folder.toUiModel(),
                isEditMode = true,
                barTitle = FolderFormTitleMode.NewFolder
            )

            createViewModel(
                folderId = folderId,
                folderToReturn = folder
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

            coVerify(exactly = 1) { mockGetFolderUseCase(folderId) }

            collectionJob.cancel()
        }

    @Test
    fun `should set the title to ViewFolder and not activate edit mode when receiving a valid folderId`() =
        runTest {
            val folderId = "2"
            val folder = Folder(id = "2", name = "Folder", musics = 2, date = 123L)
            val expectedFolderFormUiState = FolderFormUiState(
                folderUi = folder.toUiModel(),
                isEditMode = false,
                barTitle = FolderFormTitleMode.ViewFolder
            )

            createViewModel(
                folderId = folderId,
                folderToReturn = folder
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

            coVerify(exactly = 1) { mockGetFolderUseCase(folderId) }

            collectionJob.cancel()
        }

    @Test
    fun `should set the title to EditFolder and activate edit mode when clicking on the edit menu with a valid folderId`() =
        runTest {
            val folderId = "3"
            val folder = Folder(id = "3", name = "Folder 3", musics = 3, date = 123L)
            val expectedFolderFormUiState = FolderFormUiState(
                folderUi = folder.toUiModel(),
                isEditMode = true,
                barTitle = FolderFormTitleMode.EditFolder
            )

            createViewModel(
                folderId = folderId,
                folderToReturn = folder
            )

            val states = mutableListOf<UiState<FolderFormUiState>>()
            val collectionJob = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.toList(states)
            }

            viewModel.onEditClicked()

            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

            assertTrue("Expected Ready state", states[1] is UiState.Ready)
            assertEquals(
                UiState.Ready<FolderFormUiState>(expectedFolderFormUiState),
                states[1]
            )

            coVerify(exactly = 1) { mockGetFolderUseCase(folderId) }

            collectionJob.cancel()
        }
}