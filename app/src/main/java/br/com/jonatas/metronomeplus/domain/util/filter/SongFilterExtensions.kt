package br.com.jonatas.metronomeplus.domain.util.filter

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