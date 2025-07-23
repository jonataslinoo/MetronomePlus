package br.com.jonatas.metronomeplus.data.repository

import br.com.jonatas.metronomeplus.data.mapper.toDomainList
import br.com.jonatas.metronomeplus.data.model.FolderDto
import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.source.FolderDataSource
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FolderRepositoryImplTest {

    private val mockFolderDataSource = mockk<FolderDataSource>()
    private val testDispatcher = StandardTestDispatcher()
    private val folderRepository = FolderRepositoryImpl(mockFolderDataSource, testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should return a list of folders when getFolders is called `() = runTest {
        val foldersDto = listOf(
            FolderDto(id = "1", name = "Folder", musics = 1, date = 123L, isDefault = true),
            FolderDto(id = "2", name = "Folder 2", musics = 3, date = 123L),
            FolderDto(id = "3", name = "Folder 3", musics = 5, date = 123L),
            FolderDto(id = "4", name = "Folder 4", musics = 0, date = 123L),
        )

        coEvery { mockFolderDataSource.getFolders() } returns flowOf(foldersDto)

        val actualFolders = folderRepository.getFolders().first()

        val expectedFolders = foldersDto.toDomainList()

        assertEquals(expectedFolders, actualFolders)
        coVerify(exactly = 1) { mockFolderDataSource.getFolders() }
    }

    @Test
    fun `should return a list of folders when getFolders is called with many emissions`() =
        runTest(testDispatcher) {
            val foldersDto = listOf(
                FolderDto(id = "1", name = "Folder", musics = 1, date = 123L, isDefault = true),
                FolderDto(id = "2", name = "Folder 2", musics = 2, date = 123L),
            )

            val foldersDto2 =
                foldersDto + FolderDto(id = "3", name = "Folder 3", musics = 3, date = 123L)

            coEvery { mockFolderDataSource.getFolders() } returns flow {
                emit(foldersDto)
                delay(100)
                emit(foldersDto2)
            }

            val actualFolders = folderRepository.getFolders()
            val actual = actualFolders.toList()

            val expected = listOf(
                foldersDto.toDomainList(),
                foldersDto2.toDomainList()
            )

            assertEquals(expected, actual)
            assertEquals(2, actual.size)
            assertEquals(2, actual[0].size)
            assertEquals(3, actual[1].size)

            coVerify(exactly = 1) { mockFolderDataSource.getFolders() }
        }

    @Test
    fun `should propagate exception from DataSource when getFolders fails`() =
        runTest(testDispatcher) {
            val expectedMessageError = "Data loading failure"
            coEvery { mockFolderDataSource.getFolders() } throws (RuntimeException(
                expectedMessageError
            ))

            try {
                folderRepository.getFolders()
                fail("was supposed to throw an exception but failed")
            } catch (e: Exception) {
                assertEquals(expectedMessageError, e.message)
            }

            coVerify(exactly = 1) { mockFolderDataSource.getFolders() }
        }

    @Test
    fun `should save a folder when receiving a new folder`() = runTest(testDispatcher) {
        val expectedNewFolder = Folder(
            id = "10", name = "Folder 10", musics = 10, date = 123L
        )

        val folderSlot = slot<FolderDto>()
        coEvery { mockFolderDataSource.save(capture(folderSlot)) } just Runs

        folderRepository.save(expectedNewFolder)

        val actualCapturedFolder = folderSlot.captured
        assertEquals(expectedNewFolder.id, actualCapturedFolder.id)
        assertEquals(expectedNewFolder.name, actualCapturedFolder.name)
        assertEquals(expectedNewFolder.musics, actualCapturedFolder.musics)
        assertEquals(expectedNewFolder.date, actualCapturedFolder.date)
        assertEquals(expectedNewFolder.isDefault, actualCapturedFolder.isDefault)

        coVerify(exactly = 1) { mockFolderDataSource.save(any()) }
    }

    @Test
    fun `should edit the folder when receiving an existing folder`() = runTest(testDispatcher) {
        val folders = listOf(
            FolderDto(id = "1", name = "Folder", musics = 1, 123L, isDefault = true),
            FolderDto(id = "2", name = "Folder 2", musics = 3, date = 123L),
        )

        coEvery { mockFolderDataSource.getFolders() } returns flowOf(folders)

        val expectedEditFolder = folderRepository.getFolders().first()[1].copy(
            name = "Edit Folder 2",
            musics = 2,
            date = 123L
        )

        val folderSlot = slot<FolderDto>()
        coEvery { mockFolderDataSource.save(capture(folderSlot)) } just Runs

        folderRepository.save(expectedEditFolder)

        val actualCapturedFolder = folderSlot.captured
        assertEquals(expectedEditFolder.id, actualCapturedFolder.id)
        assertEquals(expectedEditFolder.name, actualCapturedFolder.name)
        assertEquals(expectedEditFolder.musics, actualCapturedFolder.musics)
        assertEquals(expectedEditFolder.date, actualCapturedFolder.date)
        assertEquals(expectedEditFolder.isDefault, actualCapturedFolder.isDefault)

        coVerify(exactly = 1) { mockFolderDataSource.getFolders() }
        coVerify(exactly = 1) { mockFolderDataSource.save(any()) }
    }
}