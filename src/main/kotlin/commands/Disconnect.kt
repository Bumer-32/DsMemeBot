package ua.pp.lumivoid.commands

import ua.pp.lumivoid.Commands
import ua.pp.lumivoid.util.AudioController

object Disconnect: Command("!!disconnect") {
    override fun register() {
        Commands.registerCommand(this.command) { event ->
            logger.info("Disconnecting from voice channel")
            event.message.reply("Disconnecting from voice channel.").queue()
            AudioController.disconnect()
        }
    }
}