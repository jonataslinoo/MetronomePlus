package br.com.jonatas.metronomeplus.presenter.util

import android.graphics.drawable.Drawable
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatButton

fun AppCompatButton.setHighlightDrawableOnTouchListener(
    newDrawable: Drawable?,
    oldDrawable: Drawable?,
    execute: () -> Unit
) {
    setOnTouchListener { view, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                setCompoundDrawablesRelativeWithIntrinsicBounds(
                    newDrawable, null, null, null
                )
                execute()
                false
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                setCompoundDrawablesRelativeWithIntrinsicBounds(
                    oldDrawable, null, null, null
                )
                false
            }

            else -> view.performClick()
        }
    }
}
