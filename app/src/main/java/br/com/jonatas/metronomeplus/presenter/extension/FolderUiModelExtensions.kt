package br.com.jonatas.metronomeplus.presenter.extension

import android.content.Context
import androidx.annotation.DrawableRes
import br.com.jonatas.metronomeplus.R
import br.com.jonatas.metronomeplus.presenter.model.FolderUiModel
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val DATE_FORMAT = "dd/MM/yyyy HH:mm"

fun FolderUiModel.getDateString(): String {
    val date = Date(this.date)
    val format = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    return format.format(date)
}

fun FolderUiModel.getMusicCountString(context: Context): String {
    val formatPattern = context.resources.getText(R.string.music_count).toString()
    return MessageFormat.format(formatPattern, this.musics)
}

@DrawableRes
fun FolderUiModel.getDrawableResId(): Int = when (this.musics) {
    0 -> R.drawable.ic_folder_empty_white
    in 1..3 -> R.drawable.ic_folder_file_white
    else -> R.drawable.ic_folder_files_white
}

fun FolderUiModel.isDefaultOrEmptyId(): Boolean {
    return !(this.isDefault || this.id.isEmpty())
}