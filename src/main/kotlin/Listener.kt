package ua.pp.lumivoid

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

object Listener: ListenerAdapter() {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return

        Commands.tryCallCommand(event.message.contentRaw, event)
    }

    override fun onReady(event: ReadyEvent) {
        logger.info("Bot stated as: ${event.jda.selfUser.name}")
    }
}