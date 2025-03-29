package br.com.jonatas.metronomeplus.presenter.util

import android.view.MotionEvent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private const val DELTA = 0.01f

class AngularVelocityTrackerImplTest {

    private val angularVelocityTracker = AngularVelocityTrackerImpl()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        angularVelocityTracker.initializeCenterXY(centerX = 100f, centerY = 100f)
    }

    @Test
    fun `should calculate and return correct angle`() {
        val angleValues = listOf(
            0f to Pair(100f, 110f),
            90f to Pair(90f, 100f),
            -90f to Pair(110f, 100f),
            -180f to Pair(100f, 90f)
        )

        for ((expectedAngle, coordinate) in angleValues) {
            val actualAngle = angularVelocityTracker.calcAngle(coordinate.first, coordinate.second)
            assertEquals(expectedAngle, actualAngle, DELTA)
        }
    }

    @Test
    fun `should calculate and correct angles for quadrants`() {
        val angleValues = listOf(
            45f to Pair(90f, 110f),
            -45F to Pair(110f, 110f),
            135f to Pair(90f, 90f),
            -135f to Pair(110f, 90f)
        )

        for ((expectedAngle, coordinate) in angleValues) {
            val actualAngle = angularVelocityTracker.calcAngle(coordinate.first, coordinate.second)
            assertEquals(expectedAngle, actualAngle, DELTA)
        }
    }

    @Test
    fun `should return correct negative velocity for counterclockwise rotation`() {
        val event1 = mockk<MotionEvent>()
        val event2 = mockk<MotionEvent>()

        every { event1.x } returns 71f
        every { event1.y } returns 120f
        every { event1.eventTime } returns 1000L

        every { event2.x } returns 90f
        every { event2.y } returns 110f
        every { event2.eventTime } returns 1020L

        angularVelocityTracker.addMovement(event1)
        angularVelocityTracker.addMovement(event2)

        val velocity = angularVelocityTracker.getAngularVelocity()

        assertEquals(-0.5f, velocity, 0.1f)
    }

    @Test
    fun `should return the correct positive velocity for clockwise rotation`() {
        val event1 = mockk<MotionEvent>()
        val event2 = mockk<MotionEvent>()

        every { event1.x } returns 120f
        every { event1.y } returns 114f
        every { event1.eventTime } returns 1000L

        every { event2.x } returns 110f
        every { event2.y } returns 110f
        every { event2.eventTime } returns 1020L

        angularVelocityTracker.addMovement(event1)
        angularVelocityTracker.addMovement(event2)

        val velocity = angularVelocityTracker.getAngularVelocity()

        assertEquals(0.5f, velocity, 0.1f)
    }

    @Test
    fun `should return zero when no movement occurs`() {
        val event1 = mockk<MotionEvent>()
        val event2 = mockk<MotionEvent>()

        every { event1.x } returns 100f
        every { event1.y } returns 100f
        every { event1.eventTime } returns 1000L

        every { event2.x } returns 100f
        every { event2.y } returns 100f
        every { event2.eventTime } returns 1020L

        angularVelocityTracker.addMovement(event1)
        angularVelocityTracker.addMovement(event2)

        val velocity = angularVelocityTracker.getAngularVelocity()

        assertEquals(0f, velocity, 0.1f)
    }
}
