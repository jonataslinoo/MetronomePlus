package br.com.jonatas.metronomeplus.di.app.factory

import androidx.recyclerview.widget.RecyclerView
import br.com.jonatas.metronomeplus.databinding.ViewFolderFormSongItemBinding
import br.com.jonatas.metronomeplus.presenter.ui.adapter.FolderFormSongsAdapter
import br.com.jonatas.metronomeplus.presenter.ui.adapter.util.ItemTouchHelperAdapter
import br.com.jonatas.metronomeplus.presenter.ui.adapter.util.ItemTouchHelperViewHolder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped

@Module
@InstallIn(FragmentComponent::class)
abstract class CallbackAdapterModule {

    @Binds
    @FragmentScoped
    abstract fun bindItemTouchHelperAdapter(folderFormSongsAdapter: FolderFormSongsAdapter): ItemTouchHelperAdapter
}