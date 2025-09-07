package br.com.jonatas.metronomeplus.presenter.ui.custom

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import br.com.jonatas.metronomeplus.data.mapper.toDomainList
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModelList
import br.com.jonatas.metronomeplus.util.Fixtures
import io.mockk.MockKAnnotations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SongBeatListViewTest {

    private lateinit var context: Context
    private lateinit var songBeatListView: SongBeatListView

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        context = ApplicationProvider.getApplicationContext()
        songBeatListView = SongBeatListView(context)
    }

    @Test
    fun `should update beat list when UpdateBeats is called with data`() {
        val testBeats = Fixtures.mockBeatListDto.toDomainList().toUiModelList()

        assertEquals(0, songBeatListView.childCount)

        songBeatListView.updateBeats(testBeats)

        assertEquals(4, songBeatListView.childCount)
    }

    @Test
    fun `should load the first image with the correct parameters when UpdateBeats is called with data`() {
        val testBeats = Fixtures.mockBeatListDto.toDomainList().toUiModelList()

        songBeatListView.updateBeats(testBeats)

        val childImage = songBeatListView.getChildAt(0)
        val layoutParamsFirstChild = childImage.layoutParams as LinearLayout.LayoutParams

        assertTrue(childImage is ImageView)
        assertEquals(0, layoutParamsFirstChild.marginStart)
        assertEquals(4, layoutParamsFirstChild.marginEnd)
        assertEquals(39, layoutParamsFirstChild.width)
        assertEquals(39, layoutParamsFirstChild.height)
    }

    @Test
    fun `should load the center images with the correct parameters when UpdateBeats is called with data`() {
        val testBeats = Fixtures.mockBeatListDto.toDomainList().toUiModelList()

        songBeatListView.updateBeats(testBeats)

        for (i in 1..<songBeatListView.childCount - 1) {
            val childImage = songBeatListView.getChildAt(i)
            val layoutParamsFirstChild = childImage.layoutParams as LinearLayout.LayoutParams

            assertTrue(childImage is ImageView)
            assertEquals(4, layoutParamsFirstChild.marginStart)
            assertEquals(4, layoutParamsFirstChild.marginEnd)
            assertEquals(39, layoutParamsFirstChild.width)
            assertEquals(39, layoutParamsFirstChild.height)
        }
    }

    @Test
    fun `should load the last image with the correct parameters when UpdateBeats is called with data`() {
        val testBeats = Fixtures.mockBeatListDto.toDomainList().toUiModelList()

        songBeatListView.updateBeats(testBeats)

        val childImage = songBeatListView.getChildAt(songBeatListView.childCount - 1)
        val layoutParamsFirstChild = childImage.layoutParams as LinearLayout.LayoutParams

        assertTrue(childImage is ImageView)
        assertEquals(4, layoutParamsFirstChild.marginStart)
        assertEquals(0, layoutParamsFirstChild.marginEnd)
        assertEquals(39, layoutParamsFirstChild.width)
        assertEquals(39, layoutParamsFirstChild.height)
    }
}