package br.com.jonatas.metronomeplus.util

import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import br.com.jonatas.metronomeplus.R

object ThemesContextTest {
    fun createThemedContext(context: Context?): Context {
        return ContextThemeWrapper(context, R.style.Theme_MetronomePlus)
    }
}