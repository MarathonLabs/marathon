package com.malinskiy.marathon.lite.configuration

import com.malinskiy.marathon.lite.toTimestamp
import java.io.Serializable
import java.time.Instant
import com.malinskiy.marathon.cliconfig.proto.FlakinessStrategy as ProtoFlakinessStrategy

sealed class FlakinessStrategy : Serializable {
    object Disabled : FlakinessStrategy()
    data class ProbabilityBased(val minSuccessRate: Double, val maxCount: Int, val timeLimit: Instant) : FlakinessStrategy()
}


fun FlakinessStrategy.toProto(): ProtoFlakinessStrategy {
    val builder = ProtoFlakinessStrategy.newBuilder()
    when (this) {
        FlakinessStrategy.Disabled -> builder.disabled = ProtoFlakinessStrategy.Disabled.getDefaultInstance()
        is FlakinessStrategy.ProbabilityBased -> {
            val probabilityBasedBuilder = ProtoFlakinessStrategy.ProbabilityBased.newBuilder()
            probabilityBasedBuilder.minSuccessRate = minSuccessRate
            probabilityBasedBuilder.maxCount = maxCount
            probabilityBasedBuilder.timeLimit = timeLimit.toTimestamp()
            builder.probabilityBased = probabilityBasedBuilder.build()
        }
    }
    return builder.build()
}
