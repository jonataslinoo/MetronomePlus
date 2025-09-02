package br.com.jonatas.metronomeplus.presenter.extension

import android.widget.TextView
import br.com.jonatas.metronomeplus.presenter.model.TimeSignatureUiModel

fun TimeSignatureUiModel.bindNumeratorToViews(
    firstDigitView: TextView,
    secondDigitView: TextView
) {
    val digits = numerator.toString().padStart(2, ' ')
    firstDigitView.text = getDigit(digits, 0)
    secondDigitView.text = getDigit(digits, 1)
}

private fun getDigit(digits: String, index: Int): CharSequence =
    digits.getOrNull(index)?.toString() ?: ""