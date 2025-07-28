package ua.pp.lumivoid.commands

import ua.pp.lumivoid.Commands
import ua.pp.lumivoid.util.AlreadyConnectedException
import ua.pp.lumivoid.util.AudioController

object Connect: Command("!!connect") {
    override fun register() {
        Commands.registerCommand(this.command) { event ->
            try {
                event.message.reply("Connecting to channel!").queue()
                AudioController.connect(event)
            } catch (_: AlreadyConnectedException) {
                event.message.reply("Already connected").queue()
            }
        }
    }
}