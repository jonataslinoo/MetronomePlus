package br.com.jonatas.metronomeplus.presenter.extension

import android.view.View

fun View.setAlphaForState(
    isEnabled: Boolean,
    enabledAlpha: Float = context.enabledAlpha,
    disabledAlpha: Float = context.disabledAlpha
) {
    this.alpha = if (isEnabled) enabledAlpha else disabledAlpha
}

