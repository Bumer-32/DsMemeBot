package ua.pp.lumivoid.util

import net.dv8tion.jda.api.audio.AudioReceiveHandler
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.audio.CombinedAudio
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.managers.AudioManager
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

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

            currentAudioManager!!.sendingHandler = EchoHandler
            currentAudioManager!!.receivingHandler = EchoHandler
            currentAudioManager!!.openAudioConnection(channel)

            logger.info("Connected to voice channel ${channel.name}")
            event.channel.sendMessage("Successfully connected to voice channel ${channel.name}").queue()
        }
    }

    fun disconnect() {
        currentAudioManager?.closeAudioConnection()
        currentAudioManager = null
    }

    object EchoHandler: AudioSendHandler, AudioReceiveHandler {
        private val queue: Queue<ByteArray> = ConcurrentLinkedQueue()

        override fun canReceiveCombined(): Boolean {
            return queue.size < 10
        }

        override fun handleCombinedAudio(combinedAudio: CombinedAudio) {
            if (combinedAudio.users.isEmpty()) return

            val data = combinedAudio.getAudioData(1.0)
            queue.add(data)
        }

        override fun canProvide(): Boolean {
            return queue.isNotEmpty()
        }

        override fun provide20MsAudio(): ByteBuffer? {
            val data = queue.poll()
            return if (data == null) null else ByteBuffer.wrap(data)
        }

        override fun isOpus(): Boolean {
            return false
        }

    }
}