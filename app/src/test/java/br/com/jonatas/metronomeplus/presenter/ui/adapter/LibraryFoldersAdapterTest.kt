package br.com.jonatas.metronomeplus.presenter.ui.adapter

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.core.app.ApplicationProvider
import br.com.jonatas.metronomeplus.databinding.ViewLibraryFoldersItemBinding
import br.com.jonatas.metronomeplus.presenter.extension.getDateString
import br.com.jonatas.metronomeplus.presenter.extension.getDrawableResId
import br.com.jonatas.metronomeplus.presenter.extension.getMusicCountString
import br.com.jonatas.metronomeplus.presenter.interfaces.OnFolderClickListener
import br.com.jonatas.metronomeplus.presenter.model.FolderUiModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class LibraryFoldersAdapterTest {

    private lateinit var context: Context
    private lateinit var adapter: LibraryFoldersAdapter
    private lateinit var testFolders: List<FolderUiModel>
    private lateinit var parent: ConstraintLayout

    @MockK
    private lateinit var mockOnFolderClickListener: OnFolderClickListener

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        context = ApplicationProvider.getApplicationContext()
        testFolders = listOf(
            FolderUiModel("1", name = "Folder", musics = 0, date = 1735689600000),
            FolderUiModel("2", name = "Folder 2", musics = 2, date = 1739629800000),
            FolderUiModel("3", name = "Folder 3", musics = 3, date = 1742491200000),
            FolderUiModel("4", name = "Folder 4", musics = 10, date = 1746028800000),
        )

        parent = ConstraintLayout(context)
        adapter = LibraryFoldersAdapter(mockOnFolderClickListener)
    }

    @Test
    fun `should returns zero size when receiving an empty list`() {
        assertTrue(adapter.currentList.isEmpty())

        adapter.submitList(emptyList())
        ShadowLooper.idleMainLooper()

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `should return one size when receiving a list with an item`() {
        assertTrue(adapter.currentList.isEmpty())
        val testAnFolder =
            listOf(FolderUiModel(id = "1", name = "Folder", musics = 5, date = 1735689600000))

        adapter.submitList(testAnFolder)
        ShadowLooper.idleMainLooper()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `should return correct size when it receives more items in folder list`() {
        assertTrue(adapter.currentList.isEmpty())

        adapter.submitList(testFolders)
        ShadowLooper.idleMainLooper()

        assertEquals(testFolders.size, adapter.itemCount)
    }

    @Test
    fun `should inflate correct layout when onCreateViewHolder is called`() {
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        assertNotNull(viewHolder)
        assertTrue(viewHolder.itemView is ConstraintLayout)
    }

    @Test
    fun `should bind the data correctly to views when onBindViewHolder is called`() {
        val position = 0
        val folder = testFolders[position]
        val viewHolder = adapter.onCreateViewHolder(parent, 0)
        val binding = ViewLibraryFoldersItemBinding.bind(viewHolder.itemView)

        adapter.submitList(testFolders)
        ShadowLooper.idleMainLooper()
        adapter.onBindViewHolder(viewHolder, position)

        val actualDrawable = binding.folderImage.drawable
        val shadowDrawable = shadowOf(actualDrawable)

        assertEquals(folder.name, binding.name.text.toString())
        assertEquals(
            folder.getMusicCountString(binding.root.context),
            binding.musics.text.toString()
        )
        assertEquals(folder.getDateString(), binding.date.text.toString())
        assertEquals(folder.getDrawableResId(), shadowDrawable.createdFromResId)
    }

    @Test
    fun `should trigger listener when root item is clicked in the first position`() {
        val position = 0
        val folder = testFolders[position]
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.submitList(testFolders)
        ShadowLooper.idleMainLooper()
        adapter.onBindViewHolder(viewHolder, position)
        viewHolder.itemView.performClick()

        verify(exactly = 1) { mockOnFolderClickListener.onFolderClicked(folder) }
        verify(exactly = 0) { mockOnFolderClickListener.onFolderOptionsClicked(any(), any()) }
    }

    @Test
    fun `should trigger listener when root item is clicked in the last position`() {
        val position = 3
        val folder = testFolders[position]
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.submitList(testFolders)
        ShadowLooper.idleMainLooper()
        adapter.onBindViewHolder(viewHolder, position)
        viewHolder.itemView.performClick()

        verify(exactly = 1) { mockOnFolderClickListener.onFolderClicked(folder) }
        verify(exactly = 0) { mockOnFolderClickListener.onFolderOptionsClicked(any(), any()) }
    }

    @Test
    fun `should trigger listener when menu image is clicked`() {
        val position = 2
        val folder = testFolders[position]
        val viewHolder = adapter.onCreateViewHolder(parent, 0)
        val binding = ViewLibraryFoldersItemBinding.bind(viewHolder.itemView)

        adapter.submitList(testFolders)
        ShadowLooper.idleMainLooper()
        adapter.onBindViewHolder(viewHolder, position)
        binding.menuImage.performClick()

        verify(exactly = 1) {
            mockOnFolderClickListener.onFolderOptionsClicked(
                folder,
                binding.menuImage
            )
        }
        verify(exactly = 0) { mockOnFolderClickListener.onFolderClicked(any()) }
    }

    @Test
    fun `should do nothing when the root item or menu image is clicked, but the listener is null`() {
        adapter = LibraryFoldersAdapter(null)
        val position = 2
        val viewHolder = adapter.onCreateViewHolder(parent, 0)
        val binding = ViewLibraryFoldersItemBinding.bind(viewHolder.itemView)

        try {
            adapter.submitList(testFolders)
            ShadowLooper.idleMainLooper()
            adapter.onBindViewHolder(viewHolder, position)
            viewHolder.itemView.performClick()
            binding.menuImage.performClick()
        } catch (e: Exception) {
            fail("Clicking on the root item or menu image should not throw an exception when the listener is null")
        }
    }
}