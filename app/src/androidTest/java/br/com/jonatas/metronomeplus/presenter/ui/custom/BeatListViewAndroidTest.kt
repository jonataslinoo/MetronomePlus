package br.com.jonatas.metronomeplus.presenter.ui.custom

import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.indices
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import br.com.jonatas.metronomeplus.R
import br.com.jonatas.metronomeplus.presenter.model.BeatStateUiModel
import br.com.jonatas.metronomeplus.presenter.model.BeatUiModel
import br.com.jonatas.metronomeplus.presenter.ui.MainActivity
import br.com.jonatas.metronomeplus.presenter.util.CustomViewMatchers.childOfParentAtIndex
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
class BeatListViewAndroidTest {

    @get:Rule
    var activityRule = ActivityScenarioRule(MainActivity::class.java)

    @RelaxedMockK
    private lateinit var mockOnBeatClickListener: OnBeatClickListener
    private lateinit var beatListView: BeatListView
    private lateinit var beatsUi: List<BeatUiModel>
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        beatsUi = listOf(
            BeatUiModel(BeatStateUiModel.Normal),
            BeatUiModel(BeatStateUiModel.Silence),
            BeatUiModel(BeatStateUiModel.Accent),
            BeatUiModel(BeatStateUiModel.Medium)
        )

