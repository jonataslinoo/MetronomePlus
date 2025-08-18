package br.com.jonatas.metronomeplus.domain.usecase.folderform

import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.repository.FolderRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveFolderUseCaseImplTest {

    private val mockRepository = mockk<FolderRepository>()
    private val saveFolderUseCase = SaveFolderUseCaseImpl(mockRepository)

    @Test
    fun `should not edit the default folder when she it is selected`() = runTest {
        val defaultFolder =
            Folder(id = "1", name = "Folder", musics = 10, date = 123L, isDefault = true)

        saveFolderUseCase(defaultFolder)

        coVerify(exactly = 0) { mockRepository.save(any()) }
    }

    @Test
    fun `should edit the folder when receiving an existing folder`() = runTest {
        val editFolder = Folder(id = "10", name = "Folder 3", musics = 3, date = 123L)

        val folderSlot = slot<Folder>()
        coEvery { mockRepository.save(capture(folderSlot)) } just Runs

        saveFolderUseCase(editFolder)

        val capturedFolder = folderSlot.captured
        assertEquals(editFolder.id, capturedFolder.id)
        assertEquals(editFolder.name, capturedFolder.name)
        assertEquals(editFolder.musics, capturedFolder.musics)
        assertEquals(editFolder.date, capturedFolder.date)
        assertEquals(editFolder.isDefault, capturedFolder.isDefault)

        coVerify(exactly = 1) { mockRepository.save(any()) }
    }

    @Test
    fun `should save a folder when receiving a new folder`() = runTest {
        val newFolder = Folder(id = "", name = "New Folder", musics = 10, date = 123L)

        val folderSlot = slot<Folder>()
        coEvery { mockRepository.save(capture(folderSlot)) } just Runs

        saveFolderUseCase(newFolder)

        val capturedFolder = folderSlot.captured
        assertTrue(capturedFolder.id.isNotEmpty())
        assertEquals(newFolder.name, capturedFolder.name)
        assertEquals(newFolder.musics, capturedFolder.musics)
        assertEquals(newFolder.date, capturedFolder.date)
        assertEquals(newFolder.isDefault, capturedFolder.isDefault)

        coVerify(exactly = 1) { mockRepository.save(any()) }
    }
}