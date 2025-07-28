package ua.pp.lumivoid.commands

import org.slf4j.LoggerFactory

abstract class Command(protected val command: String) {
    protected val logger = LoggerFactory.getLogger(this.javaClass)!!

    init {
        logger.info("Registering command: $command")
    }

    abstract fun register()
}