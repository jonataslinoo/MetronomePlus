package br.com.jonatas.metronomeplus.domain.provider

import android.content.res.AssetManager

interface AssetProvider {
    fun getAssets(): AssetManager
}