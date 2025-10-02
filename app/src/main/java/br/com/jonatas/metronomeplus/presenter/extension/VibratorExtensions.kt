package br.com.jonatas.metronomeplus.presenter.extension

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

fun Vibrator.vibrateHeavyClick() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
        this.vibrate(effect)
    } else {
        @Suppress("DEPRECATION")
        this.vibrate(50)
    }
}