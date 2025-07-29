package ua.pp.lumivoid.commands

import ua.pp.lumivoid.util.Commands
import ua.pp.lumivoid.util.AlreadyConnectedException
import ua.pp.lumivoid.util.AudioController
import ua.pp.lumivoid.util.Vosk

object Connect: Command("!!connect") {
    override fun register() {
        Commands.registerCommand(this.command) { event ->
            try {
                event.message.reply("Connecting to channel!").queue()
                AudioController.connect(event)

                event.channel.sendMessage("Starting vosk...").queue()
                Vosk.addChannel(event.channel)
                Vosk.start()
            } catch (_: AlreadyConnectedException) {
                event.message.reply("Already connected").queue()
            }
        }
    }
}