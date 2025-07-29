package ua.pp.lumivoid.util

import net.dv8tion.jda.api.events.message.MessageReceivedEvent

object Commands {
    private val commands = mutableMapOf<String, (event: MessageReceivedEvent) -> Unit>()

    fun registerCommand(command: String, action: (MessageReceivedEvent) -> Unit) {
        commands[command] = action
    }

    fun tryCallCommand(text: String, event: MessageReceivedEvent) {
        commands.forEach {
            if (text.startsWith(it.key) ) {
                it.value.invoke(event)
                return
            }
        }
    }
}