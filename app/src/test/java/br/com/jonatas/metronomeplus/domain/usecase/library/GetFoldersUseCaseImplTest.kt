package br.com.jonatas.metronomeplus.domain.usecase.library

import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.repository.FolderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class GetFoldersUseCaseImplTest {

    private val mockFolderRepository = mockk<FolderRepository>()
    private var getFoldersUseCase = GetFoldersUseCaseImpl(mockFolderRepository)

    @Test
    fun `should return a list of folders when it succeeds in returning the data`() = runTest {
        val expectedFolders = listOf(
            Folder(id = "1", name = "Folder", musics = 1, date = 1735689600000),
            Folder(id = "2", name = "Folder 2", musics = 3, date = 1735689600000),
            Folder(id = "3", name = "Folder 3", musics = 5, date = 1735689600000),
            Folder(id = "4", name = "Folder 4", musics = 0, date = 1735689600000),
        )
        coEvery { mockFolderRepository.getFolders() } returns expectedFolders

        val actualFolders = getFoldersUseCase()

        coVerify(exactly = 1) { mockFolderRepository.getFolders() }
        assertEquals(expectedFolders, actualFolders)
    }

    @Test
    fun `should throw an exception when it fails to return the data`() = runTest {
        val expectedMessageError = "Data Loading failure"
        coEvery { mockFolderRepository.getFolders() } throws (RuntimeException(expectedMessageError))

        try {
            getFoldersUseCase()
            fail("was supposed to throw an exception but failed")
        } catch (e: Exception) {
            assertEquals(expectedMessageError, e.message)
        }

        coVerify(exactly = 1) { mockFolderRepository.getFolders() }
    }
}