package ua.pp.lumivoid.util

import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import org.slf4j.LoggerFactory
import ua.pp.lumivoid.Constants
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.zip.ZipFile
import kotlin.concurrent.thread
import kotlin.sequences.forEach

object SttManager {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    private val resourcesCode = this.javaClass.getResource("/stt.zip")!!
    private val pythonEnv = File(Constants.PYTHON_ENV_PATH)
    private val pythonEx = File(pythonEnv, "bin/python")
    private val pipEx = File(pythonEnv, "bin/pip")
    private val code = File(Constants.PYTHON_CODE_PATH)
    private val mainPy = File(code, "main.py")

    private var process: Process? = null
    private var channel: MessageChannelUnion? = null
    private var language = "en-US"

    fun prepare() {
        logger.info("Preparing python...")

        logger.info("Unpacking python code...")

        if (code.exists()) code.deleteRecursively()
        code.mkdirs()

        val archive = File(code, "archive.zip")
        archive.writeBytes(resourcesCode.readBytes())

        ZipFile(archive).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                val outFile = File(code, entry.name)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile.mkdirs()
                    zip.getInputStream(entry).use { input ->
                        outFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }

        archive.delete()


        if (!pythonEnv.exists()) {
            logger.info("Creating python environment...")

            val venvProcess = ProcessBuilder("python3", "-m", "venv", pythonEnv.absolutePath).start()
            redirectOutToLog(venvProcess)
            venvProcess.waitFor()

            val installProcess = ProcessBuilder(pipEx.absolutePath, "install", "--no-cache-dir", "-r", File(code, "requirements.txt").absolutePath).start()
            redirectOutToLog(installProcess)
            installProcess.waitFor()
        }

        logger.info("Prepared.")
    }

    fun run(channel: MessageChannelUnion? = null) {
        logger.info("Starting stt...")
        this.channel = channel
        channel?.sendMessage("Starting stt...")?.queue()

        thread (name = "Internal communication server", isDaemon = true) {
            Server.run()
        }

        process = ProcessBuilder(pythonEx.absolutePath, mainPy.absolutePath, Constants.SERVER_PORT.toString(), language).start()
        redirectOutToLog(process!!)
    }

    fun stop() {
        logger.info("Stopping stt...")
        channel?.sendMessage("Stopping stt...")?.queue()
        process?.destroy()
        Server.stop()
        process = null
    }

    fun send(bytes: ByteArray) {
        Server.send(bytes)
    }

    fun setLanguage(language: String) {
        this.language = language
        logger.info("Set stt language to $language")
    }

    private object Server {
        private var socket: ServerSocket? = null
        private var running = false
        private var client: Socket? = null

        fun run() {
            if (running) return

            logger.info("Starting communication server...")
            running = true
            val ip = "127.0.0.1"
            val port = Constants.SERVER_PORT

            socket = ServerSocket(port, 2, InetAddress.getByName(ip))
            logger.info("Server started on $ip:$port")

            while (running) {
                try {
                    client = socket!!.accept()

                    client!!.inputStream.bufferedReader().forEachLine { line ->
                        logger.info("New data: $line")
                        if (line.startsWith("#")) {
                            channel?.sendMessage("Ready!")?.queue()
                        } else {
                            SoundsComparator.recognize(line)
                        }
                    }
                } catch (_: SocketException) {
                    logger.info("Socker closed")
                }

                // if because `running` can be non-actual
                if (running) SttManager.run()
            }
        }

        fun send(bytes: ByteArray) {
            try {
                client?.outputStream?.write(bytes)
            } catch (_: SocketException) {}
        }

        fun stop() {
            running = false
            socket?.close()
            socket = null
        }
    }
}