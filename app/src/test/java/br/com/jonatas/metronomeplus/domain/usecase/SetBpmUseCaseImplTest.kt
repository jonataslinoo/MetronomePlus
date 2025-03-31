package br.com.jonatas.metronomeplus.domain.usecase

import br.com.jonatas.metronomeplus.domain.util.MAX_BPM_VALUE
import br.com.jonatas.metronomeplus.domain.util.MIN_BPM_VALUE
import org.junit.Assert.assertEquals
import org.junit.Test

class SetBpmUseCaseImplTest {

    private val setBpmUseCase = SetBpmUseCaseImpl()

    @Test
    fun `should set bpm when receiving a value`() {
        val values = listOf(
            100,
            50,
            200,
            375,
            20,
            35,
            600
        )

        for (bpm in values) {
            val actualBpm = setBpmUseCase(bpm)

            assertEquals(bpm, actualBpm)
        }
    }

    @Test
    fun `should not set bpm when receiving a value lower than the minimum bpm value`() {
        val bpm = 10

        val actualBpm = setBpmUseCase(bpm)

        assertEquals(MIN_BPM_VALUE, actualBpm)
    }

    @Test
    fun `should not set bpm when receiving a value higher than the maximum bpm value`() {
        val bpm = 650

        val actualBpm = setBpmUseCase(bpm)

        assertEquals(MAX_BPM_VALUE, actualBpm)
    }
}