        activityRule.scenario.onActivity { activity ->
            beatListView = activity.findViewById(R.id.beatListView)

            beatListView.setOnBeatClickListener(mockOnBeatClickListener)
            beatListView.updateBeats(beatsUi)
            beatListView.updateBpm(60)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun updateBeats_shouldDisplayCorrectNumberOfBeats() {

        onView(withId(R.id.beatListView))
            .check(matches(isDisplayed()))
            .check(matches(hasChildCount(4)))
    }

    @Test
    fun updateBeats_shouldDisplayBeatsWithCorrectDrawables() {

        onView(withId(R.id.beatListView))
            .check(matches(isDisplayed()))

        activityRule.scenario.onActivity { activity ->

            for (index in beatListView.indices) {
                val expectedBitmap =
                    activity.getDrawable(getDrawableRes(beatsUi[index]))?.toBitmap()

                val child = beatListView.getChildAt(index) as? ImageView
                val actualBitmap = child?.drawable?.toBitmap()

                assertTrue(
                    "The bitmap at index $index does not match expected bitmap!",
                    expectedBitmap?.sameAs(actualBitmap) == true
                )
            }
        }
    }

    @Test
    fun shouldCorrectlyDisplayTheBeatChange() = runTest(testDispatcher) {
        onView(childOfParentAtIndex(withId(R.id.beatListView), 0))
            .check(matches(isDisplayed()))
            .check { view, noViewFoundException ->
                noViewFoundException?.let { throw it }

                activityRule.scenario.onActivity { activity ->
                    beatListView.nextBeat(0)

                    val childImageView = view as ImageView
                    val expectedNormalBitmap = activity
                        .getDrawable(R.drawable.beat_item_normal)?.toBitmap()
                    val actualNormalBitmapBeforeChange = childImageView.drawable.toBitmap()

                    assertNotNull(
                        "The expected normal bitmap before change is null!",
                        expectedNormalBitmap
                    )
                    assertTrue(
                        "The expected normal bitmap does not match the current normal bitmap before change!",
                        expectedNormalBitmap?.sameAs(actualNormalBitmapBeforeChange) == true
                    )

                    testScheduler.runCurrent()

                    val expectedNormalBitmapColor = activity
                        .getDrawable(R.drawable.beat_item_normal_color)?.toBitmap()
                    val actualNormalBitmapColor = childImageView.drawable.toBitmap()

                    assertNotNull(
                        "The expected colored normal bitmap is null!",
                        expectedNormalBitmapColor
                    )
                    assertTrue(
                        "The expected colored normal bitmap does not match the current colored normal bitmap!",
                        expectedNormalBitmapColor?.sameAs(actualNormalBitmapColor) == true
                    )

                    testScheduler.advanceUntilIdle()

                    val actualNormalBitmapAfterChange = childImageView.drawable.toBitmap()

                    assertNotNull(
                        "The expected normal bitmap after change is null!",
                        expectedNormalBitmap
                    )
                    assertTrue(
                        "The expected normal bitmap does not match the current normal bitmap after change!",
                        expectedNormalBitmap?.sameAs(actualNormalBitmapAfterChange) == true
                    )
                }
            }
    }

    @Test
    fun shouldCorrectlyDisplayExchangeAllBeats() = runTest(testDispatcher) {
        onView(withId(R.id.beatListView))
            .check(matches(isDisplayed()))

        for (index in beatListView.indices) {
            onView(childOfParentAtIndex(withId(R.id.beatListView), index))
                .check(matches(isDisplayed()))
                .check { view, noViewFoundException ->
                    noViewFoundException?.let { throw it }
                    val childImageView = view as ImageView

                    activityRule.scenario.onActivity { activity ->
                        beatListView.nextBeat(index)

                        val expectedBitmapBeforeChange =
                            activity.getDrawable(getDrawableRes(beatsUi[index]))?.toBitmap()
                        val actualBitmapBeforeChange = childImageView.drawable.toBitmap()

                        assertNotNull(
                            "The expected bitmap for index $index is null!",
                            expectedBitmapBeforeChange
                        )
                        assertTrue(
                            "The actual bitmap doesn't match the expected bitmap in $index!",
                            expectedBitmapBeforeChange?.sameAs(actualBitmapBeforeChange) == true
                        )

                        testScheduler.runCurrent()

                        val expectedBitmapColor =
                            activity.getDrawable(getDrawableColorRes(beatsUi[index]))?.toBitmap()
                        val actualBitmapColor = childImageView.drawable.toBitmap()

                        assertNotNull(
                            "The expected bitmap colored for index $index is null!",
                            expectedBitmapColor
                        )
                        assertTrue(
                            "The actual bitmap colored doesn't match the expected bitmap colored in $index!",
                            expectedBitmapColor?.sameAs(actualBitmapColor) == true
                        )

                        testScheduler.advanceUntilIdle()

                        val actualBitmapAfterChange = childImageView.drawable.toBitmap()

                        assertNotNull(
                            "The expected bitmap for index $index is null!",
                            expectedBitmapBeforeChange
                        )
                        assertTrue(
                            "The actual bitmap doesn't match the expected bitmap in $index!",
                            expectedBitmapBeforeChange?.sameAs(actualBitmapAfterChange) == true
                        )
                    }
                }
        }
    }

    @Test
    fun shouldCallOnBeatClickListenerWithCorrectIndex_whenBeatIsClicked() {

        onView(childOfParentAtIndex(withId(R.id.beatListView), 1)).perform(click())
        verify { mockOnBeatClickListener.onBeatClick(1) }
    }

    @Test
    fun shouldCallOnBeatClickListenerWithCorrectIndex_whenEachBeatIsClicked() {
        onView(withId(R.id.beatListView))
            .check(matches(isDisplayed()))

        activityRule.scenario.onActivity {
            val newBeats = listOf(
                BeatUiModel(BeatStateUiModel.Normal), BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal), BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal), BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal), BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal), BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal), BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal), BeatUiModel(BeatStateUiModel.Normal),
                BeatUiModel(BeatStateUiModel.Normal), BeatUiModel(BeatStateUiModel.Normal),
            )

            beatListView.updateBeats(newBeats)
        }

        for (index in beatListView.indices) {
            onView(childOfParentAtIndex(withId(R.id.beatListView), index)).perform(click())
            verify { mockOnBeatClickListener.onBeatClick(index) }
        }
    }

    private fun getDrawableRes(beatUiModel: BeatUiModel): Int {
        return when (beatUiModel.stateUiModel) {
            BeatStateUiModel.Normal -> R.drawable.beat_item_normal
            BeatStateUiModel.Silence -> R.drawable.beat_item_silence
            BeatStateUiModel.Accent -> R.drawable.beat_item_accent
            BeatStateUiModel.Medium -> R.drawable.beat_item_medium
        }
    }

    private fun getDrawableColorRes(beatUiModel: BeatUiModel): Int {
        return when (beatUiModel.stateUiModel) {
            BeatStateUiModel.Normal -> R.drawable.beat_item_normal_color
            BeatStateUiModel.Silence -> R.drawable.beat_item_silence_color
            BeatStateUiModel.Accent -> R.drawable.beat_item_accent_color
            BeatStateUiModel.Medium -> R.drawable.beat_item_medium_color
        }
    }
}