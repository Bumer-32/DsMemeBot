package ua.pp.lumivoid.commands

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.coroutines.runBlocking
import ua.pp.lumivoid.Constants
import ua.pp.lumivoid.Main
import ua.pp.lumivoid.util.Commands
import ua.pp.lumivoid.sound.SoundData
import ua.pp.lumivoid.util.SoundsComparator
import java.io.File

object AddSound: Command("!!add") {
    override fun register() {
        Commands.registerCommand(this.command) { event ->
            val message = event.message
            val content = message.contentRaw.replace(this.command, "").trim()
            val attachments = message.attachments

            if (content.isEmpty() || content.split(")").size != 2) {
                message.reply("Please add props to your sound!\n" +
                        "eg: ```!!add (My cool sound name) phrase, hello world!, тапочок```").queue()
                return@registerCommand
            }

            if (attachments.isEmpty()) {
                message.reply("Please send audio with command!").queue()
                return@registerCommand
            }

            if (attachments.size > 1) {
                message.reply("Please send only 1 file!").queue()
                return@registerCommand
            }

            if (attachments.first().fileExtension != "mp3") {
                message.reply("Please send .mp3 file!").queue()
                return@registerCommand
            }

            val name = content.split("(").last().split(")").first().replace(Regex("[\\\\/:*?\"<>|]"), "_")
            val phrases = content.split(")").last().split(",").map { it.trim() }

            if (Main.sounds.find { it.name == name } != null) {
                message.reply("Such sound name already exists!").queue()
                return@registerCommand
            }

            val file = File(Constants.SOUNDS_PATH, "$name.mp3")
            val rawFile = File(Constants.SOUNDS_PATH, "$name.pcm")
            val response = runBlocking { Main.httpClient.get(attachments.first().url) }

            if (response.status.isSuccess()) {
                val data = runBlocking { response.body<ByteArray>() }
                file.writeBytes(data)

                logger.info("Downloaded new sound")
            } else {
                message.reply("Failed to download sound!").queue()
                logger.warn("Failed to download sound!")
                return@registerCommand
            }

            logger.info("Converting to raw")
            event.channel.sendMessage("Converting...").queue()
            ProcessBuilder("ffmpeg", "-i", file.absolutePath, "-f", "s16be", "-ar", "48000", "-ac", "2", rawFile.absolutePath).redirectOutput(ProcessBuilder.Redirect.INHERIT).redirectError(ProcessBuilder.Redirect.INHERIT).start()

            Main.sounds.add(SoundData(name, phrases))
            Main.writeSoundsFile()
            SoundsComparator.updatePhrasesList()

            logger.info("Added sound \"$name\" with phrases: $phrases")
            event.channel.sendMessage("Added sound \"$name\" with phrases $phrases").queue()
        }
    }
}
