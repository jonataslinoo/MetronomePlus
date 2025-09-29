package br.com.jonatas.metronomeplus.presenter.extension

import android.content.Context
import android.os.Build
import androidx.annotation.DimenRes
import androidx.core.content.res.ResourcesCompat.getFloat
import br.com.jonatas.metronomeplus.R

val Context.enabledAlpha: Float
    get() = getAlphaByVersionSdk(R.dimen.alpha_enabled_view)


val Context.disabledAlpha: Float
    get() = getAlphaByVersionSdk(R.dimen.alpha_disabled_view)


private fun Context.getAlphaByVersionSdk(@DimenRes resInt: Int): Float {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        this.resources.getFloat(resInt)
    else getFloat(this.resources, resInt)
}
