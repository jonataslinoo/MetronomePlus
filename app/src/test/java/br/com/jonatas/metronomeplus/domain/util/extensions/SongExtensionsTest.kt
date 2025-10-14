package br.com.jonatas.metronomeplus.domain.util.extensions

import br.com.jonatas.metronomeplus.data.mapper.toDomainList
import br.com.jonatas.metronomeplus.domain.model.Song
import br.com.jonatas.metronomeplus.domain.model.TimeSignature
import br.com.jonatas.metronomeplus.util.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SongExtensionsTest {

    private lateinit var testSongs: List<Song>

    @Before
    fun setup() {
        testSongs = Fixtures.mockAllSongsDto().toDomainList()
    }

    @Test
    fun `should return all songs when query is blank`() {
        val query = ""

        val filteredSongs = testSongs.filterSongs(query)

        assertEquals(testSongs.size, filteredSongs.size)
        assertTrue(filteredSongs.containsAll(testSongs))
    }

    @Test
    fun `should filter songs by title initials`() {
        val query = "Atos 2"

        val filteredSongs = testSongs.filterSongs(query)

        assertEquals(1, filteredSongs.size)
        assertTrue(filteredSongs[0].title.startsWith(query))
    }

    @Test
    fun `should filter songs by artist initials`() {
        val query = "Ronaldo"

        val filteredSongs = testSongs.filterSongs(query)

        assertEquals(1, filteredSongs.size)
        assertTrue(filteredSongs[0].artist.startsWith(query))
    }

    @Test
    fun `should filter songs by containing title`() {
        val query = "Senhor"

        val filteredSongs = testSongs.filterSongs(query)

        assertEquals(1, filteredSongs.size)
        assertTrue(filteredSongs[0].title.contains(query))
    }

    @Test
    fun `should filter songs by containing artist`() {
        val query = "Campos"

        val filteredSongs = testSongs.filterSongs(query)

        assertEquals(1, filteredSongs.size)
        assertTrue(filteredSongs[0].artist.contains(query))
    }

    @Test
    fun `should sort the filtered songs in alphabetical order`() {
        val newTestsSongs = testSongs + Song(
            id = "song6",
            title = "Atos 3",
            artist = "Ministerio Zoe",
            bpm = 220,
            timeSignature = TimeSignature(8, 4),
            beatPatterns = emptyList(),
        )

        val query = "Atos"

        val filteredSongs = newTestsSongs.filterSongs(query)

        assertEquals(2, filteredSongs.size)
        assertEquals("Atos 2", filteredSongs[0].title)
        assertEquals("Atos 3", filteredSongs[1].title)
    }

    @Test
    fun `should filter songs sorted by title and artist in that order`() {
        val newTestsSongs = testSongs + Song(
            id = "song6",
            title = "Ronaldo Bezerra",
            artist = "Marcado",
            bpm = 220,
            timeSignature = TimeSignature(8, 4),
            beatPatterns = emptyList(),
        )

        val query = "Marcado"

        val filteredSongs = newTestsSongs.filterSongs(query)

        assertEquals(2, filteredSongs.size)
        assertEquals("Marcado", filteredSongs[0].title)
        assertEquals("Ronaldo Bezerra", filteredSongs[1].title)
    }

    @Test
    fun `should filter ignoring case`() {
        val query = "aTOS 2"

        val filteredSongs = testSongs.filterSongs(query)

        assertEquals(1, filteredSongs.size)
        assertEquals("Atos 2", filteredSongs[0].title)
    }

    @Test
    fun `should return empty list when no match is found`() {
        val query = "Inexistente"

        val filteredSongs = testSongs.filterSongs(query)

        assertTrue(filteredSongs.isEmpty())
    }

    @Test
    fun `should prioritize startsWith result over contains`() {
        val newTestsSongs = testSongs + Song(
            id = "song6",
            title = "Diante da Cruz",
            artist = "Aline Barros",
            bpm = 220,
            timeSignature = TimeSignature(8, 4),
            beatPatterns = emptyList(),
        )

        val query = "Cruz"

        val filteredSongs = newTestsSongs.filterSongs(query)

        assertEquals(2, filteredSongs.size)
        assertEquals("Cruz", filteredSongs[0].title)
        assertEquals("Diante da Cruz", filteredSongs[1].title)
    }

    @Test
    fun `should select songs when receiving a list with songIds`() {
        val position = 0
        val position2 = 3
        val selectedIds = setOf(testSongs[position].id, testSongs[position2].id)

        val selectedSongs = testSongs.selectSongs(selectedIds)

        assertTrue(selectedSongs[position].selected)
        assertTrue(selectedSongs[position2].selected)
        assertEquals(2, selectedSongs.filter { song -> song.selected }.size)
    }

    @Test
    fun `should deselect songs when receiving a list with preselected songIds`() {
        val position = 0
        val position2 = 3
        val selectedIds = setOf(testSongs[position].id, testSongs[position2].id)
        val selectedSongs = testSongs.selectSongs(selectedIds)

        val deselectedSongs = selectedSongs.selectSongs(selectedIds)

        assertFalse(deselectedSongs[position].selected)
        assertFalse(deselectedSongs[position2].selected)
        assertEquals(0, deselectedSongs.filter { song -> song.selected }.size)
    }

    @Test
    fun `should do nothing when receiving a list with invalid songIds`() {
        val selectedIds = setOf("11", "22")

        val resultSongs = testSongs.selectSongs(selectedIds)

        assertEquals(testSongs, resultSongs)
    }

    @Test
    fun `should do nothing when receiving an empty selectedIds`() {
        val selectedIds = emptySet<String>()

        val result = testSongs.selectSongs(selectedIds)

        assertEquals(testSongs, result)
    }
}