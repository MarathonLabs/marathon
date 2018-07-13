package com.malinskiy.marathon.io


enum class FileType(val dir: String, val suffix: String) {
    TEST("tests", "xml"),
    TEST_RESULT("test_result", "json"),
    LOG("logs", "log"),
    DEVICE_INFO("devices", "json"),
    VIDEO("video", "mp4")

}
