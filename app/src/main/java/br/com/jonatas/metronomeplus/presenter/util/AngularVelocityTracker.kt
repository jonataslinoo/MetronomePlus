package br.com.jonatas.metronomeplus.presenter.util

import android.view.MotionEvent

interface AngularVelocityTracker {
    fun initializeCenterXY(centerX: Float, centerY: Float)
    fun addMovement(event: MotionEvent)
    fun calcAngle(x: Float, y: Float): Float
    fun getAngularVelocity(): Float
    fun clear()
}