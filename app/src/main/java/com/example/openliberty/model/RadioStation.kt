package com.example.openliberty.model

enum class RadioStation(
    val title: String,
    val genre: String,
    val baseFrequency: Float,
    val tempoBpm: Int,
    val colorHex: Long
) {
    HEAD_RADIO("Head Radio", "Pop / Soft Rock", 104.7f, 120, 0xFFE11D48),
    LIPS_106("Lips 106", "Electro Pop & Dance", 106.3f, 128, 0xFFEC4899),
    FLASHBACK_FM("Flashback 95.6", "80s Synth Classics", 95.6f, 114, 0xFFF59E0B),
    RISE_FM("Rise FM", "Trance & Club", 98.4f, 138, 0xFF06B6D4),
    GAME_RADIO("Game Radio", "Underground Hip-Hop", 89.2f, 92, 0xFF10B981),
    MSX_FM("MSX 101.1", "Drum & Bass", 101.1f, 170, 0xFF8B5CF6),
    RADIO_OFF("Radio Off", "Silence", 0.0f, 0, 0xFF64748B)
}
