package br.com.jonatas.metronomeplus.data.source

import br.com.jonatas.metronomeplus.data.local.DataStoreManager
import br.com.jonatas.metronomeplus.data.model.BeatDto
import br.com.jonatas.metronomeplus.data.model.BeatStateDto
import br.com.jonatas.metronomeplus.data.model.FolderSongCrossRefDto
import br.com.jonatas.metronomeplus.data.model.SongDto
import br.com.jonatas.metronomeplus.data.model.TimeSignatureDto
import br.com.jonatas.metronomeplus.domain.source.SongDataSource
import br.com.jonatas.metronomeplus.util.Fixtures
import br.com.jonatas.metronomeplus.util.Fixtures.CROSS_REF_KEY
import br.com.jonatas.metronomeplus.util.Fixtures.SONGS_KEY
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
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
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class SongDataSourceImplTest {

    @RelaxedMockK
    private lateinit var mockDataStoreManager: DataStoreManager
    private lateinit var songDataSource: SongDataSource

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        songDataSource = SongDataSourceImpl(mockDataStoreManager)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should return all songs when getAllSongs is called`() = runTest {
        val jsonString = Json.encodeToString(Fixtures.mockAllSongsDto())
        coEvery { mockDataStoreManager.getData(SONGS_KEY) } returns flowOf(jsonString)

        val allSongs = songDataSource.getAllSongs().first()

        assertEquals(allSongs.size, allSongs.size)
        verify(exactly = 1) { mockDataStoreManager.getData(SONGS_KEY) }
    }

    @Test
    fun `should return songs by folderId when getSongsByFolderId is called`() = runTest {
        val folderId = "folder2"
        val crossRef = listOf(
            FolderSongCrossRefDto(folderId = "folder2", songId = "song1"),
            FolderSongCrossRefDto(folderId = "folder2", songId = "song5"),
        )
        val crossRefJsonString = Json.encodeToString(crossRef)
        coEvery { mockDataStoreManager.getData(CROSS_REF_KEY) } returns flowOf(crossRefJsonString)

        val jsonString = Json.encodeToString(Fixtures.mockAllSongsDto())
        coEvery { mockDataStoreManager.getData(SONGS_KEY) } returns flowOf(jsonString)

        val songsByFolderId = songDataSource.getSongsByFolderId(folderId = folderId).first()

        assertEquals(2, songsByFolderId.size)

        verify(exactly = 1) { mockDataStoreManager.getData(SONGS_KEY) }
        verify(exactly = 1) { mockDataStoreManager.getData(CROSS_REF_KEY) }
        coVerify(exactly = 0) { mockDataStoreManager.setData(any(), any()) }
    }

    @Test
    fun `should return mock songs and save them to the DataStore when getAllSongs is called without any saved data`() =
        runTest {
            every { mockDataStoreManager.getData(SONGS_KEY) } returns flowOf("")

            coEvery { mockDataStoreManager.setData(CROSS_REF_KEY, any()) } just Runs
            coEvery { mockDataStoreManager.setData(SONGS_KEY, any()) } just Runs

            val allSongs = songDataSource.getAllSongs().first()

            assertEquals(5, allSongs.size)

            verify(exactly = 1) { mockDataStoreManager.getData(SONGS_KEY) }
            coVerify(exactly = 1) { mockDataStoreManager.setData(SONGS_KEY, any()) }
            coVerify(exactly = 1) { mockDataStoreManager.setData(CROSS_REF_KEY, any()) }
        }

    @Test
    fun `should return mock songs and save them to the DataStore when getsSongByFolderId is called without any saved data`() =
        runTest {
            val folderId = "folder2"
            every { mockDataStoreManager.getData(SONGS_KEY) } returns flowOf("")
            coEvery { mockDataStoreManager.getData(CROSS_REF_KEY) } returns flowOf("")

            coEvery { mockDataStoreManager.setData(CROSS_REF_KEY, any()) } just Runs
            coEvery { mockDataStoreManager.setData(SONGS_KEY, any()) } just Runs

            val songByFolderId = songDataSource.getSongsByFolderId(folderId = folderId).first()

            assertEquals(3, songByFolderId.size)

            verify(exactly = 1) { mockDataStoreManager.getData(SONGS_KEY) }
            verify(exactly = 1) { mockDataStoreManager.getData(CROSS_REF_KEY) }
            coVerify(exactly = 1) { mockDataStoreManager.setData(SONGS_KEY, any()) }
            coVerify(exactly = 1) { mockDataStoreManager.setData(CROSS_REF_KEY, any()) }
        }

    @Test
    fun `should return a list of all songs when getAllSongs is called with saved data and multiple emissions`() =
        runTest {
            val songsDto = Fixtures.mockAllSongsDto()
            val jsonString = Json.encodeToString(songsDto)

            val songsDto2 = songsDto + SongDto(
                id = "song3",
                title = "Atos 2",
                artist = "Ministerio Zoe",
                bpm = 320,
                timeSignature = TimeSignatureDto(4, 4),
                beatPatterns = listOf(
                    BeatDto(BeatStateDto.Accent),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                )
            )
            val jsonString2 = Json.encodeToString(songsDto2)

            every { mockDataStoreManager.getData(SONGS_KEY) } returns flow {
                emit(jsonString)
                delay(100)
                emit(jsonString2)
            }

            val resultFlow = songDataSource.getAllSongs()
            val actualNestedList = resultFlow.toList()

            val expectedNestedList = listOf(
                songsDto,
                songsDto2
            )

            assertEquals(expectedNestedList, actualNestedList)
            verify(exactly = 1) { mockDataStoreManager.getData(SONGS_KEY) }
            coVerify(exactly = 0) { mockDataStoreManager.setData(any(), any()) }
        }

    @Test
    fun `should return a list of songs by folderId when getSongsByFolderId is called with saved data and multiple emissions`() =
        runTest {
            val folderId = "folder2"
            val songsDto = Fixtures.mockAllSongsDto()
            val jsonString = Json.encodeToString(songsDto)

            val songsDto2 = songsDto + SongDto(
                id = "song6",
                title = "Atos 2 / Vem me buscar",
                artist = "Ministerio Zoe",
                bpm = 320,
                timeSignature = TimeSignatureDto(4, 4),
                beatPatterns = listOf(
                    BeatDto(BeatStateDto.Accent),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                )
            )
            val jsonString2 = Json.encodeToString(songsDto2)

            every { mockDataStoreManager.getData(SONGS_KEY) } returns flow {
                emit(jsonString)
                delay(100)
                emit(jsonString2)
            }

            val crossRefList = listOf(
                FolderSongCrossRefDto(folderId = "folder2", songId = "song1"),
                FolderSongCrossRefDto(folderId = "folder2", songId = "song2"),
                FolderSongCrossRefDto(folderId = "folder2", songId = "song3"),
                FolderSongCrossRefDto(folderId = "folder2", songId = "song6"),
            )
            val crossRefJsonString = Json.encodeToString(crossRefList)
            coEvery { mockDataStoreManager.getData(CROSS_REF_KEY) } returns flowOf(
                crossRefJsonString
            )

            val resultFlow = songDataSource.getSongsByFolderId(folderId = folderId)
            val actualNestedList = resultFlow.toList()

            val song1 = songsDto.find { it.id == crossRefList[0].songId }!!
            val song2 = songsDto.find { it.id == crossRefList[1].songId }!!
            val song3 = songsDto.find { it.id == crossRefList[2].songId }!!
            val song6 = songsDto2.find { it.id == crossRefList[3].songId }!!

            val expectedFirstEmission = listOf(song1, song2, song3)
            val expectedSecondEmission = listOf(song1, song2, song3, song6)

            assertEquals(2, actualNestedList.size)
            assertEquals(expectedFirstEmission.toSet(), actualNestedList[0].toSet())
            assertEquals(expectedSecondEmission.toSet(), actualNestedList[1].toSet())

            verify(exactly = 1) { mockDataStoreManager.getData(SONGS_KEY) }
            verify(exactly = 1) { mockDataStoreManager.getData(CROSS_REF_KEY) }
            coVerify(exactly = 0) { mockDataStoreManager.setData(any(), any()) }
        }

    @Test
    fun `should throw exception when getAllSong is called but DataStore fails`() = runTest {
        val expectedMessageError = "DB error"
        coEvery { mockDataStoreManager.getData(SONGS_KEY) } throws RuntimeException(
            expectedMessageError
        )

        try {
            songDataSource.getAllSongs()
            fail("was supposed to throw an exception but failed")
        } catch (e: Throwable) {
            assertTrue(e is RuntimeException)
            assertEquals(expectedMessageError, e.message)
        }

        verify(exactly = 1) { mockDataStoreManager.getData(SONGS_KEY) }
        verify(exactly = 0) { mockDataStoreManager.getData(CROSS_REF_KEY) }
        coVerify(exactly = 0) { mockDataStoreManager.setData(any(), any()) }
    }

    @Test
    fun `should throw exception when getSongsByFolderId is called but DataStore fails to return Songs`() =
        runTest {
            val folderId = "folder2"
            every { mockDataStoreManager.getData(CROSS_REF_KEY) } returns flowOf("")

            val expectedMessageError = "DB error"
            every { mockDataStoreManager.getData(SONGS_KEY) } throws RuntimeException(
                expectedMessageError
            )

            try {
                songDataSource.getSongsByFolderId(folderId = folderId)
                fail("was supposed to throw an exception but failed")
            } catch (e: Throwable) {
                assertTrue(e is RuntimeException)
                assertEquals(expectedMessageError, e.message)
            }

            verify(exactly = 1) { mockDataStoreManager.getData(CROSS_REF_KEY) }
            verify(exactly = 1) { mockDataStoreManager.getData(SONGS_KEY) }
            coVerify(exactly = 0) { mockDataStoreManager.setData(any(), any()) }
        }

    @Test
    fun `should throw exception when getSongsByFolderId is called but DataStore fails to return the CrossRef`() =
        runTest {
            val folderId = "folder2"
            val expectedMessageError = "DB error"
            every { mockDataStoreManager.getData(CROSS_REF_KEY) } throws RuntimeException(
                expectedMessageError
            )

            every { mockDataStoreManager.getData(SONGS_KEY) } returns flowOf("")

            try {
                songDataSource.getSongsByFolderId(folderId = folderId)
                fail("was supposed to throw an exception but failed")
            } catch (e: Throwable) {
                assertTrue(e is RuntimeException)
                assertEquals(expectedMessageError, e.message)
            }

            verify(exactly = 1) { mockDataStoreManager.getData(CROSS_REF_KEY) }
            verify(exactly = 0) { mockDataStoreManager.getData(SONGS_KEY) }
            coVerify(exactly = 0) { mockDataStoreManager.setData(any(), any()) }
        }
}