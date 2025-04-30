package br.com.jonatas.metronomeplus.presenter.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.RecyclerView
import br.com.jonatas.metronomeplus.databinding.ViewOptionsMenuItemBinding
import br.com.jonatas.metronomeplus.presenter.model.MenuItemUiModel

class PopupOptionsMenuAdapter(
    private val menuItems: List<MenuItemUiModel> = listOf(),
    private val onMenuItemClicked: (menuItem: MenuItemUiModel) -> Unit,
) : RecyclerView.Adapter<PopupOptionsMenuAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ViewOptionsMenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.bind(menuItem)
    }

    override fun getItemCount(): Int {
        return menuItems.size
    }

    inner class ViewHolder(private val binding: ViewOptionsMenuItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var menuItem: MenuItemUiModel

        fun bind(menuItem: MenuItemUiModel) {
            this.menuItem = menuItem

            with(binding) {
                val drawable = getDrawable(root.context, menuItem.iconId)
                val title = root.context.getString(menuItem.titleId)

                titleMenu.apply {
                    text = title

                    setCompoundDrawablesRelativeWithIntrinsicBounds(
                        drawable, null, null, null
                    )
                    setOnClickListener {
                        onMenuItemClicked(menuItem)
                    }
                }
            }
        }
    }
}