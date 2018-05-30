package com.malinskiy.marathon.io


enum class FileType(val dir: String, val suffix: String) {
    TEST("tests", "xml"),
    LOG("logs", "log"),
    DEVICE_INFO("devices", "json")
}
