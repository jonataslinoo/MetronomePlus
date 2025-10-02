package br.com.jonatas.metronomeplus.util

import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit

object MyCustomListenerHelpers {

    fun performShortClick(view: View) {
        val downTime = SystemClock.uptimeMillis()
        val eventTimeUp = downTime + 10
        val x = view.width / 2f
        val y = view.height / 2f
        val metaState = 0

        val downEvent =
            MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, metaState)
        view.dispatchTouchEvent(downEvent)

        val upEvent =
            MotionEvent.obtain(downTime, eventTimeUp, MotionEvent.ACTION_UP, x, y, metaState)
        view.dispatchTouchEvent(upEvent)
    }

    fun performLongClick(
        view: View,
        duration: Long,
        finalEventAction: Int = MotionEvent.ACTION_UP,
    ) {
        val downTime = SystemClock.uptimeMillis()
        val x = view.width / 2f
        val y = view.height / 2f
        val metaState = 0

        val downEvent =
            MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, metaState)
        view.dispatchTouchEvent(downEvent)

        ShadowLooper.idleMainLooper(duration, TimeUnit.MILLISECONDS)

        val eventTimeUp = downTime + duration
        val upEvent =
            MotionEvent.obtain(downTime, eventTimeUp, finalEventAction, x, y, metaState)
        view.dispatchTouchEvent(upEvent)
    }
}
