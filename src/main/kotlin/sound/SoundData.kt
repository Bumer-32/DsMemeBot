package ua.pp.lumivoid.sound

import kotlinx.serialization.Serializable

@Serializable
data class SoundData(val name: String, val phrases: List<String>)