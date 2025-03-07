package br.com.jonatas.metronomeplus.core

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.FloatRange
import androidx.annotation.Nullable
import br.com.jonatas.metronomeplus.R
import br.com.jonatas.metronomeplus.databinding.RotaryKnobViewBinding
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

class CircularSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val binding: RotaryKnobViewBinding =
        RotaryKnobViewBinding.inflate(LayoutInflater.from(context), this)

    @Nullable
    private var mOnCircularSeekBarChangeListener: OnCircularSeekBarChangeListener? = null
    private var mAngularVelocityTracker: AngularVelocityTracker? = null

    private var mEnabled = true
    private var mMinValue = 0f
    private var mMaxValue = 100f
    private var mProgress = 0f
    private var mSpeedMultiplier: Float = 1f

    private var initialTouchAngle: Float? = null
    private var lastAngle = 0f
    private val center: PointF
        get() = PointF((width / 2).toFloat(), (height / 2).toFloat())

    private var knobDrawable: Drawable? = null
    private var knobBackgroundDrawable: Drawable? = null

    /**
     * Accelerate or decelerate the change in progress relative to the user's circular scrolling movement
     * @param speedMultiplier 0-1 to decrease change, 1+ to increase change
     */
    var speedMultiplier: Float
        get() = mSpeedMultiplier
        set(value) {
            mSpeedMultiplier = value
        }

    var enable: Boolean
        get() = mEnabled
        set(value) {
            mEnabled = value
        }

    var minValue: Float
        get() = mMinValue
        set(value) {
            mMinValue = value
        }

    var maxValue: Float
        get() = mMaxValue
        set(value) {
            mMaxValue = value
        }

    /**
     * Returns the currently displayed value from the view. Depending on the
     * used method to show the value, this value can be percent or actual value.
     * @param progress
     */
    var progress: Float
        get() = mProgress
        set(value) {
            mProgress = value
            if (mOnCircularSeekBarChangeListener != null) {
                mOnCircularSeekBarChangeListener!!.onProgressChanged(this, mProgress, false)
            }
        }

    override fun setEnabled(enabled: Boolean) {
        mEnabled = enabled
    }

    override fun isEnabled(): Boolean {
        return mEnabled
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RotarySeekBar,
            0,
            0
        ).apply {
            try {
                mEnabled = getBoolean(R.styleable.RotarySeekBar_Cenabled, mEnabled)
                mMinValue = getFloat(R.styleable.RotarySeekBar_CminValue, mMinValue)
                mMaxValue = getFloat(R.styleable.RotarySeekBar_CmaxValue, mMaxValue)
                mSpeedMultiplier =
                    getFloat(R.styleable.RotarySeekBar_CspeedMultiplier, speedMultiplier)
                mProgress = getFloat(R.styleable.RotarySeekBar_Cprogress, mProgress)

                knobBackgroundDrawable =
                    getDrawable(R.styleable.RotarySeekBar_CknobBackgroundDrawable)
                binding.knobBackgroundImageView.setImageDrawable(knobBackgroundDrawable)

                knobDrawable = getDrawable(R.styleable.RotarySeekBar_CknobRotaryDrawable)
                binding.knobImageView.setImageDrawable(knobDrawable)
            } finally {
                recycle()
            }
        }
    }

    override fun onSizeChanged(xNew: Int, yNew: Int, xOld: Int, yOld: Int) {
        super.onSizeChanged(xNew, yNew, xOld, yOld)
        mAngularVelocityTracker = AngularVelocityTracker(center.x, center.y)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mEnabled) {

            when (event.action) {
                MotionEvent.ACTION_DOWN -> trackTouchStart(event)
                MotionEvent.ACTION_MOVE -> trackTouchMove(event)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> trackTouchStop(event)
            }

            return true
        } else {
            return super.onTouchEvent(event)
        }
    }

    private fun trackTouchStart(event: MotionEvent) {
        mAngularVelocityTracker!!.clear()

        setKnobPosition(event.x, event.y)

        if (mOnCircularSeekBarChangeListener != null) {
            mOnCircularSeekBarChangeListener!!.onStartTrackingTouch(this)
        }
    }

    private fun trackTouchMove(event: MotionEvent) {
        mAngularVelocityTracker!!.addMovement(event)

        setKnobPosition(event.x, event.y)

        if (mOnCircularSeekBarChangeListener != null) {
            mOnCircularSeekBarChangeListener!!.onProgressChanged(this, mProgress, true)
        }
    }

    private fun trackTouchStop(event: MotionEvent) {
        mAngularVelocityTracker!!.clear()

        resetInitalTouchAngle(event.x, event.y)

        if (mOnCircularSeekBarChangeListener != null) {
            mOnCircularSeekBarChangeListener!!.onStopTrackingTouch(this)
        }
    }

    /**
     * calculate rotation angle
     * @param x
     * @param y
     */
    private fun setKnobPosition(x: Float, y: Float) {
        val currentTouchAngle = getAngle(x, y)

        if (initialTouchAngle == null) {
            initialTouchAngle = currentTouchAngle
        }

        val deltaAngle = currentTouchAngle - initialTouchAngle!!
        val newRotation = lastAngle + deltaAngle

        updateProgress()
        updateKnobRotation(newRotation)
    }

    /**
     * Calculate rotation angle with progress updates
     */
    fun setKnobPositionUpdatePrgoress(){

    }

    /**
     * update rotation display with the given touch position
     * @param angle
     */
    private fun updateKnobRotation(angle: Float) {
        val matrix = Matrix()
        binding.knobImageView.scaleType = ImageView.ScaleType.MATRIX
        matrix.postRotate(angle, width.toFloat() / 2, height.toFloat() / 2)
        binding.knobImageView.imageMatrix = matrix
    }

    /**
     * resets the initial touch angle
     * @param x
     * @param y
     */
    private fun resetInitalTouchAngle(x: Float, y: Float) {
        if (initialTouchAngle != null) {
            lastAngle += getAngle(x, y) - initialTouchAngle!!
        }
        initialTouchAngle = null
    }

    /**
     * calculate the new value depending on progress
     */
    private fun updateProgress() {
        val speed = mAngularVelocityTracker!!.angularVelocity

        var newVal = mProgress + mMaxValue / 100 * speed * speedMultiplier
        newVal = min(newVal.toDouble(), mMaxValue.toDouble()).toFloat()
        newVal = max(newVal.toDouble(), mMinValue.toDouble()).toFloat()
        mProgress = newVal
    }

    /**
     * return angle relative to the view center for the given point on the chart in degrees
     * @param x
     * @param y
     * @return angle in degrees. 0° is NORTH
     */
    @FloatRange(from = 0.0, to = 360.0)
    private fun getAngle(x: Float, y: Float): Float {
        return -Math.toDegrees(atan2((center.x - x).toDouble(), (center.y - y).toDouble()))
            .toFloat()
    }

    /**
     * Set a listener for touch-events related to the CircularSeekBar
     * @param listener
     */
    fun setOnCircularSeekBarChangeListener(@Nullable listener: OnCircularSeekBarChangeListener?) {
        mOnCircularSeekBarChangeListener = listener
    }

    /**
     * Listen for touch-events on the ring area
     */
    interface OnCircularSeekBarChangeListener {
        fun onProgressChanged(seekBar: CircularSeekBar?, progress: Float, fromUser: Boolean)
        fun onStartTrackingTouch(seekBar: CircularSeekBar?)
        fun onStopTrackingTouch(seekBar: CircularSeekBar?)
    }
}