package br.com.jonatas.metronomeplus.utils

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.FloatRange
import androidx.annotation.Nullable
import br.com.jonatas.metronomeplus.R
import br.com.jonatas.metronomeplus.databinding.RotaryKnobViewBinding
import br.com.jonatas.metronomeplus.teste.AngularVelocityTracker
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

class CircularSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    /**
     * Listen for touch-events on the ring area > Ouça eventos de toque na área do ringue
     */
    interface OnCircularSeekBarChangeListener {
        fun onProgressChanged(seekBar: CircularSeekBar?, progress: Float, fromUser: Boolean)
        fun onStartTrackingTouch(seekBar: CircularSeekBar?)
        fun onStopTrackingTouch(seekBar: CircularSeekBar?)
    }

    // settable by the client through attributes and programmatically > configurável pelo cliente através de atributos e programaticamente
    @Nullable
    private var mOnCircularSeekBarChangeListener: OnCircularSeekBarChangeListener? = null

    private var mEnabled = true

    private var mMinValue = 0f
    private var mMaxValue = 100f


    var min: Float
        get() = mMinValue
        /**
         * Minimum possible value of the progress > Valor mínimo possível do progresso
         * @param min
         */
        set(min) {
            mMinValue = min
            progress = min(mMinValue.toDouble(), mProgress.toDouble()).toFloat()
        }

    var max: Float
        get() = mMaxValue
        /**
         * Maximum possible value of the progress > Valor máximo possível do progresso
         * @param max
         */
        set(max) {
            mMaxValue = max
        }

    var progress: Float
        /**
         * Returns the currently displayed value from the view. Depending on the
         * used method to show the value, this value can be percent or actual value.
         *
         * Retorna o valor atualmente exibido na visualização. Dependendo do
         * método utilizado para mostrar o valor, este valor pode ser percentual ou valor real.
         *
         * @return
         */
        get() = mProgress
        /**
         * Set current value of the progress > Defina o valor atual do progresso
         * @param progress
         */
        set(progress) {
            mProgress = progress
            if (mOnCircularSeekBarChangeListener != null) {
                mOnCircularSeekBarChangeListener!!.onProgressChanged(this, mProgress, false)
            }
            invalidate()
        }


    /**
     * Accelerate or decelerate the change in progress relative to the user's circular scrolling movement
     * @param speedMultiplier 0-1 to decrease change, 1+ to increase change
     *
     * Acelere ou desacelere a mudança em andamento em relação ao movimento de rolagem circular do usuário
     * @param speedMultiplier 0-1 para diminuir a mudança, 1+ para aumentar a mudança
     *
     */
    @FloatRange(from = 0.0)
    var speedMultiplier: Float = 1f
    private var mProgress = 0f

    private var mGestureDetector: GestureDetector? = null

    private var mAngularVelocityTracker: AngularVelocityTracker? = null


    private val center: PointF
        get() = PointF((width / 2).toFloat(), (height / 2).toFloat())

    private var binding: RotaryKnobViewBinding =
        RotaryKnobViewBinding.inflate(LayoutInflater.from(context), this)
    private var knobDrawable: Drawable? = null
    private var knobBackgroundDrawable: Drawable? = null

    /**
     * Enable touch gestures on the CircularSeekBar > Habilite gestos de toque no CircularSeekBar
     * @param enable
     */
    override fun setEnabled(enable: Boolean) {
        mEnabled = enable
        invalidate()
    }

    override fun isEnabled(): Boolean {
        return mEnabled
    }

    private var lastAngle = 0f // Mantém o ângulo atual da imagem
    private var initialTouchAngle: Float? = null // Ângulo inicial do toque

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
                speedMultiplier = getFloat(
                    R.styleable.RotarySeekBar_CspeedMultiplier,
                    speedMultiplier
                )
                mProgress = getFloat(R.styleable.RotarySeekBar_CinitialValue, mProgress)
                knobBackgroundDrawable =
                    getDrawable(R.styleable.RotarySeekBar_CknobBackgroundDrawable)
                binding.knobBackgroundImageView.setImageDrawable(knobBackgroundDrawable)
                knobDrawable = getDrawable(R.styleable.RotarySeekBar_CknobRotaryDrawable)
                binding.knobImageView.setImageDrawable(knobDrawable)

            } finally {
                recycle()
            }
        }

        mGestureDetector = GestureDetector(getContext(), GestureListener())
    }

    //region Lifecycle
    override fun onSizeChanged(xNew: Int, yNew: Int, xOld: Int, yOld: Int) {
        super.onSizeChanged(xNew, yNew, xOld, yOld)
        mAngularVelocityTracker = AngularVelocityTracker(center.x, center.y)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mEnabled) {

            if (mGestureDetector!!.onTouchEvent(event)) {
                return true
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    trackTouchStart(event)
                }

                MotionEvent.ACTION_MOVE -> {
                    trackTouchMove(event)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    trackTouchStop(event)
                }
            }

            return true
        } else {
            return super.onTouchEvent(event)
        }
    }

    private fun trackTouchStart(event: MotionEvent) {
        mAngularVelocityTracker!!.clear()

        //Upadtes rotation progress
        updateProgress(event.x, event.y, mAngularVelocityTracker!!.angularVelocity)

        if (mOnCircularSeekBarChangeListener != null) {
            mOnCircularSeekBarChangeListener!!.onStartTrackingTouch(this)
        }
    }

    private fun trackTouchMove(event: MotionEvent) {
        mAngularVelocityTracker!!.addMovement(event)

        //Upadtes rotation progress
        updateProgress(event.x, event.y, mAngularVelocityTracker!!.angularVelocity)

        if (mOnCircularSeekBarChangeListener != null) {
            mOnCircularSeekBarChangeListener!!.onProgressChanged(this, mProgress, true)
        }
    }

    private fun trackTouchStop(event: MotionEvent) {
        mAngularVelocityTracker!!.clear()

        //updates the last angle as a reference for the next touch - Atualiza o ângulo final como referência para o próximo toque
        if (initialTouchAngle != null) {
            lastAngle += getAngle(event.x, event.y).toFloat() - initialTouchAngle!!
        }

        //resets for next interaction - Reseta para a próxima interação
        initialTouchAngle = null

        if (mOnCircularSeekBarChangeListener != null) {
            mOnCircularSeekBarChangeListener!!.onStopTrackingTouch(this)
        }
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent): Boolean {
            return false
        }
    }

    /**
     * Set a listener for touch-events related to the outer ring of the CircularSeekBar > Defina um ouvinte para eventos de toque relacionados ao anel externo do CircularSeekBar
     * @param listener
     */
    fun setOnCircularSeekBarChangeListener(@Nullable listener: OnCircularSeekBarChangeListener?) {
        mOnCircularSeekBarChangeListener = listener
    }

    fun setKnobPosition(x: Float, y: Float) {
        val currentTouchAngle = getAngle(x, y).toFloat()

        // Se for o primeiro toque após soltar, define como referência inicial
        if (initialTouchAngle == null) {
            initialTouchAngle = currentTouchAngle
        }

        // Calcula a diferença desde o primeiro toque (movimento do usuário)
        val deltaAngle = currentTouchAngle - initialTouchAngle!!

        // Atualiza a rotação total baseada no último ângulo salvo
        val newRotation = lastAngle + deltaAngle

        val matrix = Matrix()
        binding.knobImageView.scaleType = ImageView.ScaleType.MATRIX
        matrix.postRotate(newRotation, width.toFloat() / 2, height.toFloat() / 2)
        binding.knobImageView.imageMatrix = matrix
    }

    /**
     * update display with the given touch position > Atualize a exibição com a posição de toque dada
     *
     * @param x
     * @param y
     */
    private fun updateProgress(x: Float, y: Float, speed: Float) {
        setKnobPosition(x, y)

        // calculate the new value depending on angle > calcule o novo valor dependendo do ângulo
        var newVal = mProgress + mMaxValue / 100 * speed * speedMultiplier
        newVal = min(newVal.toDouble(), mMaxValue.toDouble()).toFloat()
        newVal = max(newVal.toDouble(), mMinValue.toDouble()).toFloat()
        mProgress = newVal
    }

    /**
     * return angle relative to the view center for the given point on the chart in degrees >
     * Retorne o ângulo relativo ao centro da visualização para o ponto no gráfico dado em graus
     *
     * @param x
     * @param y
     * @return angle in degrees. 0° is NORTH
     */
    @FloatRange(from = 0.0, to = 360.0)
    private fun getAngle(x: Float, y: Float): Double {
        return -Math.toDegrees(atan2((center.x - x).toDouble(), (center.y - y).toDouble()))
    }
}