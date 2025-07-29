package ua.pp.lumivoid.util

import kotlinx.serialization.Serializable

@Serializable
data class SoundData(val name: String, val phrases: List<String>)