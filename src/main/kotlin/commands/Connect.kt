package ua.pp.lumivoid.commands

import ua.pp.lumivoid.util.Commands
import ua.pp.lumivoid.sound.AudioController
import ua.pp.lumivoid.util.SttManager

object Connect: Command("!!connect") {
    override fun register() {
        Commands.registerCommand(this.command) { event ->
            event.message.reply("Connecting to channel!").queue()
            AudioController.connect(event)
            SttManager.run(event.channel)
        }
    }
}