package br.com.jonatas.metronomeplus.presenter.ui.adapter.util

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class ItemTouchHelperCallbackTest {

    @MockK
    private lateinit var mockHelperAdapter: ItemTouchHelperAdapter

    @MockK
    private lateinit var mockRecyclerView: RecyclerView

    private lateinit var context: Context
    private lateinit var itemTouchHelperCallback: ItemTouchHelperCallback

    private lateinit var stubImplementingHelperViewHolder: MyStubImplementingHelperViewHolder
    private lateinit var stubNonImplementingViewHolder: MyStubNonImplementingHelperViewHolder

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        context = ApplicationProvider.getApplicationContext()

        itemTouchHelperCallback = ItemTouchHelperCallback(mockHelperAdapter)

        stubImplementingHelperViewHolder = MyStubImplementingHelperViewHolder(View(context))
        stubNonImplementingViewHolder = MyStubNonImplementingHelperViewHolder(View(context))
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    /**
     *  getMovementFlags é composto por 3 conjuntos de 8 bits.
     *  Idle, Swipe e Drag -> 0-7, 8-15, 16-23
     *  Cada conjunto tem um espaço especifico de suas flags que ele usa para deslocar seus valores para esquerda.
     *  Mesmo com esses espaços especificos, ele empacota as duas informações (drag e swipe) em um único inteiro,
     *  colocando cada uma, em uma seção diferente.
     *  Usando 8 bits para deslocar swipe para esquerda, e 16 bits para deslocar o drag para esquerda.
     *
     *  Para testar, é preciso fazer o reverso e descolar os valores para a direta usando 8 bits para swap, e 16 bits para o drag.
     *  Usando a função shl e shr, é possivel deslocar tanto para esquerda, quanto para direita esses valores.
     *  Por isso usamos as mascaras de 8 bits 0xFF que serve como o filtro para essa operação.
     *  Ela olha apenas para os 8 bits que interessa e ignora completamente os 32 restantes.
     */

    @Test
    fun `should set movement flags when getMovementFlags is called`() {
        val expectedDragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val expectedSwipeFlags = 0

        val packedMovementFlags =
            itemTouchHelperCallback.getMovementFlags(
                mockRecyclerView,
                stubImplementingHelperViewHolder
            )

        val dragMask = 0xFF shl 16
        val swipeMask = 0xFF shl 8

        val extractedDragFlags = (packedMovementFlags and dragMask) shr 16
        val extractedSwipeFlags = (packedMovementFlags and swipeMask) shr 8

        assertEquals(expectedDragFlags, extractedDragFlags)
        assertEquals(expectedSwipeFlags, extractedSwipeFlags)
    }

    @Test
    fun `should trigger onRowMove on adapter when onMove is called`() {
        val fromPosition = 1
        val toPosition = 4
        val fromViewHolder = mockk<RecyclerView.ViewHolder>()
        val toViewHolder = mockk<RecyclerView.ViewHolder>()
        every { fromViewHolder.adapterPosition } returns fromPosition
        every { toViewHolder.adapterPosition } returns toPosition
        every { mockHelperAdapter.onRowMove(any(), any()) } just Runs

        val result = itemTouchHelperCallback.onMove(mockRecyclerView, fromViewHolder, toViewHolder)

        assertTrue(result)
        verify(exactly = 1) { mockHelperAdapter.onRowMove(fromPosition, toPosition) }
    }

    @Test
    fun `should call onItemSelected when state is drag and holder implements interface`() {
        itemTouchHelperCallback.onSelectedChanged(
            stubImplementingHelperViewHolder,
            ItemTouchHelper.ACTION_STATE_DRAG
        )

        assertTrue(stubImplementingHelperViewHolder.onItemSelectedCalled)
    }

    @Test
    fun `should not call onItemSelected when state does not is drag`() {
        itemTouchHelperCallback.onSelectedChanged(
            stubImplementingHelperViewHolder,
            ItemTouchHelper.ACTION_STATE_IDLE
        )

        assertFalse(stubImplementingHelperViewHolder.onItemSelectedCalled)
    }

    @Test
    fun `should not call onItemSelected when state is drag but holder does not implements interface`() {
        itemTouchHelperCallback.onSelectedChanged(
            stubNonImplementingViewHolder,
            ItemTouchHelper.ACTION_STATE_DRAG
        )

        assertFalse(stubNonImplementingViewHolder.onItemSelectedCalled)
    }

    @Test
    fun `should call onItemClear when state is drag ends and holder implements interface`() {
        itemTouchHelperCallback.clearView(mockRecyclerView, stubImplementingHelperViewHolder)

        assertTrue(stubImplementingHelperViewHolder.onItemClearCalled)
    }

    @Test
    fun `should not call onItemClear when state is drag ends but holder does not implements interface`() {
        itemTouchHelperCallback.clearView(mockRecyclerView, stubNonImplementingViewHolder)

        assertFalse(stubNonImplementingViewHolder.onItemClearCalled)
    }
}

class MyStubImplementingHelperViewHolder(view: View) : RecyclerView.ViewHolder(view),
    ItemTouchHelperViewHolder {
    var onItemSelectedCalled = false
    var onItemClearCalled = false

    override fun onItemSelected() {
        onItemSelectedCalled = true
    }

    override fun onItemClear() {
        onItemClearCalled = true
    }
}

class MyStubNonImplementingHelperViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var onItemSelectedCalled = false
    var onItemClearCalled = false
}