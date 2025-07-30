package ua.pp.lumivoid.commands

import ua.pp.lumivoid.util.Commands
import kotlin.system.exitProcess

object Stop: Command("!!stop") {
    override fun register() {
        Commands.registerCommand(this.command) { event ->
            event.message.reply("Stopping").queue()
            exitProcess(0)
        }
    }
}