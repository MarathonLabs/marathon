package marathon.model

data class PoolSummary(
    var id: String,
    var tests: Array<ShortTest>,
    var passed_count: Int,
    var failed_count: Int,
    var ignored_count: Int,
    var duration_millis: Long,
    var devices: Array<Device>
)
