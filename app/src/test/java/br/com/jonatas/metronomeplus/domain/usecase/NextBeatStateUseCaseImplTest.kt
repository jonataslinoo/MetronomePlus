package br.com.jonatas.metronomeplus.domain.usecase

import br.com.jonatas.metronomeplus.domain.model.Beat
import br.com.jonatas.metronomeplus.domain.model.BeatState
import org.junit.Assert.assertEquals
import org.junit.Test

class NextBeatStateUseCaseImplTest {

    private val nextBeatStateUseCase = NextBeatStateUseCaseImpl()

    @Test
    fun `should change to the next beat state when nextBeatStateUseCase is called`() {
        val index = 0
        val beats = listOf(Beat(state = BeatState.Normal))
        val expectedBeats = listOf(Beat(state = BeatState.Silence))

        val actualBeats = nextBeatStateUseCase(index, beats)

        assertEquals(BeatState.Silence, actualBeats[index].state)
        assertEquals(expectedBeats, actualBeats)
    }

    @Test
    fun `should switch to the next beat state until all states have been switched`() {
        val beats = listOf(
            Beat(BeatState.Normal),
            Beat(BeatState.Silence),
            Beat(BeatState.Accent),
            Beat(BeatState.Medium)
        )
        val testCases = mapOf(
            0 to BeatState.Silence,
            1 to BeatState.Accent,
            2 to BeatState.Medium,
            3 to BeatState.Normal
        )

        for ((index, expectedState) in testCases) {
            val actualBeats = nextBeatStateUseCase(index, beats)

            assertEquals(expectedState, actualBeats[index].state)
        }
    }
}