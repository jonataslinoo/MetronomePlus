package br.com.jonatas.metronomeplus.domain.usecase

import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.BeatState
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoveBeatUseCaseImplTest {

    private val removeBeatUseCase = RemoveBeatUseCaseImpl()

    @Test
    fun `should remove the last beat of the list when called`() {
        val initialBeats = listOf(Beat(BeatState.Accent), Beat(BeatState.Normal))
        val expectedBeats = listOf(Beat(BeatState.Accent))

        val actualBeats = removeBeatUseCase(initialBeats)

        assertEquals(expectedBeats.size, actualBeats.size)
        assertEquals(expectedBeats, actualBeats)
    }

    @Test
    fun `should not remove the beat when there is only one beat`() {
        val initialBeats = listOf(Beat(BeatState.Accent))
        val expectedBeats = listOf(Beat(BeatState.Accent))

        val actualBeats = removeBeatUseCase(initialBeats)

        assertEquals(expectedBeats.size, actualBeats.size)
        assertEquals(expectedBeats, actualBeats)
    }
}