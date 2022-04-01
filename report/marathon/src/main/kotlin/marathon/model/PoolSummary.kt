package marathon.model

import kotlinx.serialization.Serializable

@Serializable
data class PoolSummary(
    var id: String,
    var tests: List<ShortTest>,
    var passed_count: Int,
    var failed_count: Int,
    var ignored_count: Int,
    var duration_millis: Long,
    var devices: List<Device>
)
