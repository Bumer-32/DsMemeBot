package ua.pp.lumivoid

import io.github.cdimascio.dotenv.Dotenv
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.slf4j.LoggerFactory
import ua.pp.lumivoid.commands.Connect
import ua.pp.lumivoid.commands.Disconnect
import ua.pp.lumivoid.commands.Ping

fun main() {
    Main.main()
}

object Main {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    val start = System.currentTimeMillis()

    fun main() {
        val dotenv = Dotenv.load()
        val token = dotenv["DISCORD_TOKEN"]

        val intents = setOf(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_VOICE_STATES
        )

        JDABuilder.createDefault(token, intents)
            .setStatus(OnlineStatus.DO_NOT_DISTURB)
            .setActivity(Activity.listening("Never gonna give you up"))
            .enableCache(CacheFlag.VOICE_STATE)
            .addEventListeners(Listener)
            .build()

        logger.info("-=-=-=-=-=-=-REGISTERING COMMANDS-=-=-=-=-=-=-")

        Ping.register()
        Connect.register()
        Disconnect.register()

        logger.info("-=-=-=-=-=-=-REGISTERING COMMANDS-=-=-=-=-=-=-")
    }
}
