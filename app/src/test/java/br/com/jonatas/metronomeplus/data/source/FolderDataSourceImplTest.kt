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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
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
                FolderDto(id = "1", name = "Folder", musics = 1, date = 1735689600000),
                FolderDto(id = "2", name = "Folder 2", musics = 3, date = 1735689600000),
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

            assertEquals(actualFolders.size, 4)
            assertEquals(actualFolders[0].id, "1")
            assertEquals(actualFolders[0].name, "Folder")
            assertEquals(actualFolders[0].isDefault, true)

            coVerify(exactly = 1) { mockDataStoreManager.getData(FOLDERS_KEY) }
            coVerify(exactly = 1) { mockDataStoreManager.setData(any(), any()) }
        }
}