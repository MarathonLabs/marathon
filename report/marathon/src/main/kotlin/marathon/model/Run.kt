package marathon.model

data class Run(
    var title: String,
    var totalFailed: Int,
    var totalFlaky: Int,
    var totalIgnored: Int,
    var totalPassed: Int,
    var totalDuration: Number,
    var averageDuration: Number,
    var maxDuration: Number,
    var minDuration: Number,
    var pools: Array<PoolSummary>
)
