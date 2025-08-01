package ua.pp.lumivoid

import java.io.File

object Constants {
    val APP_PATH = File(System.getProperty("user.dir"), "app")
    val SOUNDS_PATH = "${APP_PATH}/sounds"
    val SOUNDS_FILE_PATH = "${APP_PATH}/sounds.json"
    val PYTHON_ENV_PATH = "${APP_PATH}/python"
    val PYTHON_CODE_PATH = "${APP_PATH}/stt"
    const val SERVER_PORT = 51777
}