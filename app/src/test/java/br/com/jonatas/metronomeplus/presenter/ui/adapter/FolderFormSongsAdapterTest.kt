package br.com.jonatas.metronomeplus.presenter.ui.adapter

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.test.core.app.ApplicationProvider
import br.com.jonatas.metronomeplus.data.mapper.toDomainList
import br.com.jonatas.metronomeplus.databinding.ViewFolderFormSongItemBinding
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModelList
import br.com.jonatas.metronomeplus.presenter.model.song.SongCallbacks
import br.com.jonatas.metronomeplus.presenter.model.song.SongUiModel
import br.com.jonatas.metronomeplus.util.Fixtures
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class FolderFormSongsAdapterTest {

    private lateinit var context: Context
    private lateinit var songsAdapter: FolderFormSongsAdapter
    private lateinit var testSongs: List<SongUiModel>
    private lateinit var parent: ConstraintLayout
    private lateinit var viewHolder: FolderFormSongsAdapter.ViewHolder

    @MockK
    private lateinit var binding: ViewFolderFormSongItemBinding

    @MockK
    private lateinit var callbacks: SongCallbacks

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        context = ApplicationProvider.getApplicationContext()
        testSongs = Fixtures.mockAllSongsDto().toDomainList().toUiModelList()
        parent = ConstraintLayout(context)
        songsAdapter = FolderFormSongsAdapter()

        viewHolder = songsAdapter.onCreateViewHolder(parent, 0)
        binding = ViewFolderFormSongItemBinding.bind(viewHolder.itemView)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should returns zero size when receiving`() {
        assertTrue(songsAdapter.currentList.isEmpty())

        songsAdapter.submitList(emptyList())
        ShadowLooper.idleMainLooper()

        assertEquals(0, songsAdapter.itemCount)
    }

    @Test
    fun `should return the correct size when receiving many songs`() {
        assertTrue(songsAdapter.currentList.isEmpty())

        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()

        assertEquals(testSongs.size, songsAdapter.itemCount)
    }

    @Test
    fun `should bind the data correctly to views when onBindViewHolder is called`() {
        val position = 0
        val songUi = testSongs[position]

        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, position)

        val numeratorDigits = songUi.timeSignature.numerator.toString().padStart(2, ' ')
        val timeSignatureView = binding.songItemViewSignature

        assertTrue(binding.songItemOptions.isVisible)
        assertTrue(binding.songItemSelected.isInvisible)

        assertEquals(songUi.title, binding.songItemTitle.text.toString())
        assertEquals(songUi.artist, binding.songItemArtist.text.toString())
        assertEquals(songUi.bpm.toString(), binding.songItemBpm.text.toString())
        assertEquals(songUi.beatPatterns.size, binding.songItemBeatListview.childCount)

        assertEquals(
            numeratorDigits[0].toString(),
            timeSignatureView.songSigNumeratorOne.text.toString()
        )
        assertEquals(
            numeratorDigits[1].toString(),
            timeSignatureView.songSigNumeratorTwo.text.toString()
        )
        assertEquals(
            songUi.timeSignature.denominator.toString(),
            timeSignatureView.songSigDenominator.text.toString()
        )
    }

    @Test
    fun `should set root view as enabled when edit mode is disabled`() {
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        assertTrue(binding.root.isEnabled)

        assertFalse(binding.songItemSelected.isEnabled)
    }

    @Test
    fun `should set all views as enabled when edit mode is enabled`() {
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.editableState = songsAdapter.editableState.copy(isEditingEnabled = true)
        songsAdapter.onBindViewHolder(viewHolder, 0)

        assertTrue(binding.root.isEnabled)
        assertTrue(binding.songItemOptions.isEnabled)
        assertTrue(binding.songItemSelected.isEnabled)
    }

    @Test
    fun `should return songId when clicking on the root view`() {
        val position = 0
        val songUi = testSongs[position]

        val slot = slot<String>()
        every { callbacks.onItemClicked(capture(slot)) } just Runs

        songsAdapter.setCallbacks(callbacks = callbacks)
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, position)

        viewHolder.itemView.performClick()

        val capturedId = slot.captured
        assertEquals(songUi.id, capturedId)
        verify(exactly = 1) { callbacks.onItemClicked(songUi.id) }
    }

    @Test
    fun `should not return songId when clicking on the root view before bind`() {
        songsAdapter.setCallbacks(callbacks = callbacks)
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()

        viewHolder.itemView.performClick()

        verify(exactly = 0) { callbacks.onItemClicked(any()) }
    }

    @Test
    fun `should return correct songId when the view holder is recycled and rebound`() {
        val position = 0
        val songUi1 = testSongs[position]

        val slot = slot<String>()
        every { callbacks.onItemClicked(capture(slot)) } just Runs

        songsAdapter.setCallbacks(callbacks = callbacks)
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()

        songsAdapter.onBindViewHolder(viewHolder, position)
        viewHolder.itemView.performClick()

        assertEquals(songUi1.id, slot.captured)

        val position2 = 1
        val songUi2 = testSongs[position2]

        songsAdapter.onBindViewHolder(viewHolder, position2)
        viewHolder.itemView.performClick()

        assertEquals(songUi2.id, slot.captured)

        verify(exactly = 2) { callbacks.onItemClicked(any()) }
    }

    @Test
    fun `should not crash when clicked and callbacks are null`() {
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        try {
            viewHolder.itemView.performClick()
        } catch (e: Exception) {
            fail("Clicking root view crashes the application with null callbacks: ${e.message}")
        }
    }

    @Test
    fun `should return the songId and anchorView when clicking on the songItemOptions view`() {
        val position = 0
        val songUi = testSongs[position]

        val slot = slot<String>()
        val slotView = slot<View>()
        every { callbacks.onItemMenuClicked(capture(slot), capture(slotView)) } just Runs

        songsAdapter.setCallbacks(callbacks)

        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, position)

        binding.songItemOptions.performClick()

        val capturedId = slot.captured
        val capturedView = slotView.captured
        assertEquals(songUi.id, capturedId)
        assertEquals(binding.songItemOptions, capturedView)

        verify(exactly = 1) { callbacks.onItemMenuClicked(songUi.id, binding.songItemOptions) }
    }

    @Test
    fun `should not return songId and anchorView when clicking on the songItemOptions view before bind`() {
        songsAdapter.setCallbacks(callbacks = callbacks)
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()

        binding.songItemOptions.performClick()

        verify(exactly = 0) { callbacks.onItemClicked(any()) }
    }

    @Test
    fun `should return correct songId and anchorView when the view holder is recycled and rebound`() {
        val position = 0
        val songUi1 = testSongs[position]

        val slot = slot<String>()
        val slotView = slot<View>()
        every { callbacks.onItemMenuClicked(capture(slot), capture(slotView)) } just Runs

        songsAdapter.setCallbacks(callbacks = callbacks)
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()

        songsAdapter.onBindViewHolder(viewHolder, position)
        val viewOptions = binding.songItemOptions
        viewOptions.performClick()

        assertEquals(songUi1.id, slot.captured)
        assertEquals(viewOptions, slotView.captured)

        val position2 = 1
        val songUi2 = testSongs[position2]

        songsAdapter.onBindViewHolder(viewHolder, position2)
        val viewOptions2 = binding.songItemOptions
        viewOptions2.performClick()

        assertEquals(songUi2.id, slot.captured)
        assertEquals(viewOptions2, slotView.captured)

        verify(exactly = 2) { callbacks.onItemMenuClicked(any(), any()) }
    }

    @Test
    fun `should not crash when clicked on songItemOptions and callbacks are null`() {
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        try {
            binding.songItemOptions.performClick()
        } catch (e: Exception) {
            fail("Clicking songItemOptions crashes the application with null callbacks: ${e.message}")
        }
    }
}