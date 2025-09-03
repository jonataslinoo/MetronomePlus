package br.com.jonatas.metronomeplus.data.repository

import br.com.jonatas.metronomeplus.MainCoroutineRule
import br.com.jonatas.metronomeplus.data.mapper.toDomainList
import br.com.jonatas.metronomeplus.domain.repository.SongRepository
import br.com.jonatas.metronomeplus.domain.source.SongDataSource
import br.com.jonatas.metronomeplus.util.Fixtures
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongRepositoryImplTest {

    @MockK
    private lateinit var mockSongDataSource: SongDataSource
    private lateinit var songRepository: SongRepository

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        songRepository = SongRepositoryImpl(mockSongDataSource, coroutineRule.testDispatcher)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should return all songs when getAllSongs is called`() = runTest {
        every { mockSongDataSource.getAllSongs() } returns flowOf(Fixtures.mockAllSongsDto())

        val actualAllSongs = songRepository.getAllSongs().first()

        assertEquals(Fixtures.mockAllSongsDto().toDomainList().toSet(), actualAllSongs.toSet())

        verify(exactly = 1) { mockSongDataSource.getAllSongs() }
        verify(exactly = 0) { mockSongDataSource.getSongsByFolderId(any()) }
    }

    @Test
    fun `should return songsByFolderId when getSongsByFolderId is called`() = runTest {
        val folderId = "folder2"
        every { mockSongDataSource.getSongsByFolderId(folderId = folderId) } returns flowOf(Fixtures.mockAllSongsDto())

        val actualAllSongs = songRepository.getSongsByFolderId(folderId = folderId).first()

        assertEquals(Fixtures.mockAllSongsDto().toDomainList().toSet(), actualAllSongs.toSet())

        verify(exactly = 1) { mockSongDataSource.getSongsByFolderId(folderId = folderId) }
        verify(exactly = 0) { mockSongDataSource.getAllSongs() }
    }

    @Test
    fun `should throw an exception when getAllSongs is called`() = runTest {
        val expectedMessageError = "Db Error"
        every { mockSongDataSource.getAllSongs() } throws RuntimeException(
            expectedMessageError
        )

        try {
            songRepository.getAllSongs()
            fail("was supposed to throw an exception but failed")
        } catch (e: Throwable) {
            assertTrue(e is RuntimeException)
            assertEquals(expectedMessageError, e.message)
        }

        verify(exactly = 1) { mockSongDataSource.getAllSongs() }
        verify(exactly = 0) { mockSongDataSource.getSongsByFolderId(any()) }
    }

    @Test
    fun `should throw an exception when getSongsByFolderId is called`() = runTest {
        val folderId = ""
        val expectedMessageError = "Db Error"
        every { mockSongDataSource.getSongsByFolderId(folderId = folderId) } throws RuntimeException(
            expectedMessageError
        )

        try {
            songRepository.getSongsByFolderId(folderId = folderId)
            fail("was supposed to throw an exception but failed")
        } catch (e: Throwable) {
            assertTrue(e is RuntimeException)
            assertEquals(expectedMessageError, e.message)
        }

        verify(exactly = 1) { mockSongDataSource.getSongsByFolderId(folderId = folderId) }
        verify(exactly = 0) { mockSongDataSource.getAllSongs() }
    }
}