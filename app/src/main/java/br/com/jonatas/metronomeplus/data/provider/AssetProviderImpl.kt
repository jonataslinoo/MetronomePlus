package br.com.jonatas.metronomeplus.data.provider

import android.content.Context
import android.content.res.AssetManager
import br.com.jonatas.metronomeplus.domain.provider.AssetProvider

class AssetProviderImpl(private val context: Context) : AssetProvider {
    override fun getAssets(): AssetManager = context.assets
}