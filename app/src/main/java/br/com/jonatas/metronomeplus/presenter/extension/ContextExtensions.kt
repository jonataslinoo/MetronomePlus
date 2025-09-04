package br.com.jonatas.metronomeplus.presenter.extension

import android.content.Context
import br.com.jonatas.metronomeplus.R

val Context.enabledAlpha: Float
    get() = this.resources.getFloat(R.dimen.alpha_enabled_view)

val Context.disabledAlpha: Float
    get() = this.resources.getFloat(R.dimen.alpha_disabled_view)
