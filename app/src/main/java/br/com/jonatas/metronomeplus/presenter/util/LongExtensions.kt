package br.com.jonatas.metronomeplus.presenter.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val DATE_FORMAT = "dd/MM/yyyy HH:mm"

fun Long.convertToDateString(): String {
    val date = Date(this)
    val format = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    return format.format(date)
}