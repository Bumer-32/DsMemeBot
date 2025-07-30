package ua.pp.lumivoid

import java.io.File

object Constants {
    val CONFIG_PATH = File(System.getProperty("user.dir"), "config")
    val URL_FILE_PATH = "${CONFIG_PATH}/model.txt"
    val MODEL_PATH = "${CONFIG_PATH}/model"
    val SOUNDS_PATH = "${CONFIG_PATH}/sounds"
    val SOUNDS_FILE_PATH = "${CONFIG_PATH}/sounds.json"
}