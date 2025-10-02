package br.com.jonatas.metronomeplus.presenter.ui.adapter.util

import androidx.recyclerview.widget.ItemTouchHelper
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ItemTouchHelperManagerTest {

    @RelaxedMockK
    private lateinit var mockHelperAdapter: ItemTouchHelperAdapter

    @RelaxedMockK
    private lateinit var mockHelperCallback: ItemTouchHelperCallback

    private lateinit var helperManager: ItemTouchHelperManager

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should create an ItemTouchHelper and attach on adapter`() {
        val helperSlot = slot<ItemTouchHelper>()
        every { mockHelperAdapter.onAttachHelper(capture(helperSlot)) } just Runs

        helperManager = ItemTouchHelperManager(mockHelperAdapter, mockHelperCallback)

        assertEquals(helperManager.itemTouchHelper, helperSlot.captured)

        verify { mockHelperAdapter.onAttachHelper(any()) }
    }
}
