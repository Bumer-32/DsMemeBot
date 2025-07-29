package ua.pp.lumivoid.commands

import ua.pp.lumivoid.util.Commands
import ua.pp.lumivoid.util.AudioController
import ua.pp.lumivoid.util.Vosk

object Disconnect: Command("!!disconnect") {
    override fun register() {
        Commands.registerCommand(this.command) { event ->
            logger.info("Disconnecting from voice channel")
            event.message.reply("Disconnecting from voice channel.").queue()
            AudioController.disconnect()
            Vosk.disable()
        }
    }
}