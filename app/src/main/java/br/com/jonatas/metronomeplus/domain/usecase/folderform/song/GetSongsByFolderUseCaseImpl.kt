package br.com.jonatas.metronomeplus.domain.usecase.folderform.song

import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.model.Song
import br.com.jonatas.metronomeplus.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSongsByFolderUseCaseImpl @Inject constructor(
    private val repository: SongRepository
) : GetSongsByFolderUseCase {
    override fun invoke(folder: Folder): Flow<List<Song>> {
        return if (folder.isDefault || folder.id.isEmpty()) {
            repository.getAllSongs()
        } else {
            repository.getSongsByFolderId(folderId = folder.id)
        }
    }
}