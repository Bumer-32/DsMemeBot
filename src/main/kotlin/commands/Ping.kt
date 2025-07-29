package ua.pp.lumivoid.commands

import ua.pp.lumivoid.util.Commands
import ua.pp.lumivoid.Main
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object Ping: Command("!!ping") {
    override fun register() {
        Commands.registerCommand(this.command) { event ->
            val uptime = (System.currentTimeMillis() - Main.start).toDuration(DurationUnit.MILLISECONDS).toComponents { days, hours, minutes, seconds, _ ->
                "$days days ${hours}:${minutes}:${seconds}"
            }
            event.message.reply("Pong!\n" +
                    "Hello @${event.message.author.name}! Seems like bot working fine or half fine.\n" +
                    "Current uptime: $uptime").queue()
        }
    }
}