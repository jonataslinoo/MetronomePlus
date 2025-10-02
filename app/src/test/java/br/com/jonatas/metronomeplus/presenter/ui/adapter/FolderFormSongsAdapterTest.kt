package br.com.jonatas.metronomeplus.presenter.ui.adapter

import android.content.Context
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.test.core.app.ApplicationProvider
import br.com.jonatas.metronomeplus.data.mapper.toDomainList
import br.com.jonatas.metronomeplus.databinding.ViewFolderFormSongItemBinding
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModelList
import br.com.jonatas.metronomeplus.presenter.model.song.SongCallbacks
import br.com.jonatas.metronomeplus.presenter.model.song.SongUiModel
import br.com.jonatas.metronomeplus.presenter.ui.adapter.utils.EditableState
import br.com.jonatas.metronomeplus.util.Fixtures
import br.com.jonatas.metronomeplus.util.MyCustomListenerHelpers.performLongClick
import br.com.jonatas.metronomeplus.util.MyCustomListenerHelpers.performShortClick
import br.com.jonatas.metronomeplus.util.ThemesContextTest
import com.google.android.material.card.MaterialCardView
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
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
import java.util.concurrent.TimeUnit

private const val DRAG_LONG_CLICK_DELAY = 200L
private const val DEFAULT_LONG_CLICK_DURATION = 600L

@RunWith(RobolectricTestRunner::class)
class FolderFormSongsAdapterTest {

    private lateinit var context: Context
    private lateinit var songsAdapter: FolderFormSongsAdapter
    private lateinit var testSongs: List<SongUiModel>
    private lateinit var parent: MaterialCardView
    private lateinit var viewHolder: FolderFormSongsAdapter.ViewHolder

    @MockK
    private lateinit var mockBinding: ViewFolderFormSongItemBinding

    @MockK
    private lateinit var mockCallbacks: SongCallbacks

    @RelaxedMockK
    private lateinit var mockItemTouchHelper: ItemTouchHelper

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        context = ApplicationProvider.getApplicationContext()
        val themedContext = ThemesContextTest.createThemedContext(context)
        parent = MaterialCardView(themedContext)
        songsAdapter = FolderFormSongsAdapter()
        testSongs = Fixtures.mockAllSongsDto().toDomainList().toUiModelList()

        viewHolder = songsAdapter.onCreateViewHolder(parent, 0)
        mockBinding = ViewFolderFormSongItemBinding.bind(viewHolder.itemView)
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
        val timeSignatureView = mockBinding.songItemViewSignature

        assertEquals(songUi.title, mockBinding.songItemTitle.text.toString())
        assertEquals(songUi.artist, mockBinding.songItemArtist.text.toString())
        assertEquals(songUi.bpm.toString(), mockBinding.songItemBpm.text.toString())
        assertEquals(songUi.beatPatterns.size, mockBinding.songItemBeatListview.childCount)

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
    fun `should set the correct visibility state when list edit mode and reordering are both disabled`() {
        songsAdapter.editableState = EditableState(isListEditMode = false, isReorderingMode = false)

        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        assertTrue(mockBinding.songItemOptions.isVisible)
        assertTrue(mockBinding.songItemDragDrop.isInvisible)
        assertTrue(mockBinding.songItemSelected.isGone)
    }

    @Test
    fun `should set the correct visibility state when list edit mode and reordering are both enabled`() {
        songsAdapter.editableState = EditableState(isListEditMode = true, isReorderingMode = true)

        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        assertTrue(mockBinding.songItemOptions.isInvisible)
        assertTrue(mockBinding.songItemDragDrop.isVisible)
        assertTrue(mockBinding.songItemSelected.isVisible)
    }

    @Test
    fun `should set the correct visibility state when list edit mode is enabled but reordering is disabled`() {
        songsAdapter.editableState = EditableState(isListEditMode = true, isReorderingMode = false)

        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        assertTrue(mockBinding.songItemOptions.isInvisible)
        assertTrue(mockBinding.songItemDragDrop.isInvisible)
        assertTrue(mockBinding.songItemSelected.isVisible)
    }

    @Test
    fun `should return songId when clicking on the root view`() {
        val position = 0
        val songUi = testSongs[position]
        val slot = slot<String>()
        every { mockCallbacks.onItemClicked(capture(slot)) } just Runs
        songsAdapter.editableState = EditableState(isEditMode = false)
        songsAdapter.setCallbacks(callbacks = mockCallbacks)
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, position)

        performShortClick(viewHolder.itemView)

        val capturedId = slot.captured
        assertEquals(songUi.id, capturedId)

