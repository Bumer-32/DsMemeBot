package ua.pp.lumivoid.util

import org.apache.commons.text.similarity.FuzzyScore
import org.slf4j.LoggerFactory
import ua.pp.lumivoid.Constants
import ua.pp.lumivoid.Main
import ua.pp.lumivoid.sound.AudioController
import java.io.File
import java.util.Locale

object SoundsComparator {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val threshold = 7

    private val phrasesList = mutableListOf<String>()
    private var language = "en-US"
    private var fuzzy: FuzzyScore? = null

    private fun matchRecognizedWord(word: String): String? {
        return phrasesList.maxByOrNull { variant ->
            fuzzy!!.fuzzyScore(word, variant)
        }?.takeIf { variant ->
            fuzzy!!.fuzzyScore(word, variant) >= threshold
        }
    }

    fun updatePhrasesList() {
        phrasesList.clear()
        Main.sounds.forEach { sound ->
            sound.phrases.forEach { phrase ->
                phrasesList.add(phrase)
            }
        }
    }

    fun setup(language: String) {
        this.language = language
        logger.info("Set comparator language to $language")
        updatePhrasesList()
        fuzzy = FuzzyScore(Locale.of(language))
    }

    fun recognize(phrase: String) {
        val matched = matchRecognizedWord(phrase)
        if (matched != null) {
            val sound = Main.sounds.find { it.phrases.contains(matched) }
            if (sound != null && !AudioController.isPlaying()) {
                logger.info("Sound \"${sound.name}\" has been sent")
                val file = File(Constants.SOUNDS_PATH, "${sound.name}.pcm")
                AudioController.playFile(file.absolutePath)
            }
        }
    }
}