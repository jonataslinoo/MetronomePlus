package br.com.jonatas.metronomeplus.domain.util.extensions

import br.com.jonatas.metronomeplus.domain.model.Song

fun List<Song>.filterSongs(query: String): List<Song> {
    if (query.isBlank()) return this

    return filter { songUi ->

        songUi.title.startsWith(query, ignoreCase = true) ||
                songUi.artist.startsWith(query, ignoreCase = true) ||
                songUi.title.contains(query, ignoreCase = true) ||
                songUi.artist.contains(query, ignoreCase = true)

    }.sortedWith(compareBy<Song> {
        when {
            it.title.startsWith(query, ignoreCase = true) -> 0
            it.artist.startsWith(query, ignoreCase = true) -> 1
            else -> 2
        }
    }.thenBy { it.title }.thenBy { it.artist })
}

fun List<Song>.selectSongs(selectedIds: Set<String>): List<Song> {
    return map { song ->
        if (song.id in selectedIds) song.copy(selected = !song.selected)
        else song
    }
}
