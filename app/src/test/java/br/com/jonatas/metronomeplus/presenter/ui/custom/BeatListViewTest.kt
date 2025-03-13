package br.com.jonatas.metronomeplus.presenter.ui.custom

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import br.com.jonatas.metronomeplus.presenter.model.BeatStateUiModel
import br.com.jonatas.metronomeplus.presenter.model.BeatUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BeatListViewTest {

    private lateinit var context: Context
    private lateinit var beatListView: BeatListView

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        beatListView = BeatListView(context)
    }

    @Test
    fun `should update the list of beats when receive beats`() {
        val beatsUi = listOf(
            BeatUiModel(BeatStateUiModel.Normal),
            BeatUiModel(BeatStateUiModel.Silence),
            BeatUiModel(BeatStateUiModel.Accent),
            BeatUiModel(BeatStateUiModel.Medium)
        )

        beatListView.updateBeats(beatsUi)

        assertEquals(4, beatListView.childCount)
    }

    @Test
    fun `should load each ImageView with the correct LayoutParams when it receives beats`() {
        val expectedWidth = 0
        val expectedHeight = LinearLayout.LayoutParams.WRAP_CONTENT
        val expectedWeight = 1f
        val expectedMargin = 4
        val beats = listOf(
            BeatUiModel(BeatStateUiModel.Accent),
            BeatUiModel(BeatStateUiModel.Normal)
        )
        beatListView.updateBeats(beats)

        val childAccent = beatListView.getChildAt(0)
        val layoutParamsChildAccent = childAccent.layoutParams as LinearLayout.LayoutParams

        assertTrue(childAccent is ImageView)
        assertEquals(expectedWidth, layoutParamsChildAccent.width)
        assertEquals(expectedHeight, layoutParamsChildAccent.height)
        assertEquals(expectedWeight, layoutParamsChildAccent.weight)
        assertEquals(expectedMargin, layoutParamsChildAccent.marginStart)
        assertEquals(expectedMargin, layoutParamsChildAccent.topMargin)
        assertEquals(expectedMargin, layoutParamsChildAccent.marginEnd)
        assertEquals(expectedMargin, layoutParamsChildAccent.bottomMargin)

        val childNormal = beatListView.getChildAt(1)
        val layoutParamsChildNormal = childNormal.layoutParams as LinearLayout.LayoutParams

        assertTrue(childNormal is ImageView)
        assertEquals(expectedWidth, layoutParamsChildNormal.width)
        assertEquals(expectedHeight, layoutParamsChildNormal.height)
        assertEquals(expectedWeight, layoutParamsChildNormal.weight)
        assertEquals(expectedMargin, layoutParamsChildNormal.marginStart)
        assertEquals(expectedMargin, layoutParamsChildNormal.topMargin)
        assertEquals(expectedMargin, layoutParamsChildNormal.marginEnd)
        assertEquals(expectedMargin, layoutParamsChildNormal.bottomMargin)
    }

    @Test
    fun `should update the bpm and calculate interval correctly when it receives a new bpm`() {
        val bpm = 120

        beatListView.updateBpm(bpm)

        val bpmField = BeatListView::class.java.getDeclaredField("bpm")
        bpmField.isAccessible = true

        assertEquals(bpm, bpmField.get(beatListView))

        val intervalBeatField = BeatListView::class.java.getDeclaredField("intervalBeat")
        intervalBeatField.isAccessible = true

        assertEquals(83L, intervalBeatField.get(beatListView))
    }

    @Test
    fun `should not update the bpm and calculate the interval if the newBpm is the same`() {
        val initialBpm = 120

        beatListView.updateBpm(initialBpm)

        val bpmField = BeatListView::class.java.getDeclaredField("bpm")
        bpmField.isAccessible = true
        val expectedBpm = bpmField.get(beatListView)

        val intervalBeatField = BeatListView::class.java.getDeclaredField("intervalBeat")
        intervalBeatField.isAccessible = true
        val expectedInterval = intervalBeatField.get(beatListView) as Long

        beatListView.updateBpm(initialBpm)

        val bpmFieldTwo = BeatListView::class.java.getDeclaredField("bpm")
        bpmFieldTwo.isAccessible = true

        assertEquals(expectedBpm, bpmFieldTwo.get(beatListView))

        val intervalBeatFieldTwo = BeatListView::class.java.getDeclaredField("intervalBeat")
        intervalBeatFieldTwo.isAccessible = true

        assertEquals(expectedInterval, intervalBeatFieldTwo.get(beatListView))
    }

    @Test
    fun `should correctly calculate the intervals when receiving different bpms`() {
        val testCases = mapOf(
            60 to 166L,
            120 to 83L,
            240 to 41L,
            20 to 500L,
            600 to 16L
        )

        val intervalBeatField = BeatListView::class.java.getDeclaredField("intervalBeat")
        intervalBeatField.isAccessible = true

        for ((bpm, expectedInterval) in testCases) {
            beatListView.updateBpm(bpm)

            assertEquals(expectedInterval, intervalBeatField.get(beatListView))
        }
    }
}