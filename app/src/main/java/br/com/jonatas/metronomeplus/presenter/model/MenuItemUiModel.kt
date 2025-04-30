package br.com.jonatas.metronomeplus.presenter.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class MenuItemUiModel(
    val action: FolderMenuActionUiModel,
    @DrawableRes val iconId: Int,
    @StringRes val titleId: Int
)