        verify(exactly = 1) { mockCallbacks.onItemClicked(songUi.id) }
    }

    @Test
    fun `should not return songId when clicking on the root view before bind`() {
        songsAdapter.setCallbacks(callbacks = mockCallbacks)
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()

        performShortClick(viewHolder.itemView)

        verify(exactly = 0) { mockCallbacks.onItemClicked(any()) }
    }

    @Test
    fun `should return correct songId when the view holder is recycled and rebound`() {
        val position = 0
        val position2 = 1
        val songUi1 = testSongs[position]
        val songUi2 = testSongs[position2]
        val slot = slot<String>()
        every { mockCallbacks.onItemClicked(capture(slot)) } just Runs
        songsAdapter.setCallbacks(callbacks = mockCallbacks)
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()

        songsAdapter.onBindViewHolder(viewHolder, position)
        performShortClick(viewHolder.itemView)

        assertEquals(songUi1.id, slot.captured)

        songsAdapter.onBindViewHolder(viewHolder, position2)
        performShortClick(viewHolder.itemView)

        assertEquals(songUi2.id, slot.captured)

        verify(exactly = 2) { mockCallbacks.onItemClicked(any()) }
    }

    @Test
    fun `should not crash when clicked and callbacks are null`() {
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        try {
            performShortClick(viewHolder.itemView)
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
        every { mockCallbacks.onItemMenuClicked(capture(slot), capture(slotView)) } just Runs

        songsAdapter.setCallbacks(mockCallbacks)

        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, position)

        mockBinding.songItemOptions.performClick()

        val capturedId = slot.captured
        val capturedView = slotView.captured
        assertEquals(songUi.id, capturedId)
        assertEquals(mockBinding.songItemOptions, capturedView)

        verify(exactly = 1) {
            mockCallbacks.onItemMenuClicked(
                songUi.id,
                mockBinding.songItemOptions
            )
        }
    }

    @Test
    fun `should not return songId and anchorView when clicking on the songItemOptions view before bind`() {
        songsAdapter.setCallbacks(callbacks = mockCallbacks)
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()

        mockBinding.songItemOptions.performClick()

        verify(exactly = 0) { mockCallbacks.onItemClicked(any()) }
    }

    @Test
    fun `should return correct songId and anchorView when the view holder is recycled and rebound`() {
        val position = 0
        val songUi1 = testSongs[position]

        val slot = slot<String>()
        val slotView = slot<View>()
        every { mockCallbacks.onItemMenuClicked(capture(slot), capture(slotView)) } just Runs

        songsAdapter.setCallbacks(callbacks = mockCallbacks)
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()

        songsAdapter.onBindViewHolder(viewHolder, position)
        val viewOptions = mockBinding.songItemOptions
        viewOptions.performClick()

        assertEquals(songUi1.id, slot.captured)
        assertEquals(viewOptions, slotView.captured)

        val position2 = 1
        val songUi2 = testSongs[position2]

        songsAdapter.onBindViewHolder(viewHolder, position2)
        val viewOptions2 = mockBinding.songItemOptions
        viewOptions2.performClick()

        assertEquals(songUi2.id, slot.captured)
        assertEquals(viewOptions2, slotView.captured)

        verify(exactly = 2) { mockCallbacks.onItemMenuClicked(any(), any()) }
    }

    @Test
    fun `should not crash when clicked on songItemOptions and callbacks are null`() {
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        try {
            mockBinding.songItemOptions.performClick()
        } catch (e: Exception) {
            fail("Clicking songItemOptions crashes the application with null callbacks: ${e.message}")
        }
    }

    @Test
    fun `should not execute long click callback on the root view when edit mode is disabled`() {
        every { mockCallbacks.onItemClicked(any()) } just Runs
        every { mockCallbacks.onListEditMode(any(), any()) } just Runs
        songsAdapter.editableState = EditableState(isEditMode = false)
        songsAdapter.setCallbacks(mockCallbacks)
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        performLongClick(viewHolder.itemView, DEFAULT_LONG_CLICK_DURATION)

        verify(exactly = 1) { mockCallbacks.onItemClicked(any()) }
        verify(exactly = 0) { mockCallbacks.onListEditMode(any(), any()) }
    }

    @Test
    fun `should execute the callback and enable list edit mode when a long click is performed in edit mode`() {
        val position = 0
        val songUi = testSongs[position]
        val slotId = slot<String>()
        val slotState = slot<Boolean>()
        every { mockCallbacks.onListEditMode(capture(slotId), capture(slotState)) } just Runs
        songsAdapter.editableState = EditableState(isEditMode = true)
        songsAdapter.setCallbacks(mockCallbacks)
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, position)

        performLongClick(viewHolder.itemView, DEFAULT_LONG_CLICK_DURATION)

        assertEquals(songUi.id, slotId.captured)
        assertEquals(true, slotState.captured)

        verify(exactly = 1) { mockCallbacks.onListEditMode(songUi.id, true) }
    }

    @Test
    fun `should not enable list edit mode when gesture is cancelled`() {
        every { mockCallbacks.onListEditMode(any(), any()) } just Runs
        songsAdapter.editableState = EditableState(isEditMode = true)
        songsAdapter.setCallbacks(mockCallbacks)
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        performLongClick(viewHolder.itemView, 100L, MotionEvent.ACTION_CANCEL)
        ShadowLooper.idleMainLooper(DEFAULT_LONG_CLICK_DURATION, TimeUnit.MILLISECONDS)

        verify(exactly = 0) { mockCallbacks.onListEditMode(any(), any()) }
    }

    @Test
    fun `should not execute the long click callback in the root view when in list edit mode`() {
        every { mockCallbacks.onListEditMode(any(), any()) } just Runs
        songsAdapter.editableState = EditableState(isEditMode = true, isListEditMode = true)
        songsAdapter.setCallbacks(mockCallbacks)
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        performLongClick(viewHolder.itemView, DEFAULT_LONG_CLICK_DURATION)

        verify(exactly = 0) { mockCallbacks.onListEditMode(any(), any()) }
    }

    @Test
    fun `should not crash when long clicked on root view and callbacks are null`() {
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        try {
            performLongClick(viewHolder.itemView, DEFAULT_LONG_CLICK_DURATION)
        } catch (e: Exception) {
            fail("Long clicking root view crashes the application with null callbacks: ${e.message}")
        }
    }

    @Test
    fun `should not execute item clicked callback when in list edit mode`() {
        every { mockCallbacks.onItemClicked(any()) } just Runs
        songsAdapter.editableState = EditableState(isEditMode = true, isListEditMode = true)
        songsAdapter.setCallbacks(mockCallbacks)
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        performShortClick(viewHolder.itemView)

        verify(exactly = 0) { mockCallbacks.onItemClicked(any()) }
    }

    @Test
    fun `Should trigger drag and drop when middle press and drag is performed with editableState fully enabled`() {
        every { mockItemTouchHelper.startDrag(viewHolder) } just Runs
        songsAdapter.editableState =
            EditableState(isEditMode = true, isListEditMode = true, isReorderingMode = true)
        songsAdapter.onAttachHelper(mockItemTouchHelper)
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        performLongClick(viewHolder.itemView, DRAG_LONG_CLICK_DELAY)

        verify(exactly = 1) { mockItemTouchHelper.startDrag(viewHolder) }
    }

    @Test
    fun `Should not trigger drag and drop when gesture is canceled`() {
        every { mockItemTouchHelper.startDrag(viewHolder) } just Runs
        songsAdapter.editableState =
            EditableState(isEditMode = true, isListEditMode = true, isReorderingMode = true)
        songsAdapter.onAttachHelper(mockItemTouchHelper)
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        performLongClick(viewHolder.itemView, 100L, MotionEvent.ACTION_CANCEL)

        verify(exactly = 0) { mockItemTouchHelper.startDrag(viewHolder) }
    }

    @Test
    fun `Should not trigger drag and drop when reordering is disabled`() {
        every { mockItemTouchHelper.startDrag(viewHolder) } just Runs
        songsAdapter.editableState =
            EditableState(isEditMode = true, isListEditMode = true, isReorderingMode = false)
        songsAdapter.onAttachHelper(mockItemTouchHelper)
        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)

        performLongClick(viewHolder.itemView, DRAG_LONG_CLICK_DELAY)

        verify(exactly = 0) { mockItemTouchHelper.startDrag(viewHolder) }
    }

    @Test
    fun `should trigger the item move callback when dragging is initiated`() {
        val fromPosition = 0
        val toPosition = 2
        val slotFromPosition = slot<Int>()
        val slotToPosition = slot<Int>()
        every {
            mockCallbacks.onItemMove(capture(slotFromPosition), capture(slotToPosition))
        } just Runs
        songsAdapter.setCallbacks(mockCallbacks)

        songsAdapter.onRowMove(fromPosition, toPosition)

        assertEquals(fromPosition, slotFromPosition.captured)
        assertEquals(toPosition, slotToPosition.captured)

        verify(exactly = 1) { mockCallbacks.onItemMove(fromPosition, toPosition) }
    }
}