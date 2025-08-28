package br.com.jonatas.metronomeplus.presenter.viewmodel

import androidx.lifecycle.SavedStateHandle
import br.com.jonatas.metronomeplus.MainCoroutineRule
import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.usecase.folderform.GetFolderUseCase
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModel
import br.com.jonatas.metronomeplus.presenter.model.FolderUiModel
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

            createViewModel(
                folderId = folderId,
                folderToReturn = folder
            )

            val states = mutableListOf<UiState<FolderUiModel>>()
            val collectionJob = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.toList(states)
            }
            mainCoroutineRule.testDispatcher.scheduler.advanceUntilIdle()

            assertTrue("Expected Loading state", states[0] is UiState.Loading)
            assertTrue("Expected Ready state", states[1] is UiState.Ready)
            assertEquals(
                UiState.Ready<FolderUiModel>(result = folder.toUiModel()),
                states[1]
            )

            coVerify(exactly = 1) { mockGetFolderUseCase(folderId) }

            collectionJob.cancel()
        }
}