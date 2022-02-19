package marathon.model

data class ShortTest(
    var id: String,
    var package_name: String,
    var filename: String,
    var class_name: String,
    var name: String,
    var duration_millis: Number,
    var status: Status,
    var deviceId: String
)
