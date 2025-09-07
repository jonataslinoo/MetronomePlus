package br.com.jonatas.metronomeplus.presenter.ui.adapter

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.test.core.app.ApplicationProvider
import br.com.jonatas.metronomeplus.data.mapper.toDomainList
import br.com.jonatas.metronomeplus.databinding.ViewFolderFormSongItemBinding
import br.com.jonatas.metronomeplus.presenter.extension.disabledAlpha
import br.com.jonatas.metronomeplus.presenter.mapper.toUiModelList
import br.com.jonatas.metronomeplus.presenter.model.song.SongUiModel
import br.com.jonatas.metronomeplus.util.Fixtures
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.LEGACY)
class FolderFormSongsAdapterTest {

    private lateinit var context: Context
    private lateinit var songsAdapter: FolderFormSongsAdapter
    private lateinit var testSongs: List<SongUiModel>
    private lateinit var parent: ConstraintLayout
    private lateinit var viewHolder: FolderFormSongsAdapter.ViewHolder

    @MockK
    private lateinit var binding: ViewFolderFormSongItemBinding

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        context = ApplicationProvider.getApplicationContext()
        testSongs = Fixtures.mockAllSongsDto().toDomainList().toUiModelList()
        parent = ConstraintLayout(context)
        songsAdapter = FolderFormSongsAdapter()

        viewHolder = songsAdapter.onCreateViewHolder(parent, 0)
        binding = ViewFolderFormSongItemBinding.bind(viewHolder.itemView)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should returns zero size when receiving`() {
        assertTrue(songsAdapter.currentList.isEmpty())

        songsAdapter.submitList(emptyList())
        ShadowLooper.idleMainLooper()

        assertEquals(0, songsAdapter.itemCount)
    }

    @Test
    fun `should return the correct size when receiving many songs`() {
        val songs = Fixtures.mockAllSongsDto().toDomainList()

        assertTrue(songsAdapter.currentList.isEmpty())

        songsAdapter.submitList(songs.toUiModelList())
        ShadowLooper.idleMainLooper()

        assertEquals(songs.size, songsAdapter.itemCount)
    }

    @Test
    fun `should bind the data correctly to views when onBindViewHolder is called`() {
        val position = 0
        val testSongs = Fixtures.mockAllSongsDto().toDomainList().toUiModelList()
        val songUi = testSongs[position]

        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, position)

        val numeratorDigits = songUi.timeSignature.numerator.toString().padStart(2, ' ')
        val timeSignatureView = binding.songItemViewSignature

        assertEquals(songUi.title, binding.songItemTitle.text.toString())
        assertEquals(songUi.artist, binding.songItemArtist.text.toString())
        assertEquals(songUi.bpm.toString(), binding.songItemBpm.text.toString())
        assertEquals(songUi.beatPatterns.size, binding.songItemBeatListview.childCount)

        assertTrue(binding.songItemOptions.isVisible)
        assertTrue(binding.songItemSelected.isInvisible)
        assertFalse(binding.root.isEnabled)
        assertFalse(binding.songItemOptions.isEnabled)
        assertFalse(binding.songItemSelected.isEnabled)

        assertEquals(
            numeratorDigits[0].toString(),
            timeSignatureView.songSigNumeratorOne.text.toString()
        )
        assertEquals(
            numeratorDigits[1].toString(),
            timeSignatureView.songSigNumeratorTwo.text.toString()
        )
        assertEquals(
            songUi.timeSignature.denominator.toString(),
            timeSignatureView.songSigDenominator.text.toString()
        )
    }

    @Test
    fun `should set views with disabledAlpha when edit mode is disabled`() {
        val testSongs = Fixtures.mockAllSongsDto().toDomainList().toUiModelList()

        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)
        songsAdapter.isEditingEnabled = false

        val timeSignatureView = binding.songItemViewSignature
        val disabledAlpha = context.disabledAlpha

        assertEquals(disabledAlpha, binding.songItemTitle.alpha)
        assertEquals(disabledAlpha, binding.songItemArtist.alpha)
        assertEquals(disabledAlpha, binding.songItemBpm.alpha)
        assertEquals(disabledAlpha, binding.songItemOptions.alpha)
        assertEquals(disabledAlpha, binding.songItemBeatListview.alpha)
        assertEquals(disabledAlpha, timeSignatureView.songSigNumeratorOne.alpha)
        assertEquals(disabledAlpha, timeSignatureView.songSigNumeratorTwo.alpha)
        assertEquals(disabledAlpha, timeSignatureView.songSigBar.alpha)
        assertEquals(disabledAlpha, timeSignatureView.songSigDenominator.alpha)
    }

    @Test
    fun `should set views with enabledAlpha when edit mode is enabled`() {
        val testSongs = Fixtures.mockAllSongsDto().toDomainList().toUiModelList()

        songsAdapter.submitList(testSongs)
        ShadowLooper.idleMainLooper()
        songsAdapter.onBindViewHolder(viewHolder, 0)
        songsAdapter.isEditingEnabled = true

        val timeSignatureView = binding.songItemViewSignature
        val disabledAlpha = context.disabledAlpha

        assertEquals(disabledAlpha, binding.songItemTitle.alpha)
        assertEquals(disabledAlpha, binding.songItemArtist.alpha)
        assertEquals(disabledAlpha, binding.songItemBpm.alpha)
        assertEquals(disabledAlpha, binding.songItemOptions.alpha)
        assertEquals(disabledAlpha, binding.songItemBeatListview.alpha)
        assertEquals(disabledAlpha, timeSignatureView.songSigNumeratorOne.alpha)
        assertEquals(disabledAlpha, timeSignatureView.songSigNumeratorTwo.alpha)
        assertEquals(disabledAlpha, timeSignatureView.songSigBar.alpha)
        assertEquals(disabledAlpha, timeSignatureView.songSigDenominator.alpha)
    }
}