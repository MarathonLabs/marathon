package marathon.model

data class FullTest(
    val pool_id: String,
    var package_name: String,
    var class_name: String,
    var name: String,
    var id: String = "$package_name$class_name$name",
    var filename: String,
    var duration_millis: Number,
    var status: Status,
    var stacktrace: String?,
    var deviceId: String,
    var diagnostic_video: Boolean,
    var diagnostic_screenshots: Boolean,
    var screenshot: String,
    var video: String,
    var log_file: String,
)
