package br.com.jonatas.metronomeplus.domain.usecase.library

import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.repository.FolderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class GetFoldersUseCaseImplTest {

    private val mockFolderRepository = mockk<FolderRepository>()
    private var getFoldersUseCase = GetFoldersUseCaseImpl(mockFolderRepository)

    @Test
    fun `should return a list of folders when it succeeds in returning the data`() = runTest {
        val expectedFolders = flowOf(
            listOf(
                Folder(id = "1", name = "Folder", musics = 1, date = 1735689600000),
                Folder(id = "2", name = "Folder 2", musics = 3, date = 1735689600000),
                Folder(id = "3", name = "Folder 3", musics = 5, date = 1735689600000),
                Folder(id = "4", name = "Folder 4", musics = 0, date = 1735689600000),
            )
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

    @Test
    fun `should return a list of folders when it succeeds in returning the data with many emissions`() =
        runTest {
            val folders = listOf(
                Folder(id = "1", name = "Folder", musics = 1, date = 123L, isDefault = true),
                Folder(id = "2", name = "Folder 2", musics = 2, date = 123L),
            )
            val folders2 = folders +
                    Folder(id = "3", name = "Folder 3", musics = 3, date = 123L)

            val folders3 = folders2 +
                    Folder(id = "4", name = "Folder 4", musics = 0, date = 123L)

            coEvery { mockFolderRepository.getFolders() } returns flow {
                emit(folders)
                delay(100)
                emit(folders2)
                delay(100)
                emit(folders3)
            }

            val actualFolders = getFoldersUseCase()

            val actual = actualFolders.toList()
            val expected = listOf(
                folders,
                folders2,
                folders3
            )

            assertEquals(expected, actual)
            assertEquals(2, actual[0].size)
            assertEquals("Folder", actual[0][0].name)
            assertEquals(true, actual[0][0].isDefault)
            assertEquals(3, actual[1].size)
            assertEquals("Folder 2", actual[1][1].name)
            assertEquals(4, actual[2].size)
            assertEquals("Folder 4", actual[2][3].name)

            coVerify(exactly = 1) { mockFolderRepository.getFolders() }
        }
}