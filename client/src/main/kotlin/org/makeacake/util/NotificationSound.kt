package org.makeacake.util

import org.makeacake.Main
import java.io.BufferedInputStream
import javax.sound.sampled.AudioSystem

/**
 * Notification sound
 */
object NotificationSound {
    fun play() {
        try {
            val audioFileInputStream = Main::class.java.classLoader.getResourceAsStream("notification.wav")
            val audioBufferedInputStream = BufferedInputStream(audioFileInputStream)
            val ais = AudioSystem.getAudioInputStream(audioBufferedInputStream)
            val clip = AudioSystem.getClip()
            clip.open(ais)
            clip.setFramePosition(0)
            clip.start()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}