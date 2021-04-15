package com.malinskiy.marathon.lite.configuration


import com.malinskiy.marathon.lite.toTimestamp
import java.io.Serializable
import java.time.Instant
import com.malinskiy.marathon.cliconfig.proto.SortingStrategy as ProtoSortingStrategy

sealed class SortingStrategy : Serializable {
    object Disabled : SortingStrategy()
    data class SuccessRate(val timeLimit: Instant, val ascending: Boolean = false) : SortingStrategy()
    data class ExecutionTime(val percentile: Double, val timeLimit: Instant) : SortingStrategy()
}

fun SortingStrategy.toProto(): ProtoSortingStrategy {
    val builder = ProtoSortingStrategy.newBuilder();
    when (this) {
        SortingStrategy.Disabled -> builder.disabled = ProtoSortingStrategy.Disabled.getDefaultInstance()
        is SortingStrategy.SuccessRate -> {
            val successRateBuilder = ProtoSortingStrategy.SuccessRate.newBuilder()
            successRateBuilder.timeLimit = timeLimit.toTimestamp()
            successRateBuilder.ascending = ascending
            builder.successRate = successRateBuilder.build()
        }
        is SortingStrategy.ExecutionTime -> {
            val executionTimeBuilder = ProtoSortingStrategy.ExecutionTime.newBuilder()
            executionTimeBuilder.timeLimit = timeLimit.toTimestamp()
            executionTimeBuilder.percentile = percentile
            builder.executionTime = executionTimeBuilder.build()
        }
    }
    return builder.build()
}


