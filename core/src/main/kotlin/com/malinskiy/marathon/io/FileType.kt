package com.malinskiy.marathon.io


// Artifact locations under marathon's output directory. Entries with a
// `html/` prefix are the ones the html-report links to (`../../../<dir>/...`
// from a per-test HTML at `html/pools/<pool>/<device>/<test>.html`); keeping
// them under `html/` makes the report a single self-contained subtree that
// archive tools can copy without also chasing sibling directories.
//
// Layout note (breaking change vs marathon <=0.10.x):
//   - `<output>/html/screenshot/<pool>/<device>/<name>.<ext>`
//   - `<output>/html/video/<pool>/<device>/<name>.mp4`
//   - `<output>/html/logs/<pool>/<device>/<name>.log`
//   - `<output>/html/device-logs/...`
// were previously at `<output>/screenshot/`, `<output>/video/`, etc.
// Downstream tools that hardcoded the old paths need to switch to the
// html-prefixed ones.
enum class FileType(val dir: String, val suffix: String) {
    TEST("tests", "xml"),
    TEST_RESULT("test_result", "json"),
    LOG("html/logs", "log"),
    DEVICE_LOG("html/device-logs", "log"),
    DEVICE_INFO("devices", "json"),
    VIDEO("html/video", "mp4"),
    SCREENSHOT("html/screenshot", "gif"),
    SCREENSHOT_PNG("html/screenshot", "png"),
    SCREENSHOT_JPG("html/screenshot", "jpg"),
    SCREENSHOT_WEBP("html/screenshot", "jpg"),
    SCREENSHOT_GIF("html/screenshot", "jpg"),
    XCTESTRUN("xctestrun", "xctestrun"),
    BILL("bill", "json"),
    TRACING("profiling", "profile"),
}
