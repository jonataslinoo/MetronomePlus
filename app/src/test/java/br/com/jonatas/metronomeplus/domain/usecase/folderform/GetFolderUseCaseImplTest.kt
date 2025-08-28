package br.com.jonatas.metronomeplus.domain.usecase.folderform

import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.repository.FolderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetFolderUseCaseImplTest {

    val mockRepository = mockk<FolderRepository>()
    val getFolderUseCase = GetFolderUseCaseImpl(mockRepository)

    @Test
    fun `should return a folder correctly when it receives an valid folder id`() = runTest {
        val folderId = "1"
        val expectedFolder =
            Folder(id = "1", name = "Folder", musics = 1, date = 123L, isDefault = true)

        coEvery { mockRepository.getFolder(folderId) } returns expectedFolder

        val actualFolder = getFolderUseCase(folderId)

        assertEquals(expectedFolder, actualFolder)

        coVerify(exactly = 1) { mockRepository.getFolder(folderId) }
    }

    @Test
    fun `should return a empty folder when it receives an invalid folder id`() = runTest {
        val folderId = null
        val expectedEmptyFolder =
            Folder(id = "", name = "", musics = 0, date = 0L, isDefault = false)

        coEvery { mockRepository.getFolder(any()) } returns null

        val actualFolder = getFolderUseCase(folderId)

        assertEquals(expectedEmptyFolder, actualFolder)

        coVerify(exactly = 1) { mockRepository.getFolder(any()) }
    }
}