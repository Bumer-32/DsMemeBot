package ua.pp.lumivoid.commands

import ua.pp.lumivoid.sound.AudioController
import ua.pp.lumivoid.util.Commands
import ua.pp.lumivoid.util.SttManager
import kotlin.system.exitProcess

object Stop: Command("!!stop") {
    override fun register() {
        Commands.registerCommand(this.command) { event ->
            event.message.reply("Stopping").queue()
            SttManager.stop()
            AudioController.disconnect()
            exitProcess(0)
        }
    }
}