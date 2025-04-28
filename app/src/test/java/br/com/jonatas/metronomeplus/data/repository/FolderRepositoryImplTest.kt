package br.com.jonatas.metronomeplus.data.repository

import br.com.jonatas.metronomeplus.data.model.FolderDto
import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.source.FolderDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class FolderRepositoryImplTest {

    private val mockFolderDataSource = mockk<FolderDataSource>()
    private val folderRepository = FolderRepositoryImpl(mockFolderDataSource)

    @Test
    fun `should return a list of folders when getFolders is called `() = runTest {
        val foldersDto = listOf(
            FolderDto(id = "1", name = "Folder", musics = 1, date = 1735689600000),
            FolderDto(id = "2", name = "Folder 2", musics = 3, date = 1735689600000),
            FolderDto(id = "3", name = "Folder 3", musics = 5, date = 1735689600000),
            FolderDto(id = "4", name = "Folder 4", musics = 0, date = 1735689600000),
        )
        val expectedFolders = listOf(
            Folder(id = "1", name = "Folder", musics = 1, date = 1735689600000),
            Folder(id = "2", name = "Folder 2", musics = 3, date = 1735689600000),
            Folder(id = "3", name = "Folder 3", musics = 5, date = 1735689600000),
            Folder(id = "4", name = "Folder 4", musics = 0, date = 1735689600000),
        )

        coEvery { mockFolderDataSource.getFolders() } returns foldersDto
        val actualFolders = folderRepository.getFolders()

        assertEquals(expectedFolders, actualFolders)
        coVerify(exactly = 1) { mockFolderDataSource.getFolders() }
    }

    @Test
    fun `should propagate exception from DataSource when getFolders fails`() = runTest {
        val expectedMessageError = "Data loading failure"
        coEvery { mockFolderDataSource.getFolders() } throws (RuntimeException(expectedMessageError))

        try {
            folderRepository.getFolders()
            fail("was supposed to throw an exception but failed")
        } catch (e: Exception) {
            assertEquals(expectedMessageError, e.message)
        }

        coVerify(exactly = 1) { mockFolderDataSource.getFolders() }
    }
}