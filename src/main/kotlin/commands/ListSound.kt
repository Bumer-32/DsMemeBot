package ua.pp.lumivoid.commands

import ua.pp.lumivoid.Main
import ua.pp.lumivoid.util.Commands

object ListSound: Command("!!list") {
    override fun register() {
        Commands.registerCommand(this.command) { event ->
            var names = "```"
            Main.sounds.forEach { sound ->
                names += sound.name + "\n"
            }
            names += "```"

            event.message.reply(names).queue()
        }
    }
}