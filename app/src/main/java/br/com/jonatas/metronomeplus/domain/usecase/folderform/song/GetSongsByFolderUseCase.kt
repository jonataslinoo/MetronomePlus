package br.com.jonatas.metronomeplus.domain.usecase.folderform.song

import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface GetSongsByFolderUseCase {
    operator fun invoke(folder: Folder): Flow<List<Song>>
}