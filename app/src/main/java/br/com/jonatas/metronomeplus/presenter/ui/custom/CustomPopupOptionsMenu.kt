package br.com.jonatas.metronomeplus.presenter.ui.custom

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import br.com.jonatas.metronomeplus.R
import br.com.jonatas.metronomeplus.databinding.ViewOptionsMenuBinding
import br.com.jonatas.metronomeplus.presenter.model.FolderMenuActionUiModel
import br.com.jonatas.metronomeplus.presenter.model.MenuItemUiModel
import br.com.jonatas.metronomeplus.presenter.ui.adapter.PopupOptionsMenuAdapter

class CustomPopupOptionsMenu(
    private val context: Context,
    private val anchorView: View,
    private val menuItems: List<MenuItemUiModel>,
    private val onMenuItemClicked: (action: FolderMenuActionUiModel) -> Unit
) {
    private val binding by lazy {
        ViewOptionsMenuBinding.inflate(
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        )
    }

    private val popupWindow = PopupWindow(
        binding.root,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        true
    )

    private fun showPopupMenu() {
        popupWindow.setBackgroundDrawable(ColorDrawable())
        popupWindow.animationStyle = R.style.popup_window_animation

        with(binding.recyclerViewOptionMenu) {
            layoutManager = LinearLayoutManager(context, VERTICAL, false)
            adapter = PopupOptionsMenuAdapter(menuItems) { menuItem ->
                onMenuItemClicked(menuItem.action)
                popupWindow.dismiss()
            }
        }

        popupWindow.showAsDropDown(anchorView, -460, 0)
    }

    init {
        showPopupMenu()
    }
}