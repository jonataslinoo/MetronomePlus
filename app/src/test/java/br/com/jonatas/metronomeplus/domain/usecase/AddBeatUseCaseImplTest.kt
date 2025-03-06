package br.com.jonatas.metronomeplus.domain.usecase

import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.BeatState
import org.junit.Assert.assertEquals
import org.junit.Test

class AddBeatUseCaseImplTest {

    private val addBeatUseCase = AddBeatUseCaseImpl()

    @Test
    fun `should add a normal beat to the list when called`() {
        val initialBeats = listOf(Beat(BeatState.Accent))
        val expectedBeats = listOf(Beat(BeatState.Accent), Beat(BeatState.Normal))

        val actualBeats = addBeatUseCase(initialBeats)

        assertEquals(expectedBeats, actualBeats)
    }

    @Test
    fun `should not add a beat when there are already sixteen beats in the list`() {
        val initialBeats = listOf(
            Beat(BeatState.Accent),
            Beat(BeatState.Normal),
            Beat(BeatState.Normal),
            Beat(BeatState.Normal),
            Beat(BeatState.Normal),
            Beat(BeatState.Normal),
            Beat(BeatState.Normal),
            Beat(BeatState.Normal),
            Beat(BeatState.Normal),
            Beat(BeatState.Normal),
            Beat(BeatState.Normal),
            Beat(BeatState.Normal),
            Beat(BeatState.Normal),
            Beat(BeatState.Normal),
            Beat(BeatState.Normal),
            Beat(BeatState.Normal)
        )
        val expectedBeats = initialBeats.toList()

        val actualBeats = addBeatUseCase(initialBeats)

        assertEquals(expectedBeats.size, actualBeats.size)
        assertEquals(expectedBeats, actualBeats)
    }
}