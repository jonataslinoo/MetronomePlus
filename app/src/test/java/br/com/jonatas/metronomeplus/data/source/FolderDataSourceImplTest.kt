package br.com.jonatas.metronomeplus.data.source

import androidx.datastore.preferences.core.stringPreferencesKey
import br.com.jonatas.metronomeplus.data.local.DataStoreManager
import br.com.jonatas.metronomeplus.data.model.FolderDto
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test


class FolderDataSourceImplTest {

    private val mockDataStoreManager = mockk<DataStoreManager>()
    private val folderDataSource = FolderDataSourceImpl(mockDataStoreManager)

    companion object {
        private val FOLDERS_KEY = stringPreferencesKey("FOLDERS_KEY")
    }

    @Test
    fun `should return a list of folders when getFolders is called with saved data`() =
        runTest {
            val expectedFolders = listOf(
                FolderDto(id = "1", name = "Folder", musics = 1, date = 123L, isDefault = true),
                FolderDto(id = "2", name = "Folder 2", musics = 3, date = 123L),
            )
            val jsonString = Json.encodeToString(expectedFolders)

            every { mockDataStoreManager.getData(FOLDERS_KEY) } returns flowOf(jsonString)

            val resultFlow = folderDataSource.getFolders()
            val actualFolders = resultFlow.first()

            assertEquals(expectedFolders.size, actualFolders.size)
            assertEquals(expectedFolders[0].id, actualFolders[0].id)
            assertEquals(expectedFolders, actualFolders)
            coVerify(exactly = 1) { mockDataStoreManager.getData(FOLDERS_KEY) }
            coVerify(exactly = 0) { mockDataStoreManager.setData(any(), any()) }
        }

    @Test
    fun `should return mock folders and save them to the DataStore when getFolders is called without any saved data`() =
        runTest {
            every { mockDataStoreManager.getData(FOLDERS_KEY) } returns flowOf("")
            coEvery { mockDataStoreManager.setData(FOLDERS_KEY, any()) } just Runs

            val resultFlow = folderDataSource.getFolders()
            val actualFolders = resultFlow.first()

            assertEquals(4, actualFolders.size)
            assertEquals("1", actualFolders[0].id)
            assertEquals("Folder", actualFolders[0].name)
            assertEquals(true, actualFolders[0].isDefault)

            coVerify(exactly = 1) { mockDataStoreManager.getData(FOLDERS_KEY) }
            coVerify(exactly = 1) { mockDataStoreManager.setData(any(), any()) }
        }

    @Test
    fun `should return a list of folders when getFolders is called with saved data and multiple emissions`() =
        runTest {
            val foldersDto = listOf(
                FolderDto(id = "1", name = "Folder", musics = 1, date = 123L, isDefault = true),
                FolderDto(id = "2", name = "Folder 2", musics = 2, date = 123L),
            )
            val jsonString = Json.encodeToString(foldersDto)
            val foldersDto2 =
                foldersDto + FolderDto(id = "3", name = "Folder 3", musics = 3, date = 123L)
            val jsonString2 = Json.encodeToString(foldersDto2)

            every { mockDataStoreManager.getData(FOLDERS_KEY) } returns flow {
                emit(jsonString)
                delay(100)
                emit(jsonString2)
            }

            val resultFlow = folderDataSource.getFolders()
            val actualFolders = resultFlow.toList()

            val expectedFolders = listOf(
                foldersDto,
                foldersDto2
            )

            assertEquals(expectedFolders, actualFolders)
            assertEquals(2, actualFolders.size)
            assertEquals(2, actualFolders[0].size)
            assertEquals(3, actualFolders[1].size)
            coVerify(exactly = 1) { mockDataStoreManager.getData(FOLDERS_KEY) }
            coVerify(exactly = 0) { mockDataStoreManager.setData(any(), any()) }
        }

    @Test
    fun `should append a new folder to the existing list and persist it correctly`() = runTest {
        val appendFolderDto = FolderDto(id = "3", name = "Folder 3", musics = 3, date = 123L)
        val foldersDto = listOf(
            FolderDto(id = "1", name = "Folder", musics = 1, date = 123L, isDefault = true),
            FolderDto(id = "2", name = "Folder 2", musics = 3, date = 123L),
        )
        val jsonString = Json.encodeToString(foldersDto)
        every { mockDataStoreManager.getData(FOLDERS_KEY) } returns flowOf(jsonString)

        val capturedJson = slot<String>()
        coEvery { mockDataStoreManager.setData(FOLDERS_KEY, capture(capturedJson)) } just Runs

        folderDataSource.save(appendFolderDto)

        val finalList = Json.decodeFromString<List<FolderDto>>(capturedJson.captured)
        val appended = finalList.find { it.id == appendFolderDto.id }

        assertEquals(3, finalList.size)
        assertNotNull(appended)
        assertEquals(appendFolderDto.id, appended!!.id)
        assertEquals(appendFolderDto.name, appended.name)
        assertEquals(appendFolderDto.musics, appended.musics)
        assertEquals(appendFolderDto.date, appended.date)
        assertEquals(appendFolderDto.isDefault, appended.isDefault)

        verify(exactly = 1) { mockDataStoreManager.getData(FOLDERS_KEY) }
        coVerify(exactly = 1) { mockDataStoreManager.setData(any(), capture(capturedJson)) }
    }

    @Test
    fun `should update a existing folder to the list and persist it correctly`() = runTest {
        val updateFolderDto = FolderDto(id = "2", name = "Folder 20", musics = 2, date = 123L)
        val foldersDto = listOf(
            FolderDto(id = "1", name = "Folder", musics = 1, date = 123L, isDefault = true),
            FolderDto(id = "2", name = "Folder 2", musics = 3, date = 123L),
        )
        val jsonString = Json.encodeToString(foldersDto)
        every { mockDataStoreManager.getData(FOLDERS_KEY) } returns flowOf(jsonString)

        val capturedJson = slot<String>()
        coEvery { mockDataStoreManager.setData(FOLDERS_KEY, capture(capturedJson)) } just Runs

        folderDataSource.save(updateFolderDto)

        val finalList = Json.decodeFromString<List<FolderDto>>(capturedJson.captured)
        val updated: FolderDto? = finalList.find { it.id == updateFolderDto.id }

        assertNotNull(updated)
        assertEquals(2, finalList.size)
        assertEquals(updateFolderDto.id, updated!!.id)
        assertEquals(updateFolderDto.name, updated.name)
        assertEquals(updateFolderDto.musics, updated.musics)
        assertEquals(updateFolderDto.date, updated.date)
        assertEquals(updateFolderDto.isDefault, updated.isDefault)

        verify(exactly = 1) { mockDataStoreManager.getData(FOLDERS_KEY) }
        coVerify(exactly = 1) { mockDataStoreManager.setData(any(), any()) }
    }
}