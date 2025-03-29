package br.com.jonatas.metronomeplus.presenter.ui.custom

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.RelativeLayout
import br.com.jonatas.metronomeplus.R
import br.com.jonatas.metronomeplus.databinding.RotaryKnobViewBinding
import br.com.jonatas.metronomeplus.presenter.util.AngularVelocityTracker

interface OnCircularSeekBarChangeListener {
    fun onProgressChanged(value: Float)
}

private const val PROGRESS_DIVIDER = 100

class CircularSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val binding: RotaryKnobViewBinding =
        RotaryKnobViewBinding.inflate(LayoutInflater.from(context), this)

    private var mOnCircularSeekBarChangeListener: OnCircularSeekBarChangeListener? = null
    private var mAngularVelocityTracker: AngularVelocityTracker? = null
    private var enabled: Boolean = true
    private var initialTouchAngle: Float? = null
    private var lastAngle: Float = 0f
    private val center: PointF
        get() = PointF((width / 2).toFloat(), (height / 2).toFloat())
    private val knobMatrix: Matrix = Matrix()

    var speedMultiplier: Float = 1f
        set(value) {
            field = if (value >= 0) {
                value
            } else {
                0f
            }
        }
    var minValue: Float = 0f
        set(value) {
            field = if (value >= 0) {
                value
            } else {
                0f
            }
        }
    var maxValue: Float = 100f
    var value: Float = 20f
        set(value) {
            field = value.coerceIn(minValue, maxValue)
            mOnCircularSeekBarChangeListener?.onProgressChanged(field)
        }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CircularSeekBar,
            0,
            0
        ).apply {
            try {
                enabled = getBoolean(R.styleable.CircularSeekBar_enabled, enabled)
                minValue = getFloat(R.styleable.CircularSeekBar_minValue, minValue)
                maxValue = getFloat(R.styleable.CircularSeekBar_maxValue, maxValue)
                value = getFloat(R.styleable.CircularSeekBar_value, value)
                speedMultiplier = getFloat(
                    R.styleable.CircularSeekBar_speedMultiplier,
                    speedMultiplier
                )

                getDrawable(R.styleable.CircularSeekBar_knobBackgroundDrawable).apply {
                    binding.knobBackgroundImageView.setImageDrawable(this)
                }

                getDrawable(R.styleable.CircularSeekBar_knobRotaryDrawable).apply {
                    binding.knobImageView.setImageDrawable(this)
                }
            } finally {
                recycle()
            }
        }
    }

    fun createAngularVelocityTracker(angularVelocityTracker: AngularVelocityTracker) {
        mAngularVelocityTracker = angularVelocityTracker
    }

    fun setOnCircularSeekBarChangeListener(listener: OnCircularSeekBarChangeListener?) {
        mOnCircularSeekBarChangeListener = listener
    }

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    override fun isEnabled(): Boolean {
        return enabled
    }

    override fun onSizeChanged(xNew: Int, yNew: Int, xOld: Int, yOld: Int) {
        super.onSizeChanged(xNew, yNew, xOld, yOld)
        if (mAngularVelocityTracker == null) {
            isEnabled = false
        } else {
            mAngularVelocityTracker?.initializeCenterXY(centerX = center.x, centerY = center.y)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!enabled) {
            return super.onTouchEvent(event)
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> trackTouchStart(event)
            MotionEvent.ACTION_MOVE -> trackTouchMove(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> trackTouchStop(event)
        }

        return true
    }

    private fun trackTouchStart(event: MotionEvent) {
        mAngularVelocityTracker?.clear()
        setKnobPosition(event.x, event.y)
    }

    private fun trackTouchMove(event: MotionEvent) {
        mAngularVelocityTracker?.addMovement(event)
        setKnobPosition(event.x, event.y)
        mOnCircularSeekBarChangeListener?.onProgressChanged(value)
    }

    private fun trackTouchStop(event: MotionEvent) {
        mAngularVelocityTracker?.clear()
        resetInitialTouchAngle(event.x, event.y)
    }

    private fun setKnobPosition(x: Float, y: Float) {
        val currentTouchAngle = mAngularVelocityTracker?.calcAngle(x, y)

        if (initialTouchAngle == null) {
            initialTouchAngle = currentTouchAngle
        }

        val deltaAngle = currentTouchAngle?.minus(initialTouchAngle!!)
        val newRotation = deltaAngle?.plus(lastAngle)

        updateProgress()
        updateKnobRotation(newRotation)
    }

    private fun updateKnobRotation(angle: Float?) {
        knobMatrix.reset()
        binding.knobImageView.scaleType = ImageView.ScaleType.MATRIX
        angle?.apply {
            knobMatrix.postRotate(angle, width.toFloat() / 2, height.toFloat() / 2)
        }
        binding.knobImageView.imageMatrix = knobMatrix
    }

    private fun resetInitialTouchAngle(x: Float, y: Float) {
        initialTouchAngle?.let { initialTouchAngle ->
            lastAngle += mAngularVelocityTracker?.calcAngle(x, y)?.minus(initialTouchAngle) ?: 0f
        }

        initialTouchAngle = null
    }

    private fun updateProgress() {
        val speed = mAngularVelocityTracker?.getAngularVelocity()

        val newVal = speed?.let {
            (value + maxValue / PROGRESS_DIVIDER * it * speedMultiplier)
                .coerceIn(minValue, maxValue)
        } ?: 0f

        value = newVal
    }
}