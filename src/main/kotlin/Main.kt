package ua.pp.lumivoid

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.slf4j.LoggerFactory
import ua.pp.lumivoid.commands.AddSound
import ua.pp.lumivoid.commands.Connect
import ua.pp.lumivoid.commands.Disconnect
import ua.pp.lumivoid.commands.ListSound
import ua.pp.lumivoid.commands.Ping
import ua.pp.lumivoid.commands.RemoveSound
import ua.pp.lumivoid.commands.Stop
import ua.pp.lumivoid.sound.SoundData
import ua.pp.lumivoid.util.SoundsComparator
import ua.pp.lumivoid.util.SttManager
import java.io.File


fun main() {
    Main.main()
}

object Main {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val json = Json {
        prettyPrint = true
    }

    val start = System.currentTimeMillis()
    val httpClient = HttpClient(CIO)

    val sounds = mutableListOf<SoundData>()

    fun main() {
        logger.info("Starting Bot")

        val dotenv = dotenv {
            directory = Constants.APP_PATH.absolutePath
        }
        val token = dotenv["DISCORD_TOKEN"]
        val language = dotenv["PREFERRED_LANGUAGE"]

        File(Constants.SOUNDS_PATH).mkdirs()

        val soundsFile = File(Constants.SOUNDS_FILE_PATH)

        if (soundsFile.exists()) {
            val data = json.decodeFromString<MutableList<SoundData>>(soundsFile.readText())
            data.forEach { sounds.add(it) }
        }

        SttManager.prepare()
        SttManager.setLanguage(language)
        SoundsComparator.setup(language)

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
        AddSound.register()
        ListSound.register()
        RemoveSound.register()
        Stop.register()

        logger.info("-=-=-=-=-=-=-REGISTERING COMMANDS-=-=-=-=-=-=-")
    }

    fun writeSoundsFile() {
        val soundsFile = File(Constants.SOUNDS_FILE_PATH)
        soundsFile.writeText(json.encodeToString(sounds))
    }
}
