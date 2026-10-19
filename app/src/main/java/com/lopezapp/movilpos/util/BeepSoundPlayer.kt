package com.lopezapp.movilpos.util

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Utility object for generating instant hardware beep sound feedback using ToneGenerator.
 */
object BeepSoundPlayer {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (_: Throwable) {
            // May fail in non-Android/JVM unit test environments
        }
    }

    fun playBeep() {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
            }
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        } catch (_: Throwable) {
            // May fail in non-Android/JVM unit test environments
        }
    }
}
