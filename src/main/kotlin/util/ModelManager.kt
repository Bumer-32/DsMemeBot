package ua.pp.lumivoid.util

import io.ktor.client.plugins.onDownload
import io.ktor.client.plugins.timeout
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.isSuccess
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import ua.pp.lumivoid.Constants
import ua.pp.lumivoid.Main
import java.io.File
import java.io.OutputStream
import java.util.zip.ZipFile
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.minutes

object ModelManager {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private var modelFolder = File(Constants.MODEL_PATH)

    private fun readUrlFile(): String {
        val file = File(Constants.URL_FILE_PATH)

        if (!file.exists()) {
            file.writeText("""# Place here, on second stroke url for your model (you can get it from here "https://alphacephei.com/vosk/models")""")
            logger.info("PLEASE PLACE URL IN model.txt FILE!")
            exitProcess(0)
        }

        val lines = file.readLines()
        val url = lines.find { it.startsWith("https://") }

        if (url == null) {
            logger.info("PLEASE PLACE URL IN model.txt FILE!")
            exitProcess(0)
        }

        return url
    }

    private suspend fun downloadFileWithProgress(
        url: String,
        outputStream: OutputStream,
        onProgress: (Float) -> Unit
    ) {
        Main.httpClient.prepareGet(
            urlString = url,
            block = {
                val timeout = 30.minutes.inWholeMilliseconds
                timeout {
                    requestTimeoutMillis = timeout
                    connectTimeoutMillis = timeout
                    socketTimeoutMillis = timeout
                }

                onDownload { bytesSentTotal, contentLength ->
                    val progress = (bytesSentTotal.toFloat() / contentLength!!.toFloat())
                    onProgress(progress)
                }
            }
        ).execute { response ->

            if (response.status.isSuccess()) {
                val byteReadChannel = response.bodyAsChannel()

                byteReadChannel.copyTo(outputStream)

            } else {
                logger.error("Failed to download file. HTTP Status: ${response.status.value}")
            }
        }
    }

    private fun unzip(zipFile: File, outputDir: File) {
        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                val outFile = File(outputDir, entry.name)
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
    }

    private suspend fun downloadModel() {
        val archive = File(modelFolder, "model.zip")
        val checkFile = File(modelFolder, "check")

        if (checkFile.exists()) return

        logger.info("Downloading model...")

        val  url = readUrlFile()
        modelFolder.mkdirs()

        val startTime = System.currentTimeMillis()

        println()
        downloadFileWithProgress(url, archive.outputStream(), onProgress = { progress ->
            // draw progressbar
            val barLength = 100
            val filledLength = (barLength * progress).toInt()
            val bar = "+".repeat(filledLength) + "-".repeat(barLength - filledLength)
            print("\r[$bar] ${(progress * 100).toInt()}%")
        })
        println()
        logger.info("Model downloaded in ${System.currentTimeMillis() - startTime}ms")

        // Unzipping
        logger.info("Unzipping model...")
        unzip(archive, modelFolder)

        logger.info("Deleting archive...")
        archive.delete()

        checkFile.createNewFile()
    }

    fun prepare() {
        runBlocking{ downloadModel() }
        modelFolder = modelFolder.listFiles()!!.first { it.isDirectory && it.name.startsWith("vosk-model")}
        logger.info("Model path: ${modelFolder.absolutePath}")
    }

    fun getModelPath(): String {
        return modelFolder.absolutePath
    }
}