package marathon.model

data class TestLogDetails(
    var pool_id: String,
    var test_id: String,
    var display_name: String,
    var device_id: String,
    var log_path: String
)
