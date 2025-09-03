package br.com.jonatas.metronomeplus.data.mapper

import br.com.jonatas.metronomeplus.data.model.TimeSignatureDto
import br.com.jonatas.metronomeplus.domain.model.TimeSignature

/** Dto for Domain */
fun TimeSignatureDto.toDomain(): TimeSignature = TimeSignature(
    numerator = numerator,
    denominator = denominator
)
