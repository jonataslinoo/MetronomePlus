package br.com.jonatas.metronomeplus.presenter.ui.custom

import android.content.Context
import android.view.MotionEvent
import androidx.test.core.app.ApplicationProvider
import br.com.jonatas.metronomeplus.presenter.util.AngularVelocityTracker
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val DELTA = 0.01f

@RunWith(RobolectricTestRunner::class)
class CircularSeekBarTest {

    private lateinit var context: Context
    private lateinit var circularSeekBar: CircularSeekBar

    @RelaxedMockK
    private lateinit var mockAngularVelocityTracker: AngularVelocityTracker

    @RelaxedMockK
    private lateinit var mockChangeListener: OnCircularSeekBarChangeListener

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        context = ApplicationProvider.getApplicationContext()

        circularSeekBar = CircularSeekBar(context)
    }

    @Test
    fun `should contain the minimum value in minValue variable`() {
        val minValue = circularSeekBar.minValue
        assertEquals(0.0f, minValue, DELTA)

        circularSeekBar.minValue = 20.0f

        val newMinValue = circularSeekBar.minValue
        assertEquals(20.0f, newMinValue, DELTA)
    }

    @Test
    fun `should contain the zero value when minValue receives a negative value`() {
        circularSeekBar.minValue = -10.0f

        val minValue = circularSeekBar.minValue
        assertEquals(0.0f, minValue, DELTA)
    }

    @Test
    fun `should contain the maximum value in maxValue variable`() {
        val maxValue = circularSeekBar.maxValue
        assertEquals(100.0f, maxValue, DELTA)

        circularSeekBar.maxValue = 600.0f

        val newMaxValue = circularSeekBar.maxValue
        assertEquals(600.0f, newMaxValue, DELTA)
    }

    @Test
    fun `should contain the progress value in progress variable between min and maxValue`() {
        val progress = circularSeekBar.value
        assertEquals(20.0f, progress, DELTA)

        circularSeekBar.value = 50.0f

        val newProgress = circularSeekBar.value
        assertEquals(50.0f, newProgress, DELTA)
    }

    @Test
    fun `should not receive progress value less than minValue`() {
        circularSeekBar.minValue = 50.0f
        circularSeekBar.value = 30.0f

        val progress = circularSeekBar.value
        assertEquals(50.0f, progress, DELTA)
    }

    @Test
    fun `should not receive progress value more than maxValue`() {
        circularSeekBar.maxValue = 600.0f
        circularSeekBar.value = 650.0f

        val progress = circularSeekBar.value
        assertEquals(600.0f, progress, DELTA)
    }

    @Test
    fun `should contain the speedMultiplier with value zero or more`() {
        val speedMultiplier = circularSeekBar.speedMultiplier
        assertEquals(1f, speedMultiplier, DELTA)

        circularSeekBar.speedMultiplier = 0.5f
        val newSpeedMultiplier = circularSeekBar.speedMultiplier

        assertEquals(0.5f, newSpeedMultiplier, DELTA)
    }

    @Test
    fun `should not receive a negative value for speedMultiplier`() {
        circularSeekBar.speedMultiplier = -1f

        val speedMultiplier = circularSeekBar.speedMultiplier

        assertEquals(0f, speedMultiplier, DELTA)
    }

    @Test
    fun `should update enable variable when isEnabled receive a new value`() {
        circularSeekBar.isEnabled = false
        assertFalse(circularSeekBar.isEnabled)

        circularSeekBar.isEnabled = true
        assertTrue(circularSeekBar.isEnabled)
    }

    @Test
    fun `should not handle touch events when circularSeekBar is disabled`() {
        circularSeekBar.isEnabled = false

        val event = mockk<MotionEvent>(relaxed = true)
        circularSeekBar.onTouchEvent(event)

        verify { mockAngularVelocityTracker wasNot Called }
        verify(exactly = 0) { mockChangeListener.onProgressChanged(any()) }
    }

    @Test
    fun `should handle touch down events when handle touch down events is triggered`() {
        circularSeekBar.apply {
            createAngularVelocityTracker(mockAngularVelocityTracker)
            setOnCircularSeekBarChangeListener(mockChangeListener)

            isEnabled = true
        }

        val event = mockk<MotionEvent>(relaxed = true)
        coEvery { event.action }.returns(MotionEvent.ACTION_DOWN)
        circularSeekBar.onTouchEvent(event)

        verify(exactly = 1) { mockAngularVelocityTracker.clear() }
        verify(exactly = 1) { mockAngularVelocityTracker.calcAngle(event.x, event.y) }
        verify(exactly = 1) { mockAngularVelocityTracker.getAngularVelocity() }
    }

    @Test
    fun `should handle touch up or cancel events when handle touch up or cancel events are triggered`() {
        circularSeekBar.apply {
            createAngularVelocityTracker(mockAngularVelocityTracker)
            setOnCircularSeekBarChangeListener(mockChangeListener)

            isEnabled = true
        }

        val event = mockk<MotionEvent>(relaxed = true)
        coEvery { event.action }.returns(MotionEvent.ACTION_UP)
        coEvery { event.action }.returns(MotionEvent.ACTION_CANCEL)
        circularSeekBar.onTouchEvent(event)

        verify(exactly = 1) { mockAngularVelocityTracker.clear() }
    }

    @Test
    fun `should handle touch move events when handle touch move events is triggered`() {
        circularSeekBar.apply {
            createAngularVelocityTracker(mockAngularVelocityTracker)
            setOnCircularSeekBarChangeListener(mockChangeListener)

            isEnabled = true
        }

        val event = mockk<MotionEvent>(relaxed = true)
        coEvery { event.action }.returns(MotionEvent.ACTION_MOVE)
        circularSeekBar.onTouchEvent(event)

        verify(exactly = 1) { mockAngularVelocityTracker.addMovement(event) }
        verify(exactly = 1) { mockAngularVelocityTracker.calcAngle(event.x, event.y) }
        verify(exactly = 1) { mockAngularVelocityTracker.getAngularVelocity() }
        verify { mockChangeListener.onProgressChanged(circularSeekBar.value) }
    }
}