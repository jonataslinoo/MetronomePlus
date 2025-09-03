package br.com.jonatas.metronomeplus.data.source

import br.com.jonatas.metronomeplus.data.local.DataStoreManager
import br.com.jonatas.metronomeplus.data.model.FolderDto
import br.com.jonatas.metronomeplus.data.model.FolderSongCrossRefDto
import br.com.jonatas.metronomeplus.domain.source.FolderDataSource
import br.com.jonatas.metronomeplus.util.Fixtures
import br.com.jonatas.metronomeplus.util.Fixtures.CROSS_REF_KEY
import br.com.jonatas.metronomeplus.util.Fixtures.FOLDERS_KEY
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test


class FolderDataSourceImplTest {

    @MockK
    private lateinit var mockDataStoreManager: DataStoreManager
    private lateinit var folderDataSource: FolderDataSource

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        folderDataSource = FolderDataSourceImpl(mockDataStoreManager)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should return a list of folders when getFolders is called with saved data`() =
        runTest {
            val expectedFolders = Fixtures.mockFoldersDto
            val jsonString = Json.encodeToString(expectedFolders)
            every { mockDataStoreManager.getData(FOLDERS_KEY) } returns flowOf(jsonString)

            val crossRefList = Fixtures.mockCrossRefList
            val jsonEmptyListString = Json.encodeToString(crossRefList)
            coEvery { mockDataStoreManager.getData(CROSS_REF_KEY) } returns flowOf(
                jsonEmptyListString
            )

            val resultFlow = folderDataSource.getFolders()
            val actualFolders = resultFlow.first()

            assertEquals(expectedFolders.toSet(), actualFolders.toSet())
            verify(exactly = 1) { mockDataStoreManager.getData(FOLDERS_KEY) }
            verify(exactly = 1) { mockDataStoreManager.getData(CROSS_REF_KEY) }
            coVerify(exactly = 0) { mockDataStoreManager.setData(any(), any()) }
        }

    @Test
    fun `should return mock folders and save them to the DataStore when getFolders is called without any saved data`() =
        runTest {
            every { mockDataStoreManager.getData(FOLDERS_KEY) } returns flowOf("")

            val crossRefJsonString = Json.encodeToString(emptyList<FolderSongCrossRefDto>())
            coEvery { mockDataStoreManager.getData(CROSS_REF_KEY) } returns flowOf(
                crossRefJsonString
            )

            coEvery { mockDataStoreManager.setData(FOLDERS_KEY, any()) } just Runs

            val resultFlow = folderDataSource.getFolders()
            val actualFolders = resultFlow.first()

            assertEquals(4, actualFolders.size)
            assertEquals("folder1", actualFolders[0].id)
            assertEquals("Folder", actualFolders[0].name)
            assertEquals(true, actualFolders[0].isDefault)

            verify(exactly = 1) { mockDataStoreManager.getData(FOLDERS_KEY) }
            verify(exactly = 1) { mockDataStoreManager.getData(CROSS_REF_KEY) }
            coVerify(exactly = 1) { mockDataStoreManager.setData(any(), any()) }
        }

    @Test
    fun `should return a list of folders when getFolders is called with saved data and multiple emissions`() =
        runTest {
            val foldersDto = Fixtures.mockFoldersDto
            val jsonString = Json.encodeToString(foldersDto)
            val foldersDto2 =
                foldersDto + FolderDto(id = "folder6", name = "Folder 3", musics = 1, date = 123L)
            val jsonString2 = Json.encodeToString(foldersDto2)

            every { mockDataStoreManager.getData(FOLDERS_KEY) } returns flow {
                emit(jsonString)
                delay(100)
                emit(jsonString2)
            }

            val crossRefList = Fixtures.mockCrossRefList + FolderSongCrossRefDto(
                folderId = "folder6",
                songId = "song4"
            )
            val crossRefJsonString = Json.encodeToString(crossRefList)
            coEvery { mockDataStoreManager.getData(CROSS_REF_KEY) } returns flowOf(
                crossRefJsonString
            )

            val resultFlow = folderDataSource.getFolders()
            val actualFolders = resultFlow.toList()

            val expectedFolders = listOf(
                foldersDto,
                foldersDto2
            )

            assertEquals(expectedFolders, actualFolders)
            verify(exactly = 1) { mockDataStoreManager.getData(FOLDERS_KEY) }
            verify(exactly = 1) { mockDataStoreManager.getData(CROSS_REF_KEY) }
            coVerify(exactly = 0) { mockDataStoreManager.setData(any(), any()) }
        }

    @Test
    fun `should append a new folder to the existing list and persist it correctly`() = runTest {
        val appendFolderDto = FolderDto(id = "folder30", name = "Folder 3", musics = 3, date = 123L)
        val foldersDto = Fixtures.mockFoldersDto
        val jsonString = Json.encodeToString(foldersDto)
        every { mockDataStoreManager.getData(FOLDERS_KEY) } returns flowOf(jsonString)

        val crossRefJsonString = Json.encodeToString(emptyList<FolderSongCrossRefDto>())
        coEvery { mockDataStoreManager.getData(CROSS_REF_KEY) } returns flowOf(crossRefJsonString)

        val capturedJson = slot<String>()
        coEvery { mockDataStoreManager.setData(FOLDERS_KEY, capture(capturedJson)) } just Runs

        folderDataSource.save(appendFolderDto)

        val finalList = Json.decodeFromString<List<FolderDto>>(capturedJson.captured)
        val appended = finalList.find { it.id == appendFolderDto.id }
        val finalSize = foldersDto.size + 1


        assertEquals(finalSize, finalList.size)
        assertNotNull(appended)
        assertEquals(appendFolderDto.id, appended!!.id)
        assertEquals(appendFolderDto.name, appended.name)
        assertEquals(appendFolderDto.musics, appended.musics)
        assertEquals(appendFolderDto.date, appended.date)
        assertEquals(appendFolderDto.isDefault, appended.isDefault)

        verify(exactly = 1) { mockDataStoreManager.getData(FOLDERS_KEY) }
        verify(exactly = 1) { mockDataStoreManager.getData(CROSS_REF_KEY) }
        coVerify(exactly = 1) { mockDataStoreManager.setData(any(), capture(capturedJson)) }
    }

    @Test
    fun `should update a existing folder to the list and persist it correctly`() = runTest {
        val updateFolderDto = FolderDto(id = "folder2", name = "Folder 20", musics = 2, date = 123L)
        val foldersDto = Fixtures.mockFoldersDto
        val jsonString = Json.encodeToString(foldersDto)
        every { mockDataStoreManager.getData(FOLDERS_KEY) } returns flowOf(jsonString)

        val crossRefJsonString = Json.encodeToString(emptyList<FolderSongCrossRefDto>())
        coEvery { mockDataStoreManager.getData(CROSS_REF_KEY) } returns flowOf(crossRefJsonString)

        val capturedJson = slot<String>()
        coEvery { mockDataStoreManager.setData(FOLDERS_KEY, capture(capturedJson)) } just Runs

        folderDataSource.save(updateFolderDto)

        val finalList = Json.decodeFromString<List<FolderDto>>(capturedJson.captured)
        val updated: FolderDto? = finalList.find { it.id == updateFolderDto.id }

        assertNotNull(updated)
        assertEquals(foldersDto.size, finalList.size)
        assertEquals(updateFolderDto.id, updated!!.id)
        assertEquals(updateFolderDto.name, updated.name)
        assertEquals(updateFolderDto.musics, updated.musics)
        assertEquals(updateFolderDto.date, updated.date)
        assertEquals(updateFolderDto.isDefault, updated.isDefault)

        verify(exactly = 1) { mockDataStoreManager.getData(FOLDERS_KEY) }
        verify(exactly = 1) { mockDataStoreManager.getData(CROSS_REF_KEY) }
        coVerify(exactly = 1) { mockDataStoreManager.setData(any(), any()) }
    }

    @Test
    fun `should return correctly folder when its receives a folder id`() = runTest {
        val foldersDto = Fixtures.mockFoldersDto
        val jsonString = Json.encodeToString(foldersDto)
        every { mockDataStoreManager.getData(FOLDERS_KEY) } returns flowOf(jsonString)

        val actualFolder = folderDataSource.getFolder("folder1")

        assertNotNull(actualFolder)
        assertTrue(actualFolder!!.isDefault)
        assertEquals(foldersDto[0], actualFolder)
        verify(exactly = 1) { mockDataStoreManager.getData(FOLDERS_KEY) }
        verify(exactly = 0) { mockDataStoreManager.getData(CROSS_REF_KEY) }
        coVerify(exactly = 0) { mockDataStoreManager.setData(any(), any()) }
    }

    @Test
    fun `should return a null value when its receives an empty folder id`() = runTest {
        val foldersDto = Fixtures.mockFoldersDto
        val jsonString = Json.encodeToString(foldersDto)
        every { mockDataStoreManager.getData(FOLDERS_KEY) } returns flowOf(jsonString)

        val actualFolder = folderDataSource.getFolder("")

        assertNull("Expected folder to be null", actualFolder)
        verify(exactly = 1) { mockDataStoreManager.getData(FOLDERS_KEY) }
        verify(exactly = 0) { mockDataStoreManager.getData(CROSS_REF_KEY) }
        coVerify(exactly = 0) { mockDataStoreManager.setData(any(), any()) }
    }

    @Test
    fun `should return a null value when its receives an invalid folder id`() = runTest {
        val foldersDto = Fixtures.mockFoldersDto
        val jsonString = Json.encodeToString(foldersDto)
        every { mockDataStoreManager.getData(FOLDERS_KEY) } returns flowOf(jsonString)

        val actualFolder = folderDataSource.getFolder("teste")

        assertNull("Expected folder to be null", actualFolder)

        verify(exactly = 1) { mockDataStoreManager.getData(FOLDERS_KEY) }
        verify(exactly = 0) { mockDataStoreManager.getData(CROSS_REF_KEY) }
        coVerify(exactly = 0) { mockDataStoreManager.setData(any(), any()) }
    }

    @Test
    fun `should throw exception when DataStore fails`() = runTest {
        val expectedMessageError = "DB error"
        coEvery { mockDataStoreManager.getData(FOLDERS_KEY) } throws RuntimeException(
            expectedMessageError
        )

        try {
            folderDataSource.getFolder("")
            fail("was supposed to throw an exception but failed")
        } catch (e: Throwable) {
            assertTrue(e is RuntimeException)
            assertEquals(expectedMessageError, e.message)
        }

        verify(exactly = 1) { mockDataStoreManager.getData(FOLDERS_KEY) }
        verify(exactly = 0) { mockDataStoreManager.getData(CROSS_REF_KEY) }
        coVerify(exactly = 0) { mockDataStoreManager.setData(any(), any()) }
    }
}