package br.com.jonatas.metronomeplus.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class TogglePlayPauseUseCaseImplTest {

    private val togglePlayPauseUseCase = TogglePlayPauseUseCaseImpl()

    @Test
    fun `should be toggled to true when the value of isPlaying is false`() {
        val initialPlaying = false
        val expectedPlaying = true

        val actualPlaying = togglePlayPauseUseCase(initialPlaying)

        assertEquals(expectedPlaying, actualPlaying)
    }

    @Test
    fun `should be toggled to false when the value of isPlaying is true`() {
        val initialPlaying = true
        val expectedPlaying = false

        val actualPlaying = togglePlayPauseUseCase(initialPlaying)

        assertEquals(expectedPlaying, actualPlaying)
    }
}