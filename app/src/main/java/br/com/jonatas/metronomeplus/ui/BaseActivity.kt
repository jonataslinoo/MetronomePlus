package br.com.jonatas.metronomeplus.ui

import android.content.Context
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(updateConfiguration(base))
    }

    private fun updateConfiguration(context: Context?): Context? {
        var newContext = context ?: return null

        val displayMetrics = context.resources.displayMetrics
        val configuration = context.resources.configuration

        if (displayMetrics.densityDpi != DisplayMetrics.DENSITY_400) {

            configuration.densityDpi = DisplayMetrics.DENSITY_400
            newContext = context.createConfigurationContext(configuration)
        }

        return newContext
    }
}