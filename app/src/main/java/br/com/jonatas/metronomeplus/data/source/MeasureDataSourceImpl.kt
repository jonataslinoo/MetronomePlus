package br.com.jonatas.metronomeplus.data.source

import br.com.jonatas.metronomeplus.data.model.BeatDto
import br.com.jonatas.metronomeplus.data.model.BeatStateDto
import br.com.jonatas.metronomeplus.data.model.MeasureDto
import br.com.jonatas.metronomeplus.domain.source.MeasureDataSource

class MeasureDataSourceImpl : MeasureDataSource {
    override suspend fun getMeasure(): MeasureDto {
        return MeasureDto(
            bpm = 120,
            beats = mutableListOf(
                BeatDto(BeatStateDto.Accent),
                BeatDto(BeatStateDto.Normal),
                BeatDto(BeatStateDto.Normal),
                BeatDto(BeatStateDto.Normal),
            )
        )
    }
}