package com.example.openliberty.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.openliberty.model.RadioStation
import kotlin.math.PI
import kotlin.math.sin

class SoundSynth {
    private var audioTrack: AudioTrack? = null
    private var isRunning = false
    private var synthThread: Thread? = null

    // Audio state parameters
    @Volatile var masterVolume: Float = 0.8f
    @Volatile var sfxVolume: Float = 0.85f
    @Volatile var radioVolume: Float = 0.7f

    @Volatile var currentSpeedFraction: Float = 0f // 0.0 to 1.0
    @Volatile var isEngineRunning: Boolean = false
    @Volatile var isTireScreeching: Boolean = false
    @Volatile var isHornHonking: Boolean = false
    @Volatile var isSirenActive: Boolean = false

    @Volatile var activeStation: RadioStation = RadioStation.HEAD_RADIO
    @Volatile var isRadioPlaying: Boolean = true

    fun start() {
        if (isRunning) return
        isRunning = true

        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
        } catch (e: Exception) {
            isRunning = false
            return
        }

        synthThread = Thread {
            val chunk = ShortArray(1024)
            var phaseEngine = 0.0
            var phaseRadio = 0.0
            var phaseBass = 0.0
            var phaseSiren = 0.0
            var tick = 0L

            while (isRunning) {
                val effectiveMaster = masterVolume
                val effectiveSfx = sfxVolume * effectiveMaster
                val effectiveRadio = if (isRadioPlaying && activeStation != RadioStation.RADIO_OFF) {
                    radioVolume * effectiveMaster
                } else 0f

                for (i in chunk.indices) {
                    var sample = 0.0
                    val t = (tick + i).toDouble() / sampleRate

                    // 1. Vehicle Engine Synthesizer
                    if (isEngineRunning && effectiveSfx > 0f) {
                        val baseFreq = 55.0 + currentSpeedFraction * 140.0
                        val engineAmp = 0.15f * effectiveSfx
                        phaseEngine += 2.0 * PI * baseFreq / sampleRate
                        if (phaseEngine > 2.0 * PI) phaseEngine -= 2.0 * PI
                        
                        // Dual harmonic engine rumble with cylinder distortion
                        val engineWave = sin(phaseEngine) + 0.45 * sin(phaseEngine * 2.0) + 0.2 * sin(phaseEngine * 3.0)
                        sample += engineWave * engineAmp
                    }

                    // 2. Tire Screech Synthesizer (White noise modulated by high frequency)
                    if (isTireScreeching && effectiveSfx > 0f) {
                        val screechNoise = (Math.random() * 2.0 - 1.0) * 0.18 * effectiveSfx
                        sample += screechNoise
                    }

                    // 3. Horn Honk Synthesizer
                    if (isHornHonking && effectiveSfx > 0f) {
                        val hornAmp = 0.35f * effectiveSfx
                        val hornWave = sin(2.0 * PI * 440.0 * t) + 0.8 * sin(2.0 * PI * 554.37 * t)
                        sample += hornWave * hornAmp
                    }

                    // 4. Police Siren Synthesizer
                    if (isSirenActive && effectiveSfx > 0f) {
                        val sirenFreq = 650.0 + sin(2.0 * PI * 1.2 * t) * 250.0
                        phaseSiren += 2.0 * PI * sirenFreq / sampleRate
                        if (phaseSiren > 2.0 * PI) phaseSiren -= 2.0 * PI
                        sample += sin(phaseSiren) * 0.25 * effectiveSfx
                    }

                    // 5. Radio Station Procedural Grooves
                    if (effectiveRadio > 0f) {
                        val bpm = activeStation.tempoBpm
                        val beatIntervalSec = 60.0 / bpm
                        val beatPos = (t % beatIntervalSec) / beatIntervalSec

                        // Kick / rhythm pulse
                        val kickDecay = (1.0 - beatPos).coerceIn(0.0, 1.0)
                        val kick = sin(2.0 * PI * (60.0 + kickDecay * 120.0) * beatPos * beatIntervalSec) * (kickDecay * kickDecay) * 0.22

                        // Bassline note based on station frequency
                        val bassFreq = activeStation.baseFrequency * 1.5
                        phaseBass += 2.0 * PI * bassFreq / sampleRate
                        if (phaseBass > 2.0 * PI) phaseBass -= 2.0 * PI
                        val bass = sin(phaseBass) * 0.12

                        // Arpeggio Melody
                        val arpStep = ((t / (beatIntervalSec / 4.0)).toInt() % 8)
                        val noteOffset = when (arpStep) {
                            0 -> 1.0
                            1 -> 1.2
                            2 -> 1.25
                            3 -> 1.5
                            4 -> 1.33
                            5 -> 1.5
                            6 -> 1.8
                            else -> 2.0
                        }
                        val leadFreq = bassFreq * 2.0 * noteOffset
                        phaseRadio += 2.0 * PI * leadFreq / sampleRate
                        if (phaseRadio > 2.0 * PI) phaseRadio -= 2.0 * PI
                        val lead = (sin(phaseRadio) + 0.3 * sin(phaseRadio * 2.0)) * 0.08

                        sample += (kick + bass + lead) * effectiveRadio
                    }

                    // Clamping to 16-bit PCM range
                    val clamped = (sample.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
                    chunk[i] = clamped
                }

                tick += chunk.size
                audioTrack?.write(chunk, 0, chunk.size)
            }
        }.apply {
            isDaemon = true
            start()
        }
    }

    fun stop() {
        isRunning = false
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
