package br.com.jonatas.metronomeplus.presenter.ui.adapter

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.core.app.ApplicationProvider
import br.com.jonatas.metronomeplus.R
import br.com.jonatas.metronomeplus.databinding.ViewOptionsMenuItemBinding
import br.com.jonatas.metronomeplus.presenter.model.FolderMenuActionUiModel
import br.com.jonatas.metronomeplus.presenter.model.MenuItemUiModel
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class PopupOptionsMenuAdapterTest {

    private lateinit var context: Context
    private lateinit var adapter: PopupOptionsMenuAdapter
    private lateinit var menuItems: List<MenuItemUiModel>
    private lateinit var parent: ConstraintLayout
    private lateinit var mockOnMenuItemClicked: (menuItem: MenuItemUiModel) -> Unit

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        parent = ConstraintLayout(context)
        mockOnMenuItemClicked = mockk(relaxed = true)

        menuItems = listOf(
            MenuItemUiModel(
                action = FolderMenuActionUiModel.LOAD_INTO_METRONOME,
                iconId = R.drawable.ic_load_folder_white,
                titleId = R.string.title_load_into_metronome,
            ),
            MenuItemUiModel(
                action = FolderMenuActionUiModel.EDIT,
                iconId = R.drawable.ic_edit_folder_white,
                titleId = R.string.title_edit_folder,
            ),
            MenuItemUiModel(
                action = FolderMenuActionUiModel.DELETE,
                iconId = R.drawable.ic_delete_white,
                titleId = R.string.title_delete_folder,
            ),
        )
    }

    @Test
    fun `should return zero size when it does not receive a list`() {
        val expectedSize = 0
        adapter = PopupOptionsMenuAdapter(onMenuItemClicked = mockOnMenuItemClicked)

        val actualSize = adapter.itemCount

        assertEquals(actualSize, expectedSize)
    }

    @Test
    fun `should return the correct list size when it receives a list with items`() {
        val expectedSize = menuItems.size
        adapter = PopupOptionsMenuAdapter(
            menuItems = menuItems,
            onMenuItemClicked = mockOnMenuItemClicked
        )

        val actualSize = adapter.itemCount

        assertEquals(actualSize, expectedSize)
    }

    @Test
    fun `should inflate correct layout when onCreateViewHolder is called`() {
        adapter = PopupOptionsMenuAdapter(
            menuItems = menuItems,
            onMenuItemClicked = mockOnMenuItemClicked
        )
        val viewHolder = adapter.onCreateViewHolder(parent = parent, 0)

        assertNotNull(viewHolder)
        assertTrue(viewHolder.itemView is ConstraintLayout)
    }

    @Test
    fun `should correctly bind the item with the view when onBindViewHolder is called with position zero`() {
        adapter = PopupOptionsMenuAdapter(
            menuItems = menuItems,
            onMenuItemClicked = mockOnMenuItemClicked
        )
        val viewHolder = adapter.onCreateViewHolder(parent, 0)
        val binding = ViewOptionsMenuItemBinding.bind(viewHolder.itemView)
        val position = 0
        val menuItem = menuItems[position]

        adapter.onBindViewHolder(viewHolder, position)

        val drawables = binding.titleMenu.compoundDrawablesRelative
        val actualDrawable = drawables[0]
        val shadowDrawable = shadowOf(actualDrawable)
        val titleMenu = context.getString(menuItem.titleId)

        assertEquals(titleMenu, binding.titleMenu.text.toString())
        assertEquals(menuItem.iconId, shadowDrawable.createdFromResId)
    }

    @Test
    fun `should correctly bind the item with the view when onBindViewHolder is called with all positions`() {
        adapter = PopupOptionsMenuAdapter(
            menuItems = menuItems,
            onMenuItemClicked = mockOnMenuItemClicked
        )
        val viewHolder = adapter.onCreateViewHolder(parent, 0)
        val binding = ViewOptionsMenuItemBinding.bind(viewHolder.itemView)

        menuItems.indices.forEach { index ->
            adapter.onBindViewHolder(viewHolder, index)
            val menuItem = menuItems[index]

            val drawables = binding.titleMenu.compoundDrawablesRelative
            val actualDrawable = drawables[0]
            val shadowDrawable = shadowOf(actualDrawable)
            val titleMenu = context.getString(menuItem.titleId)

            assertEquals(titleMenu, binding.titleMenu.text.toString())
            assertEquals(menuItem.iconId, shadowDrawable.createdFromResId)
        }
    }

    @Test
    fun `should trigger listener when menuItem is clicked in the first position`() {
        adapter = PopupOptionsMenuAdapter(
            menuItems = menuItems,
            onMenuItemClicked = mockOnMenuItemClicked
        )
        val position = 0
        val menuItem = menuItems[position]
        val viewHolder = adapter.onCreateViewHolder(parent, 0)
        val binding = ViewOptionsMenuItemBinding.bind(viewHolder.itemView)

        adapter.onBindViewHolder(viewHolder, position)
        binding.titleMenu.performClick()

        verify(exactly = 1) { mockOnMenuItemClicked(menuItem) }
    }
}