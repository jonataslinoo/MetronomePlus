package br.com.jonatas.metronomeplus.presenter.ui.adapter

import android.content.Context
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.test.core.app.ApplicationProvider
import br.com.jonatas.metronomeplus.data.mapper.toDomainList
import br.com.jonatas.metronomeplus.databinding.ViewFolderFormSongItemBinding
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModelList
import br.com.jonatas.metronomeplus.presenter.model.song.SongCallbacks
import br.com.jonatas.metronomeplus.presenter.model.song.SongUiModel
import br.com.jonatas.metronomeplus.presenter.ui.adapter.utils.EditableAdapterState
import br.com.jonatas.metronomeplus.util.Fixtures
import br.com.jonatas.metronomeplus.util.ThemesContextTest
import com.google.android.material.card.MaterialCardView
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
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class FolderFormSongsAdapterTest {

    private lateinit var songsAdapter: FolderFormSongsAdapter
    private lateinit var testSongs: List<SongUiModel>
    private lateinit var parent: MaterialCardView
    private lateinit var viewHolder: FolderFormSongsAdapter.ViewHolder

    @MockK
    private lateinit var binding: ViewFolderFormSongItemBinding

    @MockK
    private lateinit var callbacks: SongCallbacks

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        val context: Context = ApplicationProvider.getApplicationContext()
        val themedContext = ThemesContextTest.createThemedContext(context)
        parent = MaterialCardView(themedContext)
        songsAdapter = FolderFormSongsAdapter()
        testSongs = Fixtures.mockAllSongsDto().toDomainList().toUiModelList()

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
    fun `should set the correct visibility state when list edit mode is disabled`() {
        songsAdapter.editableState = EditableAdapterState(isListEditingMode = false)

        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        assertTrue(binding.songItemOptions.isVisible)
        assertTrue(binding.songItemDragDrop.isGone)
        assertTrue(binding.songItemSelected.isGone)
    }

    @Test
    fun `should set the correct visibility state when list edit mode is enabled`() {
        songsAdapter.editableState = EditableAdapterState(isListEditingMode = true)

        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        assertTrue(binding.songItemOptions.isInvisible)
        assertTrue(binding.songItemDragDrop.isVisible)
        assertTrue(binding.songItemSelected.isVisible)
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

    @Test
    fun `should not execute long click callback on the root view when edit mode is disabled`() {
        songsAdapter.editableState = EditableAdapterState(isEditingEnabled = false)

        every { callbacks.onListEditMode(any()) } just Runs

        songsAdapter.setCallbacks(callbacks)

        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        viewHolder.itemView.performLongClick()

        verify(exactly = 0) { callbacks.onListEditMode(any()) }
    }

    @Test
    fun `should execute the long click callback in the root view when edit mode is enabled`() {
        songsAdapter.editableState = EditableAdapterState(isEditingEnabled = true)

        every { callbacks.onListEditMode(any()) } just Runs

        songsAdapter.setCallbacks(callbacks)

        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        viewHolder.itemView.performLongClick()

        verify(exactly = 1) { callbacks.onListEditMode(any()) }
    }

    @Test
    fun `should not execute item clicked callback when in list edit mode`() {
        songsAdapter.editableState =
            EditableAdapterState(isEditingEnabled = true, isListEditingMode = true)

        every { callbacks.onItemClicked(any()) } just Runs

        songsAdapter.setCallbacks(callbacks)

        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        viewHolder.itemView.performClick()

        verify(exactly = 0) { callbacks.onItemClicked(any()) }
    }

    @Test
    fun `should not execute item menu clicked callback when in list edit mode`() {
        songsAdapter.editableState =
            EditableAdapterState(isEditingEnabled = true, isListEditingMode = true)

        every { callbacks.onItemMenuClicked(any(), any()) } just Runs

        songsAdapter.setCallbacks(callbacks)

        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        viewHolder.itemView.performClick()

        verify(exactly = 0) { callbacks.onItemMenuClicked(any(), any()) }
    }

    @Test
    fun `should enable list edit mode when it is disabled and a long click is performed`() {
        songsAdapter.editableState =
            EditableAdapterState(isEditingEnabled = true, isListEditingMode = false)

        val slot = slot<Boolean>()
        every { callbacks.onListEditMode(capture(slot)) } just Runs

        songsAdapter.setCallbacks(callbacks)

        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        viewHolder.itemView.performLongClick()

        assertEquals(true, slot.captured)

        verify(exactly = 1) { callbacks.onListEditMode(any()) }
    }

    @Test
    fun `should disable list edit mode when it is enabled and a long click is performed`() {
        songsAdapter.editableState =
            EditableAdapterState(isEditingEnabled = true, isListEditingMode = true)

        val slot = slot<Boolean>()
        every { callbacks.onListEditMode(capture(slot)) } just Runs

        songsAdapter.setCallbacks(callbacks)

        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        viewHolder.itemView.performLongClick()

        assertEquals(false, slot.captured)

        verify(exactly = 1) { callbacks.onListEditMode(any()) }
    }

    @Test
    fun `should not crash when long clicked on root view and callbacks are null`() {
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        try {
            binding.root.performLongClick()
        } catch (e: Exception) {
            fail("Long clicking root view crashes the application with null callbacks: ${e.message}")
        }
    }
}