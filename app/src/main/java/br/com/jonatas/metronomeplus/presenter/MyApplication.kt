package br.com.jonatas.metronomeplus.presenter

import android.app.Application
import br.com.jonatas.metronomeplus.data.local.DataStoreManager

class MyApplication : Application() {

    lateinit var dataStoreManager: DataStoreManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        dataStoreManager = DataStoreManager.getInstance(context = this)
    }

    companion object {
        lateinit var instance: MyApplication
            private set
    }
}