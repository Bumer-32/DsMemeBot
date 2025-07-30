package ua.pp.lumivoid.commands

import ua.pp.lumivoid.Constants
import ua.pp.lumivoid.Main
import ua.pp.lumivoid.util.Commands
import ua.pp.lumivoid.util.Vosk
import java.io.File

object RemoveSound: Command("!!remove") {
    override fun register() {
        Commands.registerCommand(this.command) { event ->
            val content = event.message.contentRaw
            if (content.replace(this.command, "").isNotEmpty()) {
                val toRemove = Main.sounds.find { it.name == content.replace(this.command, "").trim() }
                if (toRemove != null) {
                    File(Constants.SOUNDS_PATH, "${toRemove.name}.mp3").delete()
                    File(Constants.SOUNDS_PATH, "${toRemove.name}.pcm").delete()
                    Main.sounds.remove(toRemove)
                    Main.writeSoundsFile()
                    Vosk.updateGrammarList()
                    logger.info("Removed sound ${toRemove.name}")
                    event.message.reply("Removed sound ${toRemove.name}").queue()
                }
                else event.message.reply("No such sound!").queue()
            } else {
                event.message.reply("Please specify which sound to remove!").queue()
            }

        }
    }
}