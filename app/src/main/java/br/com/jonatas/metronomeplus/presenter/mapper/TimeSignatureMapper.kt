package br.com.jonatas.metronomeplus.presenter.mapper

import br.com.jonatas.metronomeplus.domain.model.TimeSignature
import br.com.jonatas.metronomeplus.presenter.model.TimeSignatureUiModel


/** Domain for Ui */
fun TimeSignature.toUiModel(): TimeSignatureUiModel = TimeSignatureUiModel(
    numerator = numerator,
    denominator = denominator
)


/** Ui for Domain */