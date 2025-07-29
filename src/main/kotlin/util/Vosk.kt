package ua.pp.lumivoid.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import org.slf4j.LoggerFactory
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import ua.pp.lumivoid.Main
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

object Vosk: Thread("Vosk") {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val json = Json
    private var isRunning = false
    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var channel: MessageChannelUnion? = null

    private var grammarList = listOf<String>()

    init {
        isDaemon = true
        LibVosk.setLogLevel(LogLevel.DEBUG)
    }

    override fun run() {
        logger.info("Starting Vosk")
        updateGrammarListWithoutRestart()
        isRunning = true

        try {
            if (model != null && recognizer != null) return
            val (input, format) = AudioController.getAudioInput()
            val targetFormat = AudioFormat(96000f, 16, 1, true, false)

            val audioInputStream = AudioInputStream(input, format, AudioSystem.NOT_SPECIFIED.toLong())
            val convertedInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream)

            model = Model(ModelManager.getModelPath())
            recognizer = Recognizer(model, 96000f)

            recognizer!!.setGrammar(json.encodeToString(grammarList))

            logger.info("Vosk started!")
            channel?.sendMessage("Ready!")?.queue()

            val bytes = ByteArray(4096)
            while (isRunning) {
                val nbytes = convertedInputStream.read(bytes)
                if (nbytes >= 0) {
                    if (recognizer!!.acceptWaveForm(bytes, nbytes)) {
                        val result = json.decodeFromString<Message>(recognizer!!.finalResult).text
                        if (result.isNotEmpty()) logger.info("result: \"$result\"")
                        val sound = Main.sounds.find { it.phrases.contains(result) }
                        if (sound != null) {
                            logger.info("Sound \"${sound.name}\" has been sent")
                            val file = File(ua.pp.lumivoid.Constants.SOUNDS_PATH, "${sound.name}.pcm")
                            AudioController.playFile(file.absolutePath)
                        }
                    }

                }
            }
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }

    fun disable() {
        logger.info("Stopping Vosk")
        channel?.sendMessage("Stopping Vosk")?.queue()
        isRunning = false
        model?.close()
        recognizer?.close()
        model = null
        recognizer = null
    }

    fun addChannel(channel: MessageChannelUnion) { Vosk.channel = channel }

    fun updateGrammarList() {
        updateGrammarListWithoutRestart()
        if (isRunning) {
            disable()
            start()
        }
    }

    private fun updateGrammarListWithoutRestart() {
        val newList = mutableListOf<String>()
        Main.sounds.forEach { sound ->
            sound.phrases.forEach { phrase ->
                newList.add(phrase)
            }
        }
        grammarList = newList
    }

    @Serializable
    private data class Message(val text: String)
}