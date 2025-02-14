package br.com.jonatas.metronomeplus.presenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.jonatas.metronomeplus.data.provider.AssetProvider
import br.com.jonatas.metronomeplus.data.provider.AudioSettingsProvider
import br.com.jonatas.metronomeplus.domain.engine.MetronomeEngine

class MetronomeViewModel(
    private val metronomeEngine: MetronomeEngine,
    private val assetProvider: AssetProvider,
    private val audioSettingsProvider: AudioSettingsProvider
) : ViewModel() {

    init {
        metronomeEngine.initialize(assetProvider.getAssets())

        metronomeEngine.setDefaultStreamValues(
            audioSettingsProvider.getSampleRate(),
            audioSettingsProvider.getFramesPerBurst()
        )
    }
}

class MetronomeViewModelFactory(
    private val metronomeEngine: MetronomeEngine,
    private val assetProvider: AssetProvider,
    private val audioSettingsProvider: AudioSettingsProvider
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MetronomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MetronomeViewModel(metronomeEngine, assetProvider, audioSettingsProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
