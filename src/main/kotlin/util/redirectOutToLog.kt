package ua.pp.lumivoid.util

import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("External process logger")

fun redirectOutToLog(process: Process) {
    val outThread = Thread {
        process.inputStream.bufferedReader().forEachLine {
            logger.info(it)
        }
    }
    val errThread = Thread {
        process.errorStream.bufferedReader().forEachLine {
            logger.error(it)
        }
    }

    outThread.isDaemon = true
    errThread.isDaemon = true
    outThread.name = "External process logger out"
    errThread.name = "External process logger err"
    outThread.start()
    errThread.join()
}