package com.malinskiy.marathon.io


enum class FileType(val dir: String, val suffix: String) {
    TEST("tests", "xml"),
    TEST_RESULT("test_result", "json"),
    LOG("logs", "log"),
    DEVICE_LOG("device-logs", "log"),
    DEVICE_INFO("devices", "json"),
    VIDEO("video", "mp4"),
    SCREENSHOT("screenshot", "gif"),
    SCREENSHOT_PNG("screenshot", "png"),
    SCREENSHOT_JPG("screenshot", "jpg"),
    SCREENSHOT_WEBP("screenshot", "jpg"),
    SCREENSHOT_GIF("screenshot", "jpg"),
    XCTESTRUN("xctestrun", "xctestrun"),
    BILL("bill", "json"),
    TRACING("tracing", "perfetto-trace"),
}
