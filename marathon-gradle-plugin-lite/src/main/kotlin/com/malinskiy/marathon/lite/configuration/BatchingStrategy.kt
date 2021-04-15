package com.malinskiy.marathon.lite.configuration

import com.malinskiy.marathon.lite.toTimestamp
import java.io.Serializable
import java.time.Instant
import com.malinskiy.marathon.cliconfig.proto.BatchingStrategy as ProtoBatchingStrategy

sealed class BatchingStrategy : Serializable {
    data class FixedSize(
        val fixedSize: Int,
        val durationMillis: Long? = null,
        val percentile: Double? = null,
        val timeLimit: Instant? = null,
        val lastMileLength: Int = 0
    ) : BatchingStrategy()

    object Disabled : BatchingStrategy()
}

fun BatchingStrategy.toProto(): ProtoBatchingStrategy {
    val builder = ProtoBatchingStrategy.newBuilder()
    when (this) {
        BatchingStrategy.Disabled -> builder.disabled = ProtoBatchingStrategy.Disabled.getDefaultInstance()
        is BatchingStrategy.FixedSize -> {
            val fixedSizeBuilder = ProtoBatchingStrategy.FixedSize.newBuilder()
            fixedSizeBuilder.fixedSize = this.fixedSize
            this.durationMillis?.let { fixedSizeBuilder.setDurationMillis(it) }
            this.percentile?.let { fixedSizeBuilder.setPercentile(it) }
            fixedSizeBuilder.timeLimit = this.timeLimit?.toTimestamp()
            builder.fixedSize = fixedSizeBuilder.build()
        }
    }
    return builder.build()
}
