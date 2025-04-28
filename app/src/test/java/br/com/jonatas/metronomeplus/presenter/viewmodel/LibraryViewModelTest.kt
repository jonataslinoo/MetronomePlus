package br.com.jonatas.metronomeplus.presenter.viewmodel

import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.usecase.library.GetFoldersUseCase
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModelList
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val mockGetFoldersUseCase = mockk<GetFoldersUseCase>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private val viewModel by lazy {
        LibraryViewModel(
            getFoldersUseCase = mockGetFoldersUseCase,
            dispatcher = testDispatcher
        )
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should start library in Loading state when LibraryViewModel is initialized`() =
        runTest(testDispatcher) {
            val expectedState = LibraryViewModel.LibraryState.Loading

            assertEquals(expectedState, viewModel.uiState.first())

            coVerify { mockGetFoldersUseCase wasNot Called }
        }

    @Test
    fun `should transition to Error state when data loading fails`() = runTest(testDispatcher) {
        val expectedMessageError = "Data loading failure"
        coEvery { mockGetFoldersUseCase() } throws RuntimeException(expectedMessageError)

        val stateLoading = viewModel.uiState.first()
        assertTrue(stateLoading is LibraryViewModel.LibraryState.Loading)

        advanceUntilIdle()
        val stateError = viewModel.uiState.first()
        val expectedState = LibraryViewModel.LibraryState.Error(expectedMessageError)
        assertTrue(stateError is LibraryViewModel.LibraryState.Error)
        assertEquals(expectedState, stateError)

        coVerify(exactly = 1) { mockGetFoldersUseCase() }
    }

    @Test
    fun `should transition to Ready state when data loading is successful`() =
        runTest(testDispatcher) {
            val expectedFolders = listOf(
                Folder(id = "1", name = "Folder", musics = 1, date = 1735689600000),
                Folder(id = "2", name = "Folder 2", musics = 3, date = 1735689600000),
                Folder(id = "3", name = "Folder 3", musics = 5, date = 1735689600000),
                Folder(id = "4", name = "Folder 4", musics = 0, date = 1735689600000),
            )

            coEvery { mockGetFoldersUseCase() } returns expectedFolders

            val stateLoading = viewModel.uiState.first()
            assertTrue(stateLoading is LibraryViewModel.LibraryState.Loading)

            advanceUntilIdle()
            val expectedState =
                LibraryViewModel.LibraryState.Ready(foldersUi = expectedFolders.toUiModelList())
            val stateReady = viewModel.uiState.first()
            assertTrue(stateReady is LibraryViewModel.LibraryState.Ready)
            assertEquals(stateReady, expectedState)

            coVerify(exactly = 1) { mockGetFoldersUseCase() }
        }
}