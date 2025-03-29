package br.com.jonatas.metronomeplus.presenter.util

import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.atan2

class AngularVelocityTrackerImpl : AngularVelocityTracker {

    private var mInitialTime: Long = 0
    private var mFinalTime: Long = 0
    private var mInitialX = 0f
    private var mInitialY = 0f
    private var mFinalX = 0f
    private var mFinalY = 0f
    private var mCenterX = 0f
    private var mCenterY = 0f

    override fun initializeCenterXY(centerX: Float, centerY: Float) {
        mCenterX = centerX
        mCenterY = centerY
    }

    override fun addMovement(event: MotionEvent) {
        mInitialX = mFinalX
        mInitialY = mFinalY
        mInitialTime = mFinalTime
        mFinalX = event.x
        mFinalY = event.y
        mFinalTime = event.eventTime
    }

    override fun calcAngle(x: Float, y: Float): Float {
        return Math.toDegrees(atan2((x - mCenterX).toDouble(), (y - mCenterY).toDouble()) * -1)
            .toFloat()
    }

    override fun getAngularVelocity(): Float {
        var retVal = 0f
        if (mInitialTime != mFinalTime) {
            val timeLapse = mInitialTime - mFinalTime
            val initialAngle = calcAngle(mInitialX, mInitialY)
            val finalAngle = calcAngle(mFinalX, mFinalY)
            if (abs(finalAngle - initialAngle) < 20) {
                retVal = ((finalAngle - initialAngle) / timeLapse) * -1
            }
        }
        return retVal
    }

    override fun clear() {
        mInitialX = 0f
        mInitialY = 0f
        mInitialTime = 0
        mFinalX = 0f
        mFinalY = 0f
        mFinalTime = 0
    }
}