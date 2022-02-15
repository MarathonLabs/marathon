package marathon.model

data class Run(
    var title: String,
    var totalFailed: Int,
    var totalFlaky: Int,
    var totalIgnored: Int,
    var totalPassed: Int,
    var totalDuration: Long,
    var averageDuration: Long,
    var maxDuration: Long,
    var minDuration: Long,
    var pools: Array<PoolSummary>
)
