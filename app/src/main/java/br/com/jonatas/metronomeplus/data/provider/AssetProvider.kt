package br.com.jonatas.metronomeplus.data.provider

import android.content.res.AssetManager

interface AssetProvider {
    fun getAssets(): AssetManager
}