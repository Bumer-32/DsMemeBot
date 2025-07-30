package ua.pp.lumivoid.util

import net.dv8tion.jda.api.audio.AudioReceiveHandler
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.audio.CombinedAudio
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.managers.AudioManager
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import javax.sound.sampled.AudioFormat

object AudioController {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    var currentAudioManager: AudioManager? = null

    fun connect(event: MessageReceivedEvent) {
        if (currentAudioManager != null) throw IllegalStateException("Already connected")

        val member = event.member
        val voiceState = member?.voiceState
        val channel = voiceState?.channel
        if (channel != null) {
            val guild = channel.guild
            currentAudioManager = guild.audioManager

            currentAudioManager!!.sendingHandler = AudioOutputHandler
            currentAudioManager!!.receivingHandler = AudioInputHandler
            currentAudioManager!!.openAudioConnection(channel)

            logger.info("Connected to voice channel ${channel.name}")
            event.channel.sendMessage("Successfully connected to voice channel ${channel.name}").queue()
        }
    }

    fun disconnect() {
        currentAudioManager?.closeAudioConnection()
        currentAudioManager = null
    }

    fun getAudioInput(): Pair<PipedInputStream, AudioFormat> {
        val output = PipedOutputStream()
        val input = PipedInputStream(output)

        val thread = Thread {
            logger.info("New stream")

            while (true) {
                val data = AudioInputHandler.buffer.poll()
                if (data != null) output.write(data)
                else Thread.sleep(10)
            }
        }
        thread.isDaemon = true
        thread.name = "AudioStream"
        thread.start()
        val format = AudioFormat(96000f, 16, 1, true, true)
        return Pair(input, format)
    }

    fun playFile(path: String) {
        val fis = FileInputStream(path)
        synchronized (AudioOutputHandler.queue) {
            AudioOutputHandler.queue.add(fis)
        }
    }

    fun isPlaying(): Boolean = AudioOutputHandler.currentStream != null

    private object AudioInputHandler: AudioReceiveHandler {
        val buffer = ConcurrentLinkedQueue<ByteArray>()

        // receive
        override fun canReceiveCombined(): Boolean = true

        override fun handleCombinedAudio(combinedAudio: CombinedAudio) {
            val data = combinedAudio.getAudioData(1.0)
            buffer.add(data)
        }
    }

    private object AudioOutputHandler: AudioSendHandler {
        val queue = LinkedList<InputStream>()
        var currentStream: InputStream? = null
        private const val BUFFER_SIZE = 3840
        private val buffer = ByteArray(BUFFER_SIZE)


        override fun canProvide(): Boolean {
            if (currentStream == null) {
                synchronized(queue) {
                    currentStream = if (queue.isNotEmpty()) queue.poll() else null
                }
            }
            return currentStream != null
        }

        override fun provide20MsAudio(): ByteBuffer? {
            val stream = currentStream ?: return null
            val read = stream.read(buffer)
            if (read == -1) {
                currentStream?.close()
                currentStream = null
                return null
            }

            return ByteBuffer.wrap(buffer, 0, read)
        }

        override fun isOpus(): Boolean = false
    }
}