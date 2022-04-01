package marathon.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Run(
    var title: String,
    @SerialName("total_failed") var totalFailed: Int,
    @SerialName("total_flaky") var totalFlaky: Int,
    @SerialName("total_ignored") var totalIgnored: Int,
    @SerialName("total_passed") var totalPassed: Int,
    @SerialName("total_duration_millis") var totalDuration: Long,
    @SerialName("average_duration_millis") var averageDuration: Long,
    @SerialName("max_duration_millis") var maxDuration: Long,
    @SerialName("min_duration_millis") var minDuration: Long,
    @SerialName("pools") var pools: List<PoolSummary>
)
