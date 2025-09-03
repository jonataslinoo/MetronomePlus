package br.com.jonatas.metronomeplus.domain.usecase.folderform.song

import br.com.jonatas.metronomeplus.data.mapper.toDomainList
import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.repository.SongRepository
import br.com.jonatas.metronomeplus.util.Fixtures
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class GetSongsByFolderUseCaseImplTest {

    @MockK
    private lateinit var mockRepository: SongRepository
    private lateinit var getSongsByFolderUseCase: GetSongsByFolderUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        getSongsByFolderUseCase = GetSongsByFolderUseCaseImpl(mockRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should return all songs when getSongsByFolderIdUseCase is called with the new folder`() =
        runTest {
            val folder =
                Folder(id = "", name = "", musics = 0, date = 123L, isDefault = false)
            every { mockRepository.getAllSongs() } returns flowOf(
                Fixtures.mockAllSongsDto().toDomainList()
            )

            val actualAllSongs = getSongsByFolderUseCase(folder = folder).first()

            assertEquals(
                Fixtures.mockAllSongsDto().toDomainList().toSet(),
                actualAllSongs.toSet()
            )

            verify(exactly = 1) { mockRepository.getAllSongs() }
            verify(exactly = 0) { mockRepository.getSongsByFolderId(any()) }
        }

    @Test
    fun `should return all songs when getSongsByFolderIdUseCase is called with the default folder`() =
        runTest {
            val folder =
                Folder(id = "folder1", name = "Folder 1", musics = 0, date = 123L, isDefault = true)
            every { mockRepository.getAllSongs() } returns flowOf(
                Fixtures.mockAllSongsDto().toDomainList()
            )

            val actualAllSongs = getSongsByFolderUseCase(folder = folder).first()

            assertEquals(
                Fixtures.mockAllSongsDto().toDomainList().toSet(),
                actualAllSongs.toSet()
            )

            verify(exactly = 1) { mockRepository.getAllSongs() }
            verify(exactly = 0) { mockRepository.getSongsByFolderId(any()) }
        }

    @Test
    fun `should return songsByFolderId when getSongsByFolderIdUseCase is called with the folderId`() =
        runTest {
            val folder = Folder(id = "folder2", name = "Folder 2", musics = 0, date = 123L)
            every { mockRepository.getSongsByFolderId(folderId = folder.id) } returns flowOf(
                Fixtures.mockAllSongsDto().toDomainList()
            )

            val actualSongsByFolderId = getSongsByFolderUseCase(folder = folder).first()

            assertEquals(
                Fixtures.mockAllSongsDto().toDomainList().toSet(),
                actualSongsByFolderId.toSet()
            )

            verify(exactly = 1) { mockRepository.getSongsByFolderId(folder.id) }
            verify(exactly = 0) { mockRepository.getAllSongs() }
        }

    @Test
    fun `should throw an exception when getSongsByFolderId is called with default folder`() =
        runTest {
            val folder =
                Folder(id = "folder2", name = "Folder 2", musics = 0, date = 123L, isDefault = true)

            val expectedMessageError = "Db Error"
            every { mockRepository.getAllSongs() } throws RuntimeException(
                expectedMessageError
            )

            try {
                getSongsByFolderUseCase(folder = folder)
                fail("was supposed to throw an exception but failed")
            } catch (e: Throwable) {
                assertTrue(e is RuntimeException)
                assertEquals(expectedMessageError, e.message)
            }

            verify(exactly = 1) { mockRepository.getAllSongs() }
            verify(exactly = 0) { mockRepository.getSongsByFolderId(folderId = any()) }
        }


    @Test
    fun `should throw an exception when getSongsByFolderId is called with empty folder`() =
        runTest {
            val folder = Folder(id = "", name = "", musics = 0, date = 123L)

            val expectedMessageError = "Db Error"
            every { mockRepository.getAllSongs() } throws RuntimeException(
                expectedMessageError
            )

            try {
                getSongsByFolderUseCase(folder = folder)
                fail("was supposed to throw an exception but failed")
            } catch (e: Throwable) {
                assertTrue(e is RuntimeException)
                assertEquals(expectedMessageError, e.message)
            }

            verify(exactly = 1) { mockRepository.getAllSongs() }
            verify(exactly = 0) { mockRepository.getSongsByFolderId(folderId = any()) }
        }

    @Test
    fun `should throw an exception when getSongsByFolderId is called with folderId`() = runTest {
        val folder = Folder(id = "folder2", name = "Folder 2", musics = 0, date = 123L)

        val expectedMessageError = "Db Error"
        every { mockRepository.getSongsByFolderId(folderId = folder.id) } throws RuntimeException(
            expectedMessageError
        )

        try {
            getSongsByFolderUseCase(folder = folder)
            fail("was supposed to throw an exception but failed")
        } catch (e: Throwable) {
            assertTrue(e is RuntimeException)
            assertEquals(expectedMessageError, e.message)
        }

        verify(exactly = 1) { mockRepository.getSongsByFolderId(folderId = folder.id) }
        verify(exactly = 0) { mockRepository.getAllSongs() }
    }